/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.api

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.service.UnknownApplicationException
import fr.vsct.tock.nlp.front.shared.codec.ApplicationDump
import fr.vsct.tock.nlp.front.shared.codec.SentencesDump
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.evaluation.EntityEvaluationQuery
import fr.vsct.tock.nlp.front.shared.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseIntentEntitiesQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.namespace
import fr.vsct.tock.shared.security.initEncryptor
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

/**
 *
 */
class NlpVerticle : WebVerticle() {

    private val protectPath = verticleBooleanProperty("tock_nlp_protect_path", false)

    override val rootPath: String = "/rest/nlp"

    private val executor: Executor by injector.instance()

    override val logger: KLogger = KotlinLogging.logger {}

    override fun authProvider(): AuthProvider? {
        return if (protectPath) defaultAuthProvider() else super.authProvider()
    }

    override fun configure() {
        val front = FrontClient
        initEncryptor()

        blockingJsonPost("/parse/intent/entities") { context, query: ParseIntentEntitiesQuery ->
            if (protectPath && context.organization != query.query.namespace) {
                unauthorized()
            } else if (query.query.queries.isEmpty()) {
                error("please set queries field with at least one query")
            } else {
                front.parseIntentEntities(query)
            }
        }

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

        blockingJsonGet("/intents") { context ->
            if (protectPath) {
                unauthorized()
            } else {
                if(context.firstQueryParam("namespace") == null || context.firstQueryParam("name") == null) {
                    badRequest("One of the parameters name or namespace is invalid")
                }
                else {
                    val applicationDefinition: ApplicationDefinition? = front.getApplicationByNamespaceAndName(context.firstQueryParam("namespace")!!, context.firstQueryParam("name")!!)
                    when {
                        applicationDefinition != null -> front.getIntentsByApplicationId(applicationDefinition._id)
                        else -> mutableListOf()
                    }
                }

            }
        }

        blockingUploadJsonPost("/dump/import") { _, dump: ApplicationDump ->
            if (protectPath) {
                unauthorized()
            } else {
                front.import(dump.application.namespace, dump).modified
            }
        }

        blockingJsonPost("/dump/import/plain") { _, dump: ApplicationDump ->
            if (protectPath) {
                unauthorized()
            } else {
                front.import(dump.application.namespace, dump).modified
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

    override fun healthcheck(): (RoutingContext) -> Unit {
        return {
            executor.executeBlocking {
                it.response().setStatusCode(if (FrontClient.healthcheck()) 200 else 500).end()
            }
        }
    }
}