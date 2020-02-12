/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.api

import com.github.salomonbrys.kodein.instance
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.service.UnknownApplicationException
import ai.tock.nlp.front.shared.codec.ApplicationDump
import ai.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import ai.tock.nlp.front.shared.codec.CreateApplicationQuery
import ai.tock.nlp.front.shared.codec.SentencesDump
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.evaluation.EntityEvaluationQuery
import ai.tock.nlp.front.shared.merge.ValuesMergeQuery
import ai.tock.nlp.front.shared.monitoring.MarkAsUnknownQuery
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.namespace
import ai.tock.shared.property
import ai.tock.shared.security.auth.TockAuthProvider
import ai.tock.shared.security.initEncryptor
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.detailedHealthcheck
import ai.tock.shared.pingMongoDatabase
import com.mongodb.MongoClient
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

/**
 *
 */
class NlpVerticle : WebVerticle() {

    private val protectPath = verticleBooleanProperty("tock_nlp_protect_path", false)

    override val rootPath: String = property("tock_nlp_root", "/rest/nlp")

    private val executor: Executor by injector.instance()
    private val mongoClient: MongoClient by injector.instance()

    override val logger: KLogger = KotlinLogging.logger {}

    override fun authProvider(): TockAuthProvider? {
        return if (protectPath) defaultAuthProvider() else super.authProvider()
    }

    override fun configure() {
        val front = FrontClient
        initEncryptor()

        blockingJsonPost("/parse") { context, query: ParseQuery ->
            if (protectPath && context.organization != query.namespace) {
                unauthorized()
            } else if (query.queries.isEmpty()) {
                badRequest("please set queries field with at least one query")
            } else {
                try {
                    front.parse(query)
                } catch (e: UnknownApplicationException) {
                    badRequest(e.message ?: "")
                }
            }
        }

        blockingJsonPost("/evaluate") { context, query: EntityEvaluationQuery ->
            if (protectPath && context.organization != query.namespace) {
                unauthorized()
            } else {
                front.evaluateEntities(query)
            }
        }

        blockingJsonPost("/merge") { context, query: ValuesMergeQuery ->
            if (protectPath && context.organization != query.namespace) {
                unauthorized()
            } else {
                front.mergeValues(query)
            }
        }

        blockingJsonPost("/unknown") { context, query: MarkAsUnknownQuery ->
            if (protectPath && context.organization != query.namespace) {
                unauthorized()
            } else {
                front.incrementUnknown(query)
            }
        }

        blockingJsonGet("/intents") { context ->
            val namespace = context.firstQueryParam("namespace")
            if (protectPath && context.organization != namespace) {
                unauthorized()
            } else {
                val name = context.firstQueryParam("name")
                if (namespace == null || name == null) {
                    badRequest("One of the parameters name or namespace is invalid")
                } else {
                    front.getApplicationByNamespaceAndName(namespace, name)
                        ?.run {
                            front.getIntentsByApplicationId(_id)
                        } ?: emptyList()
                }
            }
        }

        blockingJsonGet("/application") { context ->
            val namespace = context.firstQueryParam("namespace")
            if (protectPath && context.organization != namespace) {
                unauthorized()
            } else {
                val name = context.firstQueryParam("name")
                if (namespace == null || name == null) {
                    badRequest("One of the parameters name or namespace is invalid")
                } else {
                    front.getApplicationByNamespaceAndName(namespace, name)
                }
            }
        }

        blockingJsonPost("/application/create") { context, query: CreateApplicationQuery ->
            if (protectPath && context.organization != query.namespace) {
                unauthorized()
            } else {
                if (front.getApplicationByNamespaceAndName(query.namespace, query.name) == null) {
                    front.save(
                        ApplicationDefinition(
                            query.name,
                            query.label ?: query.name,
                            query.namespace,
                            supportedLocales = setOf(query.locale)
                        )
                    )
                } else {
                    null
                }
            }
        }

        blockingUploadJsonPost("/dump/import") { _, dump: ApplicationDump ->
            if (protectPath) {
                unauthorized()
            } else {
                front.import(
                    dump.application.namespace,
                    dump,
                    ApplicationImportConfiguration(defaultModelMayExist = true)
                ).modified
            }
        }

        blockingJsonPost("/dump/import/plain") { _, dump: ApplicationDump ->
            if (protectPath) {
                unauthorized()
            } else {
                front.import(
                    dump.application.namespace,
                    dump,
                    ApplicationImportConfiguration(defaultModelMayExist = true)
                ).modified
            }
        }

        blockingUploadJsonPost("/dump/import/sentences") { _, dump: SentencesDump ->
            if (protectPath) {
                unauthorized()
            } else {
                front.importSentences(dump.applicationName.namespace(), dump).modified
            }
        }

        blockingJsonPost("/dump/import/sentences/plain") { _, dump: SentencesDump ->
            if (protectPath) {
                unauthorized()
            } else {
                front.importSentences(dump.applicationName.namespace(), dump).modified
            }
        }
    }

    override fun defaultHealthcheck(): (RoutingContext) -> Unit {
        return {
            executor.executeBlocking {
                it.response().setStatusCode(if (FrontClient.healthcheck()) 200 else 500).end()
            }
        }
    }

    override fun detailedHealthcheck(): (RoutingContext) -> Unit = detailedHealthcheck(
        listOf(
            Pair("duckling_service", { FrontClient.healthcheck() }),
            Pair("tock_front_database", { pingMongoDatabase(mongoClient.getDatabase("tock_front")) }),
            Pair("tock_model_database", { pingMongoDatabase(mongoClient.getDatabase("tock_model")) })
        )
    )

}