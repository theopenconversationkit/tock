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

package ai.tock.nlp.admin

import ai.tock.nlp.admin.AdminService.front
import ai.tock.nlp.admin.CsvCodec.newPrinter
import ai.tock.nlp.admin.model.ApplicationScopedQuery
import ai.tock.nlp.admin.model.ApplicationWithIntents
import ai.tock.nlp.admin.model.CreateEntityQuery
import ai.tock.nlp.admin.model.EntityTestErrorQueryResultReport
import ai.tock.nlp.admin.model.EntityTestErrorWithSentenceReport
import ai.tock.nlp.admin.model.IntentTestErrorQueryResultReport
import ai.tock.nlp.admin.model.IntentTestErrorWithSentenceReport
import ai.tock.nlp.admin.model.LogStatsQuery
import ai.tock.nlp.admin.model.LogsQuery
import ai.tock.nlp.admin.model.PaginatedQuery
import ai.tock.nlp.admin.model.ParseQuery
import ai.tock.nlp.admin.model.PredefinedLabelQuery
import ai.tock.nlp.admin.model.PredefinedValueQuery
import ai.tock.nlp.admin.model.SearchQuery
import ai.tock.nlp.admin.model.SentenceReport
import ai.tock.nlp.admin.model.SentencesReport
import ai.tock.nlp.admin.model.TestBuildQuery
import ai.tock.nlp.admin.model.TranslateSentencesQuery
import ai.tock.nlp.admin.model.UpdateEntityDefinitionQuery
import ai.tock.nlp.admin.model.UpdateSentencesQuery
import ai.tock.nlp.core.DictionaryData
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.PredefinedValue
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.build.ModelBuildTrigger
import ai.tock.nlp.front.shared.codec.ApplicationDump
import ai.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import ai.tock.nlp.front.shared.codec.DumpType
import ai.tock.nlp.front.shared.codec.SentencesDump
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.monitoring.UserActionLog
import ai.tock.nlp.front.shared.monitoring.UserActionLogQuery
import ai.tock.shared.BUILTIN_ENTITY_EVALUATOR_NAMESPACE
import ai.tock.shared.Executor
import ai.tock.shared.defaultNamespace
import ai.tock.shared.devEnvironment
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.name
import ai.tock.shared.namespace
import ai.tock.shared.property
import ai.tock.shared.provide
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.security.TockUserRole.admin
import ai.tock.shared.security.TockUserRole.nlpUser
import ai.tock.shared.security.TockUserRole.technicalAdmin
import ai.tock.shared.security.UNKNOWN_USER_LOGIN
import ai.tock.shared.security.auth.TockAuthProvider
import ai.tock.shared.security.initEncryptor
import ai.tock.shared.supportedLanguages
import ai.tock.shared.vertx.RequestLogger
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.makeDetailedHealthcheck
import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod.GET
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 *
 */
open class AdminVerticle : WebVerticle() {

    override val logger: KLogger = KotlinLogging.logger {}

    override val rootPath: String = "/rest/admin"

    override fun authProvider(): TockAuthProvider = defaultAuthProvider()

    fun simpleLogger(
        actionType: String,
        dataProvider: (RoutingContext) -> Any? = { null },
        applicationIdProvider: (RoutingContext, Any?) -> Id<ApplicationDefinition>? = { context, _ -> context.pathParam("applicationId")?.toId() }

    ): RequestLogger = logger<Any>(actionType, dataProvider, applicationIdProvider)

