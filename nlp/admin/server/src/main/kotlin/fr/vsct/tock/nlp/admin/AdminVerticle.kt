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
import fr.vsct.tock.nlp.admin.model.PaginatedQuery
import fr.vsct.tock.nlp.admin.model.ParseQuery
import fr.vsct.tock.nlp.admin.model.SearchQuery
import fr.vsct.tock.nlp.admin.model.SentenceReport
import fr.vsct.tock.nlp.admin.model.UpdateEntityDefinitionQuery
import fr.vsct.tock.nlp.admin.model.UpdateSentencesQuery
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.shared.build.ModelBuildTrigger
import fr.vsct.tock.nlp.front.shared.codec.ApplicationDump
import fr.vsct.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import fr.vsct.tock.nlp.front.shared.codec.DumpType
import fr.vsct.tock.nlp.front.shared.codec.SentencesDump
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.test.TestErrorQuery
import fr.vsct.tock.shared.cache.getCachedValuesForType
import fr.vsct.tock.shared.cache.removeFromCache
import fr.vsct.tock.shared.devEnvironment
import fr.vsct.tock.shared.name
import fr.vsct.tock.shared.namespace
import fr.vsct.tock.shared.security.initEncryptor
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 *
 */
open class AdminVerticle : WebVerticle() {

    override val logger: KLogger = KotlinLogging.logger {}

    override val rootPath: String = "/rest/admin"

