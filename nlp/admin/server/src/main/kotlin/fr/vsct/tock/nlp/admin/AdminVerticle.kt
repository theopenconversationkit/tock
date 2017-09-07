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

package fr.vsct.tock.nlp.admin

import fr.vsct.tock.nlp.admin.model.ApplicationScopedQuery
import fr.vsct.tock.nlp.admin.model.ApplicationWithIntents
import fr.vsct.tock.nlp.admin.model.CreateEntityQuery
import fr.vsct.tock.nlp.admin.model.EntityTestErrorWithSentenceReport
import fr.vsct.tock.nlp.admin.model.IntentTestErrorWithSentenceReport
import fr.vsct.tock.nlp.admin.model.LogStatsQuery
import fr.vsct.tock.nlp.admin.model.LogsQuery
import fr.vsct.tock.nlp.admin.model.ParseQuery
import fr.vsct.tock.nlp.admin.model.SearchQuery
import fr.vsct.tock.nlp.admin.model.SentenceReport
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.shared.codec.ApplicationDump
import fr.vsct.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import fr.vsct.tock.nlp.front.shared.codec.DumpType
import fr.vsct.tock.nlp.front.shared.codec.SentencesDump
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.test.TestErrorQuery
import fr.vsct.tock.nlp.front.shared.updater.ModelBuildTrigger
import fr.vsct.tock.shared.devEnvironment
import fr.vsct.tock.shared.name
import fr.vsct.tock.shared.security.initEncryptor
import fr.vsct.tock.shared.vertx.BadRequestException
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import mu.KLogger
import mu.KotlinLogging
import java.util.Locale

/**
 *
 */
open class AdminVerticle(logger: KLogger = KotlinLogging.logger {}) : WebVerticle(logger) {

    override val rootPath: String = "/rest/admin"

    override fun authProvider(): AuthProvider? = authProvider

