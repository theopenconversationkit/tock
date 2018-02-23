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
import fr.vsct.tock.nlp.admin.model.EntityTestErrorQueryResultReport
import fr.vsct.tock.nlp.admin.model.EntityTestErrorWithSentenceReport
import fr.vsct.tock.nlp.admin.model.IntentTestErrorQueryResultReport
import fr.vsct.tock.nlp.admin.model.IntentTestErrorWithSentenceReport
import fr.vsct.tock.nlp.admin.model.LogStatsQuery
import fr.vsct.tock.nlp.admin.model.LogsQuery
import fr.vsct.tock.nlp.admin.model.PaginatedQuery
import fr.vsct.tock.nlp.admin.model.ParseQuery
import fr.vsct.tock.nlp.admin.model.SearchQuery
import fr.vsct.tock.nlp.admin.model.SentenceReport
import fr.vsct.tock.nlp.admin.model.SentencesReport
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
import fr.vsct.tock.shared.BUILTIN_ENTITY_EVALUATOR_NAMESPACE
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.devEnvironment
import fr.vsct.tock.shared.name
import fr.vsct.tock.shared.namespace
import fr.vsct.tock.shared.security.TockUserRole.admin
import fr.vsct.tock.shared.security.TockUserRole.technicalAdmin
import fr.vsct.tock.shared.security.initEncryptor
import fr.vsct.tock.shared.vertx.WebVerticle
import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod.GET
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

    //TODO remove this flag when the new platform is set
    private val OLD_ENTITY_TYPE_BEHAVIOUR = booleanProperty("tock_nlp_admin_old_entity_type_behaviour", false)

    fun configureServices() {
        val front = FrontClient
        val service = AdminService

        initEncryptor()

        blockingJsonGet("/applications") { context ->
            front.getApplications().filter {
                it.namespace == context.organization
            }.map {
                    service.getApplicationWithIntents(it)
                }
        }

        blockingJsonGet("/application/:id") { context ->
            service.getApplicationWithIntents(context.pathId("id"))
                ?.takeIf { it.namespace == context.organization }
        }

        blockingJsonGet("/application/dump/:id", technicalAdmin) {
            val id: Id<ApplicationDefinition> = it.pathId("id")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.export(id, DumpType.full)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/sentences/dump/:dumpType/:applicationId", admin) {
            val id: Id<ApplicationDefinition> = it.pathId("applicationId")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    id,
                    null,
                    null,
                    DumpType.parseDumpType(it.pathParam("dumpType"))
                )
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/sentences/dump/:dumpType/:applicationId", admin) { context, query: SearchQuery ->
            val id: Id<ApplicationDefinition> = context.pathId("applicationId")
            if (context.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    id,
                    null,
                    query.toSentencesQuery(id),
                    DumpType.parseDumpType(
                        context.pathParam("dumpType")
                    )
                )
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/sentences/dump/:dumpType/:applicationId/:intent", admin) {
            val id: Id<ApplicationDefinition> = it.pathId("applicationId")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    id, it.pathParam("intent"),
                    null,
                    DumpType.parseDumpType(it.pathParam("dumpType"))
                )
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/sentences/dump/:dumpType/:applicationId", technicalAdmin) { context, query: SearchQuery ->
            val id: Id<ApplicationDefinition> = context.pathId("applicationId")
            if (context.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    id,
                    null,
                    query.toSentencesQuery(id),
                    DumpType.parseDumpType(context.pathParam("dumpType"))
                )
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/application", admin) { context, application: ApplicationWithIntents ->
            if (context.organization == application.namespace
                && (application._id == null || context.organization == front.getApplicationById(application._id)?.namespace)
            ) {
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

        blockingJsonPost("/application/build/trigger", admin) { context, application: ApplicationWithIntents ->
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

        blockingUploadJsonPost("/dump/application", technicalAdmin) { context, dump: ApplicationDump ->
            front.import(context.organization, dump)
        }

        blockingUploadJsonPost("/dump/sentences", admin) { context, dump: SentencesDump ->
            front.importSentences(context.organization, dump)
        }

        blockingUploadJsonPost("/dump/application/:name", admin) { context, dump: ApplicationDump ->
            front.import(context.organization, dump, ApplicationImportConfiguration(context.pathParam("name")))
        }

        blockingUploadJsonPost("/dump/sentences/:name", admin) { context, dump: SentencesDump ->
            front.importSentences(context.organization, dump.copy(applicationName = context.pathParam("name")))
        }

        blockingDelete("/application/:id", admin) {
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
            val app = front.getApplicationById(it.pathId("appId"))!!
            val entityTypeName = it.pathParam("entityType")
            val role = it.pathParam("role")
            val entityType = front.getEntityTypeByName(entityTypeName)!!
            if (it.organization == app.namespace && it.organization == entityType.name.namespace()) {
                front.removeSubEntityFromEntity(app, entityType, role)
            } else {
                unauthorized()
            }
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
                service.parseSentence(query)
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

        jsonPost("/sentences/search")
        { context, s: SearchQuery, handler: Handler<SentencesReport> ->
            if (context.organization == s.namespace) {
                context.isAuthorized(technicalAdmin) { plain ->
                    context.executeBlocking {
                        handler.handle(service.searchSentences(s, !(plain.result() ?: false)))
                    }
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/sentences/update", admin)
        { context, s: UpdateSentencesQuery ->
            if (context.organization == s.namespace
                && (s.searchQuery == null || context.organization == s.searchQuery.namespace)
            ) {
                service.updateSentences(s)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/logs/search")
        { context, s: LogsQuery ->
            if (context.organization == s.namespace) {
                service.searchLogs(s)
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
                    service.getApplicationWithIntents(it)
                }
        }

        blockingJsonGet("/entity-types") { context ->
            if (OLD_ENTITY_TYPE_BEHAVIOUR) {
                front.getEntityTypes()
            } else {
                front.getEntityTypes().filter {
                    it.name.namespace() == context.organization || it.name.namespace() == BUILTIN_ENTITY_EVALUATOR_NAMESPACE
                }
            }
        }

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

        blockingJsonDelete("/entity-type/:name")
        {
            val entityType = it.pathParam("name")
            if (it.organization == entityType.namespace()) {
                front.deleteEntityTypeByName(entityType)
            } else {
                unauthorized()
            }
        }

        jsonPost("/test/intent-errors")
        { context, query: TestErrorQuery, handler: Handler<IntentTestErrorQueryResultReport> ->
            if (context.organization == front.getApplicationById(query.applicationId)?.namespace) {
                context.isAuthorized(technicalAdmin) { plain ->
                    context.executeBlocking {
                        handler.handle(AdminService.searchTestIntentErrors(query, !(plain.result() ?: false)))
                    }
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/intent-error/delete")
        { context, error: IntentTestErrorWithSentenceReport ->
            if (context.organization == front.getApplicationById(error.sentence.applicationId)?.namespace) {
                front.deleteTestIntentError(
                    error.sentence.applicationId,
                    error.sentence.language,
                    error.sentence.toClassifiedSentence().text
                )
            } else {
                unauthorized()
            }
        }

        jsonPost("/test/entity-errors")
        { context, query: TestErrorQuery, handler: Handler<EntityTestErrorQueryResultReport> ->
            if (context.organization == front.getApplicationById(query.applicationId)?.namespace) {
                context.isAuthorized(technicalAdmin) { plain ->
                    context.executeBlocking {
                        handler.handle(service.searchTestEntityErrors(query, !(plain.result() ?: false)))
                    }
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/entity-error/delete")
        { context, error: EntityTestErrorWithSentenceReport ->
            if (context.organization == front.getApplicationById(error.sentence.applicationId)?.namespace) {
                front.deleteTestEntityError(
                    error.sentence.applicationId,
                    error.sentence.language,
                    error.sentence.toClassifiedSentence().text
                )

            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/stats")
        { context, query: ApplicationScopedQuery ->
            if (context.organization == front.getApplicationByNamespaceAndName(
                    query.namespace,
                    query.applicationName
                )?.namespace
            ) {
                AdminService.testBuildStats(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/alexa/export", admin)
        { context, query: ApplicationScopedQuery ->
            val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
            if (app != null && context.organization == app.namespace) {
                front.exportIntentsSchema(app.name, app._id, query.language)
            } else {
                unauthorized()
            }
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
                        context.response().end(
                            it.result().toString(StandardCharsets.UTF_8).replace(
                                "_HOST_",
                                verticleProperty("nlp_external_host", "localhost:8888")
                            )
                        )
                    } else {
                        context.fail(it.cause())
                    }
                }
            }
            router.route(GET, "/*")
                .handler(StaticHandler.create().setAllowRootFileSystemAccess(true).setWebRoot(webRoot))
                .handler { context ->
                    context.vertx().fileSystem().readFile("$webRoot/index.html") {
                        if (it.succeeded()) {
                            logger.debug { "redirecting to $webRoot/index.html" }
                            context.response().end(it.result())
                        } else {
                            logger.warn { "Can't find $webRoot/index.html" }
                            context.response().statusCode = 404
                            context.response()
                                .putHeader(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
                                .end("<html><body><h1>Resource not found</h1></body></html>")
                        }
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