    override fun authProvider(): AuthProvider? = defaultAuthProvider()

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
            admin.getApplicationWithIntents(context.pathId("id"))
                    ?.takeIf { it.namespace == context.organization }
        }

        blockingJsonGet("/application/dump/:id") {
            val id: Id<ApplicationDefinition> = it.pathId("id")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.export(id, DumpType.full)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/sentences/dump/:applicationId") {
            val id: Id<ApplicationDefinition> = it.pathId("applicationId")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(id, null, null, DumpType.full)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/sentences/dump/:applicationId") { context, query: SearchQuery ->
            val id: Id<ApplicationDefinition> = context.pathId("applicationId")
            if (context.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(id, null, query.toSentencesQuery(id), DumpType.full)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/sentences/dump/:applicationId/:intent") {
            val id: Id<ApplicationDefinition> = it.pathId("applicationId")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(id, it.pathParam("intent"), null, DumpType.full)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/application") { context, application: ApplicationWithIntents ->
            if (context.organization == application.namespace
                    && (application._id == null || context.organization == front.getApplicationById(application._id)?.namespace)) {
                val appWithSameName = front.getApplicationByNamespaceAndName(application.namespace, application.name)
                if (appWithSameName != null && appWithSameName._id != application._id) {
                    badRequest("Application with same name already exists")
                }
                val newApp = front.save(application.toApplication().copy(name = application.name.toLowerCase()))
                //trigger a full rebuild if nlp engine change
                if (appWithSameName?.nlpEngineType != newApp.nlpEngineType) {
                    front.triggerBuild(ModelBuildTrigger(newApp._id, true))
                }
                ApplicationWithIntents(newApp, front.getIntentsByApplicationId(newApp._id))
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/application/build/trigger") { context, application: ApplicationWithIntents ->
            val app = front.getApplicationById(application._id!!)
            if (context.organization == app!!.namespace) {
                front.triggerBuild(ModelBuildTrigger(app._id, true))
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/application/builds") { context, query: PaginatedQuery ->
            val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
            if (context.organization == app?.namespace) {
                front.builds(app._id, query.language, query.start.toInt(), query.size)
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
            val id: Id<ApplicationDefinition> = it.pathId("id")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.deleteApplicationById(id)
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete("/application/:appId/intent/:intentId") {
            val app = front.getApplicationById(it.pathId("appId"))
            val intentId: Id<IntentDefinition> = it.pathId("intentId")
            if (it.organization == app?.namespace) {
                front.removeIntentFromApplication(app, intentId)
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete("/application/:appId/intent/:intentId/entity/:entityType/:role") {
            val app = front.getApplicationById(it.pathId("appId"))
            val intentId: Id<IntentDefinition> = it.pathId("intentId")
            val entityType = it.pathParam("entityType")
            val role = it.pathParam("role")
            val intent = front.getIntentById(intentId)!!
            if (intent.applications.size == 1 && it.organization == app?.namespace && it.organization == intent.namespace) {
                front.removeEntityFromIntent(app, intent, entityType, role)
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete("/application/:appId/entity/:entityType/:role") {
            //TODO rights
            val app = front.getApplicationById(it.pathId("appId"))!!
            val entityTypeName = it.pathParam("entityType")
            val role = it.pathParam("role")
            val entityType = front.getEntityTypeByName(entityTypeName)!!
            front.removeSubEntityFromEntity(app, entityType, role)
        }

        blockingJsonGet("/locales")
        {
            Locale.getAvailableLocales()
                    .filter { it.language.isNotEmpty() }
                    .distinctBy { it.language }
                    .map { it.language to it.getDisplayLanguage(Locale.ENGLISH).capitalize() }
                    .sortedBy { it.second }
        }

        blockingJsonPost("/parse")
        { context, query: ParseQuery ->
            if (context.organization == query.namespace) {
                admin.parseSentence(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/sentence")
        { context, s: SentenceReport ->
            if (context.organization == front.getApplicationById(s.applicationId)?.namespace) {
                front.save(s.toClassifiedSentence())
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/sentences/search")
        { context, s: SearchQuery ->
            if (context.organization == s.namespace) {
                admin.searchSentences(s)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/sentences/update")
        { context, s: UpdateSentencesQuery ->
            if (context.organization == s.namespace && context.organization == s.searchQuery.namespace) {
                admin.updateSentences(s)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/logs/search")
        { context, s: LogsQuery ->
            if (context.organization == s.namespace) {
                admin.searchLogs(s)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/logs/stats")
        { context, s: LogStatsQuery ->
            if (context.organization == s.namespace) {
                front.stats(s.toStatQuery(front.getApplicationByNamespaceAndName(s.namespace, s.applicationName)!!))
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/intent")
        { context, intent: IntentDefinition ->
            AdminService.createOrUpdateIntent(context.organization, intent) ?: unauthorized()
        }

        blockingJsonGet("/intents")
        { context ->
            front.getApplications().filter {
                it.namespace == context.organization
            }.map {
                        admin.getApplicationWithIntents(it)
                    }
        }

        blockingJsonGet("/entity-types")
        { front.getEntityTypes() }

        blockingJsonPost("/entity")
        { context, query: UpdateEntityDefinitionQuery ->
            if (context.organization == query.namespace) {
                front.updateEntityDefinition(query.namespace, query.applicationName, query.entity)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/nlp-engines")
        { front.getSupportedNlpEngineTypes() }

        blockingJsonPost<CreateEntityQuery, EntityTypeDefinition?>("/entity-type/create")
        { context, query ->
            val entityName = "${context.organization}:${query.type.toLowerCase().name()}"
            if (front.getEntityTypeByName(entityName) == null) {
                val entityType = EntityTypeDefinition(entityName, "")
                front.save(entityType)
                entityType
            } else {
                null
            }
        }

        blockingJsonPost("/entity-type")
        { context, entityType: EntityTypeDefinition ->
            if (context.organization == entityType.name.namespace()) {
                val update = front.getEntityTypeByName(entityType.name)
                        ?.run {
                            copy(
                                    description = entityType.description,
                                    subEntities = entityType.subEntities
                            )
                        }
                if (update != null) {
                    front.save(update)
                } else {
                    error("not existing entity $entityType")
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/intent-errors")
        { context, query: TestErrorQuery ->
            if (context.organization == front.getApplicationById(query.applicationId)?.namespace) {
                AdminService.searchTestIntentErrors(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/intent-error/delete")
        { context, error: IntentTestErrorWithSentenceReport ->
            if (context.organization == front.getApplicationById(error.sentence.applicationId)?.namespace) {
                front.deleteTestIntentError(error.sentence.applicationId, error.sentence.language, error.sentence.toClassifiedSentence().text)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/entity-errors")
        { context, query: TestErrorQuery ->
            if (context.organization == front.getApplicationById(query.applicationId)?.namespace) {
                AdminService.searchTestEntityErrors(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/entity-error/delete")
        { context, error: EntityTestErrorWithSentenceReport ->
            if (context.organization == front.getApplicationById(error.sentence.applicationId)?.namespace) {
                front.deleteTestEntityError(error.sentence.applicationId, error.sentence.language, error.sentence.toClassifiedSentence().text)

            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/stats")
        { context, query: ApplicationScopedQuery ->
            if (context.organization == front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)?.namespace) {
                AdminService.testBuildStats(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/alexa/export")
        { context, query: ApplicationScopedQuery ->
            val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
            if (app != null && context.organization == app.namespace) {
                front.exportIntentsSchema(app.name, app._id, query.language)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/cache/:type") {
            //TODO admin role
            getCachedValuesForType<Any>(it.pathParam("type"))
        }

        blockingJsonDelete("/cache/:id/:type") {
            //TODO admin role
            removeFromCache<Any>(it.pathId("id"), it.pathParam("type"))
            true
        }
    }

    fun configureStaticHandling() {
        if (!devEnvironment) {
            //serve statics in docker image
            val webRoot = verticleProperty("content_path", "/maven/dist")
            //swagger yaml
            router.get("/doc/nlp.yaml").handler { context ->
                context.vertx().fileSystem().readFile("$webRoot/doc/nlp.yaml") {
                    if (it.succeeded()) {
                        context.response().end(it.result().toString(StandardCharsets.UTF_8).replace("_HOST_", verticleProperty("nlp_external_host", "localhost:8888")))
                    } else {
                        context.fail(it.cause())
                    }
                }
            }
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