    fun configureServices() {
        val front = FrontClient
        val admin = AdminService

        initEncryptor()

        blockingJsonGet("/applications") { context ->
            front.getApplications().filter {
                it.namespace == context.organization
            }.map {
                admin.getApplicationWithIntents(it)
            }
        }

        blockingJsonGet("/application/:id") { context ->
            admin.getApplicationWithIntents(context.pathParam("id"))
                    ?.takeIf { it.namespace == context.organization }
        }

        blockingJsonGet("/application/dump/:id") {
            val id = it.pathParam("id")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.export(id, DumpType.full)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/sentences/dump/:applicationId") {
            val id = it.pathParam("applicationId")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(id, null, DumpType.full)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/sentences/dump/:applicationId/:intent") {
            val id = it.pathParam("applicationId")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(id, it.pathParam("intent"), DumpType.full)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/application") { context, application: ApplicationWithIntents ->
            if (context.organization == application.namespace
                    && (application._id == null || context.organization == front.getApplicationById(application._id)?.namespace)) {
                val appWithSameName = front.getApplicationByNamespaceAndName(application.namespace, application.name)
                if (appWithSameName != null && appWithSameName._id != application._id) {
                    throw BadRequestException("Application with same name already exists")
                }
                val newApp = front.save(application.toApplication().copy(name = application.name.toLowerCase()))
                //trigger a full rebuild if nlp engine change
                if (appWithSameName?.nlpEngineType != newApp.nlpEngineType) {
                    front.triggerBuild(ModelBuildTrigger(newApp._id!!, true))
                }
                ApplicationWithIntents(newApp, front.getIntentsByApplicationId(newApp._id!!))
            } else {
                unauthorized()
            }
        }

        blockingUploadJsonPost("/dump/application") { context, dump: ApplicationDump ->
            front.import(context.organization, dump)
        }

        blockingUploadJsonPost("/dump/sentences") { context, dump: SentencesDump ->
            front.importSentences(context.organization, dump)
        }

        blockingUploadJsonPost("/dump/application/:name") { context, dump: ApplicationDump ->
            front.import(context.organization, dump, ApplicationImportConfiguration(context.pathParam("name")))
        }

        blockingUploadJsonPost("/dump/sentences/:name") { context, dump: SentencesDump ->
            front.importSentences(context.organization, dump.copy(applicationName = context.pathParam("name")))
        }

        blockingDelete("/application/:id") {
            val id = it.pathParam("id")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.deleteApplicationById(id)
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete("/application/:appId/intent/:intentId") {
            val app = front.getApplicationById(it.pathParam("appId"))
            val intentId = it.pathParam("intentId")
            if (it.organization == app?.namespace) {
                front.removeIntentFromApplication(app, intentId)
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete("/application/:appId/intent/:intentId/entity/:entityType/:role") {
            val app = front.getApplicationById(it.pathParam("appId"))
            val intentId = it.pathParam("intentId")
            val entityType = it.pathParam("entityType")
            val role = it.pathParam("role")
            val intent = front.getIntentById(intentId)!!
            if (intent.applications.size == 1 && it.organization == app?.namespace && it.organization == intent.namespace) {
                front.removeEntityFromIntent(app, intent, entityType, role)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/locales") {
            Locale.getAvailableLocales()
                    .filter { it.language.isNotEmpty() }
                    .distinctBy { it.language }
                    .map { it.language to it.getDisplayLanguage(Locale.ENGLISH).capitalize() }
                    .sortedBy { it.second }
        }

        blockingJsonPost("/parse") { context, query: ParseQuery ->
            if (context.organization == query.namespace) {
                admin.parseSentence(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/sentence") { context, s: SentenceReport ->
            if (context.organization == front.getApplicationById(s.applicationId)?.namespace) {
                front.save(s.toClassifiedSentence())
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/sentences/search") { context, s: SearchQuery ->
            if (context.organization == s.namespace) {
                admin.searchSentences(s)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/logs/search") { context, s: LogsQuery ->
            if (context.organization == s.namespace) {
                admin.searchLogs(s)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/logs/stats") { context, s: LogStatsQuery ->
            if (context.organization == s.namespace) {
                front.stats(s.toStatQuery(front.getApplicationByNamespaceAndName(s.namespace, s.applicationName)!!))
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/intent") { context, intent: IntentDefinition ->
            AdminService.createOrUpdateIntent(context.organization, intent) ?: unauthorized()
        }

        blockingJsonGet("/intents") { context ->
            front.getApplications().filter {
                it.namespace == context.organization
            }.map {
                admin.getApplicationWithIntents(it)
            }
        }

        blockingJsonGet("/entities") { front.getEntityTypes() }

        blockingJsonGet("/nlp-engines") { front.getSupportedNlpEngineTypes() }

        blockingJsonPost<CreateEntityQuery, EntityTypeDefinition?>("/entity/create") { context, query ->
            val entityName = "${context.organization}:${query.type.toLowerCase().name()}"
            if (front.getEntityTypeByName(entityName) == null) {
                val entityType = EntityTypeDefinition(entityName, "")
                front.save(entityType)
                entityType
            } else {
                null
            }
        }

        blockingJsonPost("/test/intent-errors") { context, query: TestErrorQuery ->
            if (context.organization == front.getApplicationById(query.applicationId)?.namespace) {
                AdminService.searchTestIntentErrors(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/intent-error/delete") { context, error: IntentTestErrorWithSentenceReport ->
            if (context.organization == front.getApplicationById(error.sentence.applicationId)?.namespace) {
                front.deleteTestIntentError(error.sentence.applicationId, error.sentence.language, error.sentence.toClassifiedSentence().text)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/entity-errors") { context, query: TestErrorQuery ->
            if (context.organization == front.getApplicationById(query.applicationId)?.namespace) {
                AdminService.searchTestEntityErrors(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/entity-error/delete") { context, error: EntityTestErrorWithSentenceReport ->
            if (context.organization == front.getApplicationById(error.sentence.applicationId)?.namespace) {
                front.deleteTestEntityError(error.sentence.applicationId, error.sentence.language, error.sentence.toClassifiedSentence().text)

            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/stats") { context, query: ApplicationScopedQuery ->
            if (context.organization == front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)?.namespace) {
                AdminService.testBuildStats(query)
            } else {
                unauthorized()
            }
        }
    }

    fun configureStaticHandling() {
        if (!devEnvironment) {
            //serve statics in docker image
            val webRoot = verticleProperty("content_path", "/maven/dist")
            router.route("/*").handler(StaticHandler.create().setAllowRootFileSystemAccess(true).setWebRoot(webRoot))
            router.route().failureHandler { context ->
                val code = if (context.statusCode() > 0) context.statusCode() else 500
                if (code == 404) {
                    context.vertx().fileSystem().readFile("$webRoot/index.html") {
                        if (it.succeeded()) {
                            context.response().end(it.result())
                        } else {
                            logger.warn { "Can't find $webRoot/index.html" }
                            context.response().statusCode = code
                            context.response().end()
                        }
                    }
                } else {
                    context.response().statusCode = code
                    context.response().end()
                }
            }
        }
    }

    override fun configure() {
        configureServices()
        configureStaticHandling()
    }

    override fun healthcheck(): (RoutingContext) -> Unit {
        return { it.response().end() }
    }
}