    inline fun <T> logger(
        actionType: String,
        noinline dataProvider: (RoutingContext) -> Any? = { null },
        crossinline applicationIdProvider: (RoutingContext, T?) -> Id<ApplicationDefinition>? = { context, _ -> context.pathParam("applicationId")?.toId() }
    ): RequestLogger =
        object : RequestLogger {
            override fun log(context: RoutingContext, data: Any?, error: Boolean) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val log = UserActionLog(
                        context.organization,
                        applicationIdProvider.invoke(context, data as? T),
                        context.user!!.user,
                        actionType,
                        dataProvider(context) ?: data,
                        error
                    )
                    injector.provide<Executor>().executeBlocking { FrontClient.save(log) }
                } catch (e: Exception) {
                    logger.error(e)
                }

            }
        }

    open fun configureServices() {
        val front = FrontClient
        val service = AdminService

        initEncryptor()

        //Retrieve all applications of the namespace
        blockingJsonGet("/applications") { context ->
            front.getApplications().filter {
                it.namespace == context.organization
            }.map {
                service.getApplicationWithIntents(it)
            }
        }

        //Retrieve application that matches given identifier
        blockingJsonGet("/application/:applicationId") { context ->
            service.getApplicationWithIntents(context.pathId("applicationId"))
                ?.takeIf { it.namespace == context.organization }
        }

        blockingJsonGet("/application/:applicationId/model/:engine/configuration", admin) { context ->
            front.getApplicationById(context.pathId("applicationId"))
                ?.takeIf { it.namespace == context.organization }
                ?.let { front.getCurrentModelConfiguration(it.qualifiedName, NlpEngineType(context.path("engine"))) }
        }

        blockingJsonPost(
            "/application/:applicationId/model/:engine/configuration",
            admin,
            simpleLogger("Model Configuration")
        ) { context, conf: NlpApplicationConfiguration ->
            front.getApplicationById(context.pathId("applicationId"))
                ?.takeIf { it.namespace == context.organization && it.supportedLocales.isNotEmpty() }
                ?.let { front.updateModelConfiguration(it.qualifiedName, NlpEngineType(context.path("engine")), conf) }
        }

        //Retrieve full application dump that matches given identifier
        blockingJsonGet("/application/dump/:id", technicalAdmin) {
            val id: Id<ApplicationDefinition> = it.pathId("id")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.export(id, DumpType.full)
            } else {
                unauthorized()
            }
        }

        //Retrieve sentences dump that matches given application identifier
        blockingJsonGet("/sentences/dump/:dumpType/:applicationId", admin) {
            val id: Id<ApplicationDefinition> = it.pathId("applicationId")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    id,
                    DumpType.parseDumpType(it.path("dumpType"))
                )
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/sentences/dump/:dumpType/:applicationId",
            admin
        ) { context, query: SearchQuery ->
            val id: Id<ApplicationDefinition> = context.pathId("applicationId")
            if (context.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    query.toSentencesQuery(id),
                    DumpType.parseDumpType(
                        context.path("dumpType")
                    )
                )
            } else {
                unauthorized()
            }
        }

        //Retrieve qualified sentences dump that matches given application identifier and intent
        blockingJsonGet("/sentences/dump/:dumpType/:applicationId/:intent", admin) {
            val id: Id<ApplicationDefinition> = it.pathId("applicationId")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    id,
                    DumpType.parseDumpType(it.path("dumpType")),
                    it.path("intent")
                )
            } else {
                unauthorized()
            }
        }

        //Retrieve qualified sentences dump that matches given application identifier, intent and locale
        blockingJsonGet("/sentences/dump/:dumpType/:applicationId/:intent/:locale", admin) {
            val id: Id<ApplicationDefinition> = it.pathId("applicationId")
            if (it.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    id,
                    DumpType.parseDumpType(it.path("dumpType")),
                    it.path("intent"),
                    it.pathToLocale("locale")
                )
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/sentences/dump/:dumpType/:applicationId", technicalAdmin) { context, query: SearchQuery ->
            val id: Id<ApplicationDefinition> = context.pathId("applicationId")
            if (context.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    query.toSentencesQuery(id),
                    DumpType.parseDumpType(context.path("dumpType"))
                )
            } else {
                unauthorized()
            }
        }

        //Create or update application
        blockingJsonPost(
            "/application",
            admin,
            logger<ApplicationWithIntents>("Create or Update Application") { _, app ->
                app?._id
            }
        ) { context, application: ApplicationWithIntents ->
            val existingApp = application._id?.let { front.getApplicationById(it) }
            if (context.organization == application.namespace
                && (application._id == null || context.organization == existingApp?.namespace)
            ) {
                val appWithSameName = front.getApplicationByNamespaceAndName(application.namespace, application.name)
                if (appWithSameName != null && appWithSameName._id != application._id) {
                    badRequest("Application with same name already exists")
                }
                val newApp = saveApplication(existingApp, application.toApplication().copy(name = application.name.toLowerCase()))
                //trigger a full rebuild if nlp engine change
                if (appWithSameName?.nlpEngineType != newApp.nlpEngineType) {
                    front.triggerBuild(ModelBuildTrigger(newApp._id, true))
                }
                ApplicationWithIntents(newApp, front.getIntentsByApplicationId(newApp._id))
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/application/build/trigger",
            admin,
            logger<ApplicationWithIntents>("Trigger Build") { _, app ->
                app?._id
            }) { context, application: ApplicationWithIntents ->
            val app = front.getApplicationById(application._id!!)
            if (context.organization == app!!.namespace) {
                front.triggerBuild(ModelBuildTrigger(app._id, true))
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/application/builds", nlpUser) { context, query: PaginatedQuery ->
            val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
            if (context.organization == app?.namespace) {
                front.builds(app._id, query.currentLanguage, query.start.toInt(), query.size)
            } else {
                unauthorized()
            }
        }

        //Upload a complete application model
        blockingUploadJsonPost(
            "/dump/application",
            technicalAdmin,
            logger<ApplicationDump>("Application Dump") { _, app ->
                app?.application?._id
            }) { context, dump: ApplicationDump ->
            front.import(context.organization, dump)
        }

        //Upload a complete application model [sentences dump format]
        blockingUploadJsonPost(
            "/dump/sentences",
            admin,
            logger<SentencesDump>("Sentences Dump") { context, app ->
                app?.applicationName?.let {
                    front.getApplicationByNamespaceAndName(context.organization, it)?._id
                }
            }) { context, dump: SentencesDump ->
            front.importSentences(context.organization, dump)
        }

        //Upload complete application dump and set specified name as application name
        blockingUploadJsonPost(
            "/dump/application/:name",
            admin,
            logger<ApplicationDump>("Application Dump with new Name") { _, app ->
                app?.application?._id
            }) { context, dump: ApplicationDump ->
            front.import(context.organization, dump, ApplicationImportConfiguration(context.path("name")))
        }

        //Upload complete application dump [sentences dump format] and set specified name as application name
        blockingUploadJsonPost(
            "/dump/sentences/:name",
            admin,
            logger<SentencesDump>("Sentences Dump with new Name") { context, app ->
                app?.applicationName?.let {
                    front.getApplicationByNamespaceAndName(context.organization, it)?._id
                }
            }) { context, dump: SentencesDump ->
            front.importSentences(context.organization, dump.copy(applicationName = context.path("name")))
        }

        //Delete application that matches given identifier
        blockingDelete("/application/:applicationId", admin, simpleLogger("Delete Application")) {
            val id: Id<ApplicationDefinition> = it.pathId("applicationId")
            val app = front.getApplicationById(id)
            if (it.organization == app?.namespace) {
                deleteApplication(app)
            } else {
                unauthorized()
            }
        }

        //Remove an intent from an application model. If the intent does not belong to an other model, delete the intent.
        blockingJsonDelete(
            "/application/:applicationId/intent/:intentId",
            nlpUser,
            simpleLogger(
                "Remove Intent",
                {
                    front.getApplicationById(it.pathId("applicationId"))?.run {
                        intents.find { i -> i.toString() == it.path("intentId") }
                    }
                }
            )) {
            val app = front.getApplicationById(it.pathId("applicationId"))
            val intentId: Id<IntentDefinition> = it.pathId("intentId")
            if (it.organization == app?.namespace) {
                front.removeIntentFromApplication(app, intentId)
            } else {
                unauthorized()
            }
        }

        //Remove a entity role from intent of an application model.
        blockingJsonDelete(
            "/application/:applicationId/intent/:intentId/entity/:entityType/:role",
            nlpUser,
            simpleLogger(
                "Remove Entity Role from Intent",
                {
                    front.getApplicationById(it.pathId("applicationId"))?.run {
                        Triple(
                            intents.find { i -> i.toString() == it.path("intentId") },
                            it.path("entityType"),
                            it.path("role")
                        )
                    }
                }
            )) {
            val app = front.getApplicationById(it.pathId("applicationId"))
            val intentId: Id<IntentDefinition> = it.pathId("intentId")
            val entityType = it.path("entityType")
            val role = it.path("role")
            val intent = front.getIntentById(intentId)!!
            if (intent.applications.size == 1 && it.organization == app?.namespace && it.organization == intent.namespace) {
                front.removeEntityFromIntent(app, intent, entityType, role)
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete(
            "/application/:applicationId/intent/:intentId/state/:state",
            nlpUser,
            simpleLogger(
                "Remove Mandatory State from Intent",
                {
                    front.getApplicationById(it.pathId("applicationId"))?.run {
                        Pair(
                            intents.find { i -> i.toString() == it.path("intentId") },
                            it.path("state")
                        )
                    }
                }
            )) {
            val app = front.getApplicationById(it.pathId("applicationId"))
            val intentId: Id<IntentDefinition> = it.pathId("intentId")
            val state = it.path("state")
            val intent = front.getIntentById(intentId)!!
            if (intent.applications.size == 1 && it.organization == app?.namespace && it.organization == intent.namespace) {
                front.save(intent.copy(mandatoryStates = intent.mandatoryStates - state))
                true
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete(
            "/application/:applicationId/intent/:intentId/shared/:sharedIntentId",
            nlpUser,
            simpleLogger(
                "Remove SharedIntent from Intent",
                {
                    front.getApplicationById(it.pathId("applicationId"))?.run {
                        Pair(
                            intents.find { i -> i.toString() == it.path("intentId") },
                            intents.find { i -> i.toString() == it.path("sharedIntentId") }
                        )
                    }
                }
            )) {
            val app = front.getApplicationById(it.pathId("applicationId"))
            val intentId: Id<IntentDefinition> = it.pathId("intentId")
            val sharedIntentId = it.path("sharedIntentId")
            val intent = front.getIntentById(intentId)!!
            if (intent.applications.size == 1 && it.organization == app?.namespace && it.organization == intent.namespace) {
                front.save(intent.copy(sharedIntents = intent.sharedIntents - sharedIntentId.toId()))
                true
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete(
            "/application/:applicationId/entity/:entityType/:role",
            nlpUser,
            simpleLogger(
                "Remove SubEntity from Entity",
                {
                    Pair(
                        it.path("entityType"),
                        it.path("role")
                    )
                }
            )) {
            val app = front.getApplicationById(it.pathId("applicationId"))!!
            val entityTypeName = it.path("entityType")
            val role = it.path("role")
            val entityType = front.getEntityTypeByName(entityTypeName)!!
            if (it.organization == app.namespace && it.organization == entityType.name.namespace()) {
                front.removeSubEntityFromEntity(app, entityType, role)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/locales") {
            supportedLanguages
                .map { it.key to it.value.getDisplayLanguage(Locale.ENGLISH).capitalize() }
                .sortedBy { it.second }
        }

        blockingJsonPost("/parse", nlpUser)
        { context, query: ParseQuery ->
            if (context.organization == query.namespace) {
                service.parseSentence(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/sentence",
            nlpUser,
            logger<SentenceReport>("Update Sentence") { _, s ->
                s?.applicationId
            })
        { context, sentenceReport: SentenceReport ->
            if (context.organization == front.getApplicationById(sentenceReport.applicationId)?.namespace) {
                front.save(sentenceReport.toClassifiedSentence(), context.user?.user ?: UNKNOWN_USER_LOGIN)
            } else {
                unauthorized()
            }
        }

        jsonPost("/sentences/search", nlpUser)
        { context, s: SearchQuery, handler: Handler<SentencesReport> ->
            if (context.organization == s.namespace) {
                context.isAuthorized(nlpUser) { plain ->
                    context.executeBlocking {
                        handler.handle(service.searchSentences(s, !(plain.result() ?: false)))
                    }
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/sentences/update",
            admin,
            logger<UpdateSentencesQuery>("Update Sentences") { context, q ->
                q?.applicationName?.let {
                    front.getApplicationByNamespaceAndName(context.organization, it)?._id
                }
            })
        { context, s: UpdateSentencesQuery ->
            if (context.organization == s.namespace
                && (s.searchQuery == null || context.organization == s.searchQuery.namespace)
            ) {
                service.updateSentences(s)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/users/logs/search", technicalAdmin)
        { context, s: UserActionLogQuery ->
            if (context.organization == s.namespace) {
                front.search(s)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/logs/search", nlpUser)
        { context, s: LogsQuery ->
            if (context.organization == s.namespace) {
                service.searchLogs(s)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/logs/:applicationId/:locale/export", nlpUser)
        { context ->
            val app = front.getApplicationById(context.pathId("applicationId"))!!
            if (context.organization == app.namespace) {
                val sb = StringBuilder()
                val p = newPrinter(sb)

                front.export(app._id, context.pathToLocale("locale"))
                    .forEach {
                        p.printRecord(it.date, it.intent, it.text)
                    }
                sb
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/logs/stats", nlpUser)
        { context, s: LogStatsQuery ->
            if (context.organization == s.namespace) {
                front.stats(s.toStatQuery(front.getApplicationByNamespaceAndName(s.namespace, s.applicationName)!!))
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/logs/intent/stats", nlpUser)
        { context, s: LogStatsQuery ->
            if (context.organization == s.namespace) {
                front.intentStats(
                    s.toStatQuery(
                        front.getApplicationByNamespaceAndName(
                            s.namespace,
                            s.applicationName
                        )!!
                    )
                )
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/intent", nlpUser, simpleLogger("Create or Update Intent"))
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
            front.getEntityTypes().filter {
                it.name.namespace() == context.organization || it.name.namespace() == BUILTIN_ENTITY_EVALUATOR_NAMESPACE
            }
        }

        blockingJsonPost(
            "/entity",
            nlpUser,
            logger<UpdateEntityDefinitionQuery>("Update Entity") { context, q ->
                q?.applicationName?.let {
                    front.getApplicationByNamespaceAndName(context.organization, it)?._id
                }
            })
        { context, query: UpdateEntityDefinitionQuery ->
            if (context.organization == query.namespace) {
                front.updateEntityDefinition(query.namespace, query.applicationName, query.entity)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/dictionary/:qualifiedName") { context ->
            context.path("qualifiedName").takeUnless { it.namespace() != context.organization }?.let {
                front.getDictionaryDataByEntityName(it) ?: DictionaryData(it.namespace(), it.name())
            } ?: unauthorized()
        }

        blockingJsonPost("/dictionary", nlpUser, simpleLogger("Update Dictionary"))
        { context, dictionary: DictionaryData ->
            if (context.organization == dictionary.namespace) {
                front.save(dictionary)
                front.getEntityTypeByName(dictionary.qualifiedName)?.let {
                    front.save(it.copy(dictionary = dictionary.values.isNotEmpty()))
                }
            } else {
                unauthorized()
            }
        }

        blockingUploadJsonPost("/dump/dictionary/:entityName", admin, simpleLogger("Update Dictionary")) { context, dump: DictionaryData ->
            val data = dump.copy(namespace = context.organization, entityName = context.path("entityName"))
            front.save(data)
            data
        }

        blockingJsonGet("/nlp-engines")
        { front.getSupportedNlpEngineTypes() }

        blockingJsonPost<CreateEntityQuery, EntityTypeDefinition?>("/entity-type/create", nlpUser, simpleLogger("Create Entity"))
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

        blockingJsonPost("/entity-type", nlpUser, simpleLogger("Update Entity"))
        { context, entityType: EntityTypeDefinition ->
            if (context.organization == entityType.name.namespace()) {
                val update = front.getEntityTypeByName(entityType.name)
                    ?.run {
                        copy(
                            description = entityType.description,
                            subEntities = entityType.subEntities,
                            dictionary = entityType.dictionary
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

        blockingJsonDelete("/entity-type/:name", nlpUser, simpleLogger("Delete Entity", { it.path("name") to true }))
        {
            val entityType = it.path("name")
            if (it.organization == entityType.namespace()) {
                front.deleteEntityTypeByName(entityType)
            } else {
                unauthorized()
            }
        }

        jsonPost("/test/intent-errors", nlpUser)
        { context, query: TestBuildQuery, handler: Handler<IntentTestErrorQueryResultReport> ->
            if (context.organization == query.namespace) {
                context.isAuthorized(technicalAdmin) { plain ->
                    context.executeBlocking {
                        val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
                        handler.handle(AdminService.searchTestIntentErrors(query.toTestErrorQuery(app), !(plain.result()
                            ?: false)))
                    }
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/test/intent-error/delete",
            nlpUser,
            logger<IntentTestErrorWithSentenceReport>("Delete Intent Test Error") { _, e -> e?.sentence?.applicationId })
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

        jsonPost("/test/entity-errors", nlpUser)
        { context, query: TestBuildQuery, handler: Handler<EntityTestErrorQueryResultReport> ->
            if (context.organization == query.namespace) {
                context.isAuthorized(technicalAdmin) { plain ->
                    context.executeBlocking {
                        val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
                        handler.handle(service.searchTestEntityErrors(query.toTestErrorQuery(app), !(plain.result()
                            ?: false)))
                    }
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/test/entity-error/delete",
            nlpUser,
            logger<EntityTestErrorWithSentenceReport>("Delete Entity Test Error") { _, e -> e?.sentence?.applicationId }
        )
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

        blockingJsonPost("/test/stats", nlpUser)
        { context, query: TestBuildQuery ->
            val app = front.getApplicationByNamespaceAndName(
                query.namespace,
                query.applicationName
            )
            if (context.organization == app?.namespace
            ) {
                AdminService.testBuildStats(query, app)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/alexa/export", admin)
        { context, query: ApplicationScopedQuery ->
            val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
            if (app != null && context.organization == app.namespace) {
                front.exportIntentsSchema(app.name, app._id, query.currentLanguage)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/dictionary/predefined-values", nlpUser, simpleLogger("Update Predefined Value"))
        { context, query: PredefinedValueQuery ->

            front.getDictionaryDataByEntityName(query.entityTypeName)
                ?.takeIf { it.namespace == context.organization }
                ?.run {
                    val value = query.oldPredefinedValue ?: query.predefinedValue
                    copy(values = values.filter { it.value != value } +
                        (values.find { it.value == value }
                            ?.copy(value = query.predefinedValue)
                            ?: PredefinedValue(
                                query.predefinedValue,
                                mapOf(query.locale to listOf(query.predefinedValue))
                            )
                            )
                    )
                }
                ?.also {
                    front.save(it)
                }
                ?: unauthorized()
        }

        blockingDelete(
            "/dictionary/predefined-values/:entityType/:value",
            nlpUser,
            simpleLogger("Delete Predefined Value", { it.path("entityType") to it.path("value") }))
        { context ->
            val entityType = context.path("entityType")
            if (context.organization == entityType.namespace()) {
                front.deletePredefinedValueByName(entityType, context.path("value"))
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/dictionary/predefined-value/labels", nlpUser, simpleLogger("Update Predefined Labels"))
        { context, query: PredefinedLabelQuery ->

            front.getDictionaryDataByEntityName(query.entityTypeName)
                ?.takeIf { it.namespace == context.organization }
                ?.run {
                    copy(values = values.filter { it.value != query.predefinedValue } +
                        (values.find { it.value == query.predefinedValue }
                            ?.run {
                                copy(labels = labels.filter { it.key != query.locale } +
                                    mapOf(query.locale to ((labels[query.locale]?.filter { it != query.label }
                                        ?: emptyList()) + listOf(query.label)).sorted())
                                )
                            }
                            ?: PredefinedValue(
                                query.predefinedValue,
                                mapOf(query.locale to listOf(query.label))
                            ))
                    )
                }
                ?.also {
                    front.save(it)
                }
                ?: unauthorized()

        }

        blockingDelete(
            "/dictionary/predefined-value/labels/:entityType/:value/:locale/:label",
            nlpUser,
            simpleLogger(
                "Delete Predefined Label",
                {
                    listOf(
                        it.path("entityType"),
                        it.path("value"),
                        it.path("locale"),
                        it.path("label")
                    ).toTypedArray()
                }
            )
        )
        { context ->
            val entityType = context.path("entityType")
            if (context.organization == entityType.namespace()) {
                front.deletePredefinedValueLabelByName(
                    entityType,
                    context.path("value"),
                    context.pathToLocale("locale"),
                    context.path("label")
                )
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/translation/sentence",
            admin,
            logger<TranslateSentencesQuery>("Translate Sentences") { context, s ->
                s?.applicationName?.let {
                    front.getApplicationByNamespaceAndName(context.organization, it)?._id
                }
            })
        { context, s: TranslateSentencesQuery ->
            if (context.organization == s.namespace
                && (s.searchQuery == null || context.organization == s.searchQuery.namespace)
            ) {
                service.translateSentences(s)
            } else {
                unauthorized()
            }
        }

        if (devEnvironment) {
            router.get("/rest/user").handler {
                it.response().end(
                    mapper.writeValueAsString(
                        TockUser(
                            property("tock_user", "admin@app.com"),
                            defaultNamespace,
                            TockUserRole.values().map { r -> r.name }.toSet()
                        )
                    )
                )
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
            router.get("/doc/admin.yaml").handler { context ->
                context.vertx().fileSystem().readFile("$webRoot/doc/admin.yaml") {
                    if (it.succeeded()) {
                        context.response().end(it.result().toString(StandardCharsets.UTF_8))
                    } else {
                        context.fail(it.cause())
                    }
                }
            }
            router.route(GET, "/")
                .handler(
                    StaticHandler.create()
                        .setAllowRootFileSystemAccess(true).setCachingEnabled(false).setWebRoot(webRoot)
                )
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

    protected open fun deleteApplication(app: ApplicationDefinition) {
        front.deleteApplicationById(app._id)
    }

    protected open fun saveApplication(existingApp: ApplicationDefinition?, app: ApplicationDefinition): ApplicationDefinition {
        return front.save(app)
    }

    override fun configure() {
        configureServices()
        configureStaticHandling()
    }

    override fun defaultHealthcheck(): (RoutingContext) -> Unit {
        return { it.response().end() }
    }

    override fun detailedHealthcheck(): (RoutingContext) -> Unit = makeDetailedHealthcheck()

}