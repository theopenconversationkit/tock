/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.nlp.admin

import ai.tock.nlp.admin.AdminService.front
import ai.tock.nlp.admin.CsvCodec.newPrinter
import ai.tock.nlp.admin.model.*
import ai.tock.shared.exception.admin.AdminException
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
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.monitoring.UserActionLog
import ai.tock.nlp.front.shared.monitoring.UserActionLogQuery
import ai.tock.nlp.front.shared.namespace.NamespaceConfiguration
import ai.tock.nlp.front.shared.user.UserNamespace
import ai.tock.shared.*
import ai.tock.shared.security.NoEncryptionPassException
import ai.tock.shared.security.TockUserRole.*
import ai.tock.shared.security.UNKNOWN_USER_LOGIN
import ai.tock.shared.security.auth.TockAuthProvider
import ai.tock.shared.security.decrypt
import ai.tock.shared.security.initEncryptor
import ai.tock.shared.vertx.*
import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod.GET
import io.vertx.ext.web.FileUpload
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

/**
 *
 */
open class AdminVerticle : WebVerticle<AdminException>() {

    override val logger: KLogger = KotlinLogging.logger {}

    private val baseHref: String = verticleProperty("base_href", "/")
        .run {
            takeIf { endsWith("/") } ?: "$this/"
        }

    override val basePath: String = "${baseHref}rest"
    override val rootPath: String = "$basePath/admin"

    override fun authProvider(): TockAuthProvider<AdminException> = defaultAuthProvider()

    /**
     * Is creating namespace is supported ?
     */
    protected open val supportCreateNamespace: Boolean = true

    fun simpleLogger(
        actionType: String,
        dataProvider: (RoutingContext) -> Any? = { null },
        applicationIdProvider: (RoutingContext, Any?) -> Id<ApplicationDefinition>? = { context, _ ->
            context.pathParam(
                "applicationId"
            )?.toId()
        }

    ): RequestLogger = logger<Any>(actionType, dataProvider, applicationIdProvider)

    inline fun <T> logger(
        actionType: String,
        noinline dataProvider: (RoutingContext) -> Any? = { null },
        crossinline applicationIdProvider: (RoutingContext, T?) -> Id<ApplicationDefinition>? = { context, _ ->
            context.pathParam(
                "applicationId"
            )?.toId()
        }
    ): RequestLogger =
        object : RequestLogger {
            override fun log(context: RoutingContext, data: Any?, error: Boolean) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val log = UserActionLog(
                        context.organization,
                        applicationIdProvider.invoke(context, data as? T),
                        context.userLogin,
                        actionType,
                        (dataProvider(context) ?: data)?.takeUnless { it is FileUpload },
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

        // Retrieve all applications of the namespace
        blockingJsonGet("/applications", handler = toRequestHandler { context ->
            front.getApplications().filter {
                it.namespace == context.organization
            }.map {
                service.getApplicationWithIntents(it)
            }
        })

        // Retrieve application that matches given identifier
        blockingJsonGet("/application/:applicationId", handler = toRequestHandler { context ->
            service.getApplicationWithIntents(context.pathId("applicationId"))
                ?.takeIf { it.namespace == context.organization }
        })

        blockingJsonGet(
            "/application/:applicationId/model/:engine/configuration",
            admin,
            handler = toRequestHandler { context ->
                front.getApplicationById(context.pathId("applicationId"))
                    ?.takeIf { it.namespace == context.organization }
                    ?.let {
                        front.getCurrentModelConfiguration(
                            it.qualifiedName,
                            NlpEngineType(context.path("engine"))
                        )
                    }
            })

        blockingJsonPost(
            "/application/:applicationId/model/:engine/configuration",
            admin,
            simpleLogger("Model Configuration"), handler = toRequestHandler { context, conf: NlpApplicationConfiguration ->
                front.getApplicationById(context.pathId("applicationId"))
                    ?.takeIf { it.namespace == context.organization && it.supportedLocales.isNotEmpty() }
                    ?.let {
                        front.updateModelConfiguration(
                            it.qualifiedName,
                            NlpEngineType(context.path("engine")),
                            conf
                        )
                    }
            })

        // Retrieve full application dump that matches given identifier
        blockingJsonGet("/application/dump/:id", technicalAdmin, handler =  toRequestHandler { context ->
            val id: Id<ApplicationDefinition> = context.pathId("id")
            if (context.organization == front.getApplicationById(id)?.namespace) {
                front.export(id, DumpType.full)
            } else {
                unauthorized()
            }
        })

        // Retrieve sentences dump that matches given application identifier
        blockingJsonGet("/sentences/dump/:dumpType/:applicationId", admin, handler = toRequestHandler { context ->
            val id: Id<ApplicationDefinition> = context.pathId("applicationId")
            if (context.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    id,
                    DumpType.parseDumpType(context.path("dumpType"))
                )
            } else {
                unauthorized()
            }
        })

        blockingJsonPost(
            "/sentences/dump/:dumpType/:applicationId",
            setOf(admin, technicalAdmin), handler = toRequestHandler { context, query: SearchQuery ->
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
            })

        blockingJsonPost(
            "/sentences/dump/:dumpType/:applicationId/fromText",
            admin, handler = toRequestHandler { context, query: SentencesTextQuery ->
                val id: Id<ApplicationDefinition> = context.pathId("applicationId")
                if (context.organization == front.getApplicationById(id)?.namespace) {
                    if (query.texts.isNotEmpty())
                        front.exportSentences(
                            query.toSentencesQueries(id),
                            DumpType.parseDumpType(
                                context.path("dumpType")
                            )
                        )
                    else null
                } else {
                    unauthorized()
                }
            })

        // Retrieve qualified sentences dum<p that matches given application identifier and intent
        blockingJsonGet("/sentences/dump/:dumpType/:applicationId/:intent", admin, handler = toRequestHandler { context ->
            val id: Id<ApplicationDefinition> = context.pathId("applicationId")
            if (context.organization == front.getApplicationById(id)?.namespace) {
                front.exportSentences(
                    id,
                    DumpType.parseDumpType(context.path("dumpType")),
                    context.path("intent")
                )
            } else {
                unauthorized()
            }
        })

        // Retrieve qualified sentences dump that matches given application identifier, intent and locale
        blockingJsonGet(
            "/sentences/dump/:dumpType/:applicationId/:intent/:locale",
            admin,
            handler = toRequestHandler { context ->
                val id: Id<ApplicationDefinition> = context.pathId("applicationId")
                if (context.organization == front.getApplicationById(id)?.namespace) {
                    front.exportSentences(
                        id,
                        DumpType.parseDumpType(context.path("dumpType")),
                        context.path("intent"),
                        context.pathToLocale("locale")
                    )
                } else {
                    unauthorized()
                }
            })

        // Create or update application
        blockingJsonPost(
            "/application",
            admin,
            logger<ApplicationWithIntents>("Create or Update Application") { _, app ->
                app?._id
            }, handler = toRequestHandler { context, application: ApplicationWithIntents ->
                val existingApp = application._id?.let { front.getApplicationById(it) }
                if (context.organization == application.namespace &&
                    (application._id == null || context.organization == existingApp?.namespace)
                ) {
                    val appWithSameName =
                        front.getApplicationByNamespaceAndName(application.namespace, application.name)
                    if (appWithSameName != null && appWithSameName._id != application._id) {
                        badRequest("Application with same name already exists")
                    }
                    if (existingApp != null && existingApp.name != application.name) {
                        badRequest("Application name cannot be changed")
                    }
                    val newApp = saveApplication(
                        existingApp,
                        application.toApplication().copy(name = application.name.lowercase())
                    )
                    // trigger a full rebuild if nlp engine change
                    if (appWithSameName?.nlpEngineType != newApp.nlpEngineType
                        || appWithSameName.normalizeText != newApp.normalizeText
                    ) {
                        front.triggerBuild(ModelBuildTrigger(newApp._id, true))
                    }
                    ApplicationWithIntents(
                        newApp,
                        front.getIntentsByApplicationId(newApp._id),
                        front.getModelSharedIntents(application.namespace),
                    )
                } else {
                    unauthorized()
                }
            })

        blockingJsonGet(
            "/sentence/users/:applicationId",
            setOf(nlpUser, faqNlpUser), handler = toRequestHandler { context ->
                val id: Id<ApplicationDefinition> = context.pathId("applicationId")
                if (context.organization == front.getApplicationById(id)?.namespace) {
                    front.users(id)
                } else {
                    unauthorized()
                }
            })


        blockingJsonGet(
            "/sentence/configurations/:applicationId",
            setOf(nlpUser, faqNlpUser), handler = toRequestHandler { context ->
                val id: Id<ApplicationDefinition> = context.pathId("applicationId")
                if (context.organization == front.getApplicationById(id)?.namespace) {
                    front.configurations(id)
                } else {
                    unauthorized()
                }
            })

        blockingJsonPost(
            "/application/build/trigger",
            admin,
            logger<ApplicationWithIntents>("Trigger Build") { _, app ->
                app?._id
            }, handler = toRequestHandler { context, application: ApplicationWithIntents ->
                val app = front.getApplicationById(application._id!!)
                if (context.organization == app!!.namespace) {
                    front.triggerBuild(ModelBuildTrigger(app._id, true))
                } else {
                    unauthorized()
                }
            })

        blockingJsonPost("/application/builds", nlpUser, handler = toRequestHandler { context, query: PaginatedQuery ->
            val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
            if (context.organization == app?.namespace) {
                front.builds(app._id, query.currentLanguage, query.start.toInt(), query.size)
            } else {
                unauthorized()
            }
        })

        // Upload a complete application model
        blockingUploadJsonPost(
            "/dump/application",
            technicalAdmin,
            logger<ApplicationDump>("Application Dump") { _, app ->
                app?.application?._id
            }, handler = toRequestHandler { context, dump: ApplicationDump ->
                front.import(context.organization, dump)
            })

        // Upload a complete application model [sentences dump format]
        blockingUploadJsonPost(
            "/dump/sentences",
            admin,
            logger<SentencesDump>("Sentences Dump") { context, app ->
                app?.applicationName?.let {
                    front.getApplicationByNamespaceAndName(context.organization, it)?._id
                }
            }, handler = toRequestHandler { context, dump: SentencesDump ->
                front.importSentences(context.organization, dump)
            })

        // Upload complete application dump and set specified name as application name
        blockingUploadJsonPost(
            "/dump/application/:name",
            admin,
            logger<ApplicationDump>("Application Dump with new Name") { _, app ->
                app?.application?._id
            }, handler = toRequestHandler { context, dump: ApplicationDump ->
                front.import(context.organization, dump, ApplicationImportConfiguration(context.path("name")))
            })

        // Upload complete application dump [sentences dump format] and set specified name as application name
        blockingUploadJsonPost(
            "/dump/sentences/:name",
            admin,
            logger<SentencesDump>("Sentences Dump with new Name") { context, app ->
                app?.applicationName?.let {
                    front.getApplicationByNamespaceAndName(context.organization, it)?._id
                }
            }, handler = toRequestHandler { context, dump: SentencesDump ->
                front.importSentences(context.organization, dump.copy(applicationName = context.path("name")))
            })

        // Delete application that matches given identifier
        blockingDelete(
            "/application/:applicationId",
            admin,
            simpleLogger("Delete Application"),
            handler = toRequestHandler { context ->
                val id: Id<ApplicationDefinition> = context.pathId("applicationId")
                val app = front.getApplicationById(id)
                if (context.organization == app?.namespace) {
                    deleteApplication(app)
                } else {
                    unauthorized()
                }
            })

        // Remove an intent from an application model. If the intent does not belong to an other model, delete the intent.
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
            ), handler = toRequestHandler { context ->
                val app = front.getApplicationById(context.pathId("applicationId"))
                val intentId: Id<IntentDefinition> = context.pathId("intentId")
                if (context.organization == app?.namespace) {
                    front.removeIntentFromApplication(app, intentId)
                } else {
                    unauthorized()
                }
            })

        // Remove a entity role from intent of an application model.
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
            ), handler = toRequestHandler { context ->
                val app = front.getApplicationById(context.pathId("applicationId"))
                val intentId: Id<IntentDefinition> = context.pathId("intentId")
                val entityType = context.path("entityType")
                val role = context.path("role")
                val intent = front.getIntentById(intentId)!!
                if (intent.applications.size == 1 && context.organization == app?.namespace && context.organization == intent.namespace) {
                    front.removeEntityFromIntent(app, intent, entityType, role)
                } else {
                    unauthorized()
                }
            })

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
            ), handler = toRequestHandler { context ->
                val app = front.getApplicationById(context.pathId("applicationId"))
                val intentId: Id<IntentDefinition> = context.pathId("intentId")
                val state = context.path("state")
                val intent = front.getIntentById(intentId)!!
                if (intent.applications.size == 1 && context.organization == app?.namespace && context.organization == intent.namespace) {
                    front.save(intent.copy(mandatoryStates = intent.mandatoryStates - state))
                    true
                } else {
                    unauthorized()
                }
            })

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
            ), handler = toRequestHandler { context ->
                val app = front.getApplicationById(context.pathId("applicationId"))
                val intentId: Id<IntentDefinition> = context.pathId("intentId")
                val sharedIntentId = context.path("sharedIntentId")
                val intent = front.getIntentById(intentId)!!
                if (intent.applications.size == 1 && context.organization == app?.namespace && context.organization == intent.namespace) {
                    front.save(intent.copy(sharedIntents = intent.sharedIntents - sharedIntentId.toId()))
                    true
                } else {
                    unauthorized()
                }
            })

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
            ), handler = toRequestHandler { context ->
                val app = front.getApplicationById(context.pathId("applicationId"))!!
                val entityTypeName = context.path("entityType")
                val role = context.path("role")
                val entityType = front.getEntityTypeByName(entityTypeName)!!
                if (context.organization == app.namespace && context.organization == entityType.name.namespace()) {
                    front.removeSubEntityFromEntity(app, entityType, role)
                } else {
                    unauthorized()
                }
            })

        blockingJsonGet("/locales", handler = toRequestHandler { _ ->
            supportedLanguages
                .map { it.key to it.value.getDisplayLanguage(Locale.ENGLISH).capitalize() }
                .sortedBy { it.second }
        })

        blockingJsonPost("/parse", setOf(nlpUser, faqNlpUser), handler = toRequestHandler { context, query: ParseQuery ->
            if (context.organization == query.namespace) {
                service.parseSentence(query)
            } else {
                unauthorized()
            }
        })

        blockingJsonPost(
            "/sentence",
            setOf(nlpUser, faqNlpUser),
            logger<SentenceReport>("Update Sentence") { _, s ->
                s?.applicationId
            }, handler = toRequestHandler { context, sentenceReport: SentenceReport ->
                if (context.organization == front.getApplicationById(sentenceReport.applicationId)?.namespace) {
                    front.save(sentenceReport.toClassifiedSentence(), context.user?.user ?: UNKNOWN_USER_LOGIN)
                } else {
                    unauthorized()
                }
            })

        blockingJsonPost(
            "/sentences/search",
            setOf(faqNlpUser, nlpUser),
            handler = toRequestHandler { context, s: SearchQuery ->
                if (context.organization == s.namespace) {
                    try {
                        service.searchSentences(s)
                    } catch (t: NoEncryptionPassException) {
                        logger.error(t)
                        badRequest("Error obfuscating sentences: ${t.message}")
                    } catch (t: Exception) {
                        logger.error(t)
                        badRequest("Error searching sentences: ${t.message}")
                    }
                } else {
                    unauthorized()
                }
            })

        blockingJsonPost("/sentence/reveal", admin, handler = toRequestHandler { context, s: SentenceReport ->
            val key = s.key
            if (key == null) {
                unauthorized()
            } else {
                val decrypt = decrypt(key)
                val applicationDefinition = front.getApplicationById(s.applicationId)
                if (applicationDefinition?.namespace == context.organization &&
                    front.search(
                        SentencesQuery(
                            applicationId = applicationDefinition._id,
                            language = s.language,
                            search = decrypt,
                            onlyExactMatch = true
                        )
                    ).total != 0L
                ) {
                    s.copy(text = decrypt, key = null)
                } else {
                    unauthorized()
                }
            }
        })

        blockingJsonPost(
            "/sentences/update",
            admin,
            logger<UpdateSentencesQuery>("Update Sentences") { context, q ->
                q?.applicationName?.let {
                    front.getApplicationByNamespaceAndName(context.organization, it)?._id
                }
            }, handler = toRequestHandler { context, s: UpdateSentencesQuery ->
                if (context.organization == s.namespace &&
                    (s.searchQuery == null || context.organization == s.searchQuery.namespace)
                ) {
                    service.updateSentences(s)
                } else {
                    unauthorized()
                }
            })

        blockingJsonPost("/users/logs/search", technicalAdmin, handler = toRequestHandler { context, s: UserActionLogQuery ->
            if (context.organization == s.namespace) {
                front.search(s)
            } else {
                unauthorized()
            }
        })

        blockingJsonPost("/logs/search", nlpUser, handler = toRequestHandler { context, s: LogsQuery ->
            if (context.organization == s.namespace) {
                service.searchLogs(s)
            } else {
                unauthorized()
            }
        })

        blockingJsonGet("/logs/:applicationId/:locale/export", nlpUser, handler = toRequestHandler { context ->
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
        })

        blockingJsonPost("/logs/stats", nlpUser, handler = toRequestHandler { context, s: LogStatsQuery ->
            if (context.organization == s.namespace) {
                front.stats(s.toStatQuery(front.getApplicationByNamespaceAndName(s.namespace, s.applicationName)!!))
            } else {
                unauthorized()
            }
        })

        blockingJsonPost("/logs/intent/stats", nlpUser, handler = toRequestHandler { context, s: LogStatsQuery ->
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
        })

        blockingJsonPost(
            "/intent",
            nlpUser,
            simpleLogger("Create or Update Intent"), handler = toRequestHandler { context, intent: IntentDefinition ->
                AdminService.createOrUpdateIntent(context.organization, intent) ?: unauthorized()
            })

        blockingJsonGet("/intents", handler = toRequestHandler { context ->
            front.getApplications().filter {
                it.namespace == context.organization
            }.map {
                service.getApplicationWithIntents(it)
            }
        })

        blockingJsonGet("/entity-types", handler = toRequestHandler { context ->
            front.getEntityTypesByNamespaceAndSharedEntityTypes(context.organization)
        })

        blockingJsonPost(
            "/entity",
            nlpUser,
            logger<UpdateEntityDefinitionQuery>("Update Entity") { context, q ->
                q?.applicationName?.let {
                    front.getApplicationByNamespaceAndName(context.organization, it)?._id
                }
            }, handler = toRequestHandler { context, query: UpdateEntityDefinitionQuery ->
                if (context.organization == query.namespace) {
                    front.updateEntityDefinition(query.namespace, query.applicationName, query.entity)
                } else {
                    unauthorized()
                }
            })

        blockingJsonGet("/dictionary/:qualifiedName", handler = toRequestHandler { context ->
            context.path("qualifiedName").takeUnless { it.namespace() != context.organization }?.let {
                front.getDictionaryDataByEntityName(it) ?: DictionaryData(it.namespace(), it.name())
            } ?: unauthorized()
        })

        blockingJsonPost(
            "/dictionary",
            nlpUser,
            simpleLogger("Update Dictionary"), handler = toRequestHandler { context, dictionary: DictionaryData ->
                if (context.organization == dictionary.namespace) {
                    front.save(dictionary)
                    front.getEntityTypeByName(dictionary.qualifiedName)?.let {
                        front.save(it.copy(dictionary = dictionary.values.isNotEmpty()))
                    }
                } else {
                    unauthorized()
                }
            })

        blockingUploadJsonPost(
            "/dump/dictionary/:entityName",
            admin,
            simpleLogger("Update Dictionary")
            , handler = toRequestHandler { context, dump: DictionaryData ->
            val data = dump.copy(namespace = context.organization, entityName = context.path("entityName"))
            front.save(data)
            data
        })

        blockingJsonGet("/nlp-engines") { RequestSucceeded(front.getSupportedNlpEngineTypes()) }

        blockingJsonPost<CreateEntityQuery, EntityTypeDefinition?>(
            "/entity-type/create",
            nlpUser,
            simpleLogger("Create Entity")
            , handler = toRequestHandler { context, query ->
            val entityName = "${context.organization}:${query.type.lowercase().name()}"
            if (front.getEntityTypeByName(entityName) == null) {
                val entityType = EntityTypeDefinition(entityName, "")
                front.save(entityType)
                entityType
            } else {
                null
            }
        })

        blockingJsonPost(
            "/entity-type",
            nlpUser,
            simpleLogger("Update Entity")
            , handler = toRequestHandler { context, entityType: EntityTypeDefinition ->
            if (context.organization == entityType.name.namespace()) {
                val update = front.getEntityTypeByName(entityType.name)
                    ?.run {
                        copy(
                            description = entityType.description,
                            subEntities = entityType.subEntities,
                            dictionary = entityType.dictionary,
                            obfuscated = entityType.obfuscated
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
        })

        blockingJsonDelete("/entity-type/:name", nlpUser, simpleLogger("Delete Entity", { it.path("name") to true }),
            handler = toRequestHandler { context ->
            val entityType = context.path("name")
            if (context.organization == entityType.namespace()) {
                front.deleteEntityTypeByName(entityType)
            } else {
                unauthorized()
            }
        })

        blockingJsonPost("/test/intent-errors", setOf(nlpUser, faqNlpUser), handler = toRequestHandler { context, query: TestBuildQuery ->
            if (context.organization == query.namespace) {
                val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
                    ?: error("application for $query not found")
                AdminService.searchTestIntentErrors(query.toTestErrorQuery(app))
            } else {
                unauthorized()
            }
        })

        blockingJsonPost(
            "/test/intent-error/delete",
            setOf(nlpUser, faqNlpUser),
            logger<IntentTestErrorWithSentenceReport>("Delete Intent Test Error") { _, e -> e?.sentence?.applicationId }
            , handler = toRequestHandler { context, error: IntentTestErrorWithSentenceReport ->
            if (context.organization == front.getApplicationById(error.sentence.applicationId)?.namespace) {
                front.deleteTestIntentError(
                    error.sentence.applicationId,
                    error.sentence.language,
                    error.sentence.toClassifiedSentence().text
                )
            } else {
                unauthorized()
            }
        })

        blockingJsonPost("/test/entity-errors", setOf(nlpUser, faqNlpUser), handler = toRequestHandler { context, query: TestBuildQuery ->
            if (context.organization == query.namespace) {
                val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
                    ?: error("application for $query not found")
                service.searchTestEntityErrors(query.toTestErrorQuery(app))
            } else {
                unauthorized()
            }
        })

        blockingJsonPost(
            "/test/entity-error/delete",
            setOf(nlpUser, faqNlpUser),
            logger<EntityTestErrorWithSentenceReport>("Delete Entity Test Error") { _, e -> e?.sentence?.applicationId }
            , handler = toRequestHandler { context, error: EntityTestErrorWithSentenceReport ->
            if (context.organization == front.getApplicationById(error.sentence.applicationId)?.namespace) {
                front.deleteTestEntityError(
                    error.sentence.applicationId,
                    error.sentence.language,
                    error.originalSentence.toClassifiedSentence().text
                )
            } else {
                unauthorized()
            }
        })

        blockingJsonPost("/test/stats", setOf(nlpUser, faqNlpUser), handler = toRequestHandler { context, query: TestBuildQuery ->
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
        })

        blockingJsonPost("/alexa/export", admin, handler = toRequestHandler { context, query: ApplicationScopedQuery ->
            val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
            if (app != null && context.organization == app.namespace) {
                front.exportIntentsSchema(app.name, app._id, query.currentLanguage)
            } else {
                unauthorized()
            }
        })

        blockingJsonPost(
            "/dictionary/predefined-values",
            nlpUser,
            simpleLogger("Update Predefined Value")
            , handler = toRequestHandler { context, query: PredefinedValueQuery ->

            front.getDictionaryDataByEntityName(query.entityTypeName)
                ?.takeIf { it.namespace == context.organization }
                ?.run {
                    val value = query.oldPredefinedValue ?: query.predefinedValue
                    copy(
                        values = values.filter { it.value != value } +
                                (
                                        values.find { it.value == value }
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
        })

        blockingDelete(
            "/dictionary/predefined-values/:entityType/:value",
            nlpUser,
            simpleLogger("Delete Predefined Value", { it.path("entityType") to it.path("value") })
            , handler = toRequestHandler { context ->
            val entityType = context.path("entityType")
            if (context.organization == entityType.namespace()) {
                front.deletePredefinedValueByName(entityType, context.path("value"))
            } else {
                unauthorized()
            }
        })

        blockingJsonPost(
            "/dictionary/predefined-value/labels",
            nlpUser,
            simpleLogger("Update Predefined Labels")
            , handler = toRequestHandler { context, query: PredefinedLabelQuery ->

            front.getDictionaryDataByEntityName(query.entityTypeName)
                ?.takeIf { it.namespace == context.organization }
                ?.run {
                    copy(
                        values = values.filter { it.value != query.predefinedValue } +
                                (
                                        values.find { it.value == query.predefinedValue }
                                            ?.run {
                                                copy(
                                                    labels = labels.filter { it.key != query.locale } +
                                                            mapOf(
                                                                query.locale to (
                                                                        (
                                                                                labels[query.locale]?.filter { it != query.label }
                                                                                    ?: emptyList()
                                                                                ) + listOf(query.label)
                                                                        ).sorted()
                                                            )
                                                )
                                            }
                                            ?: PredefinedValue(
                                                query.predefinedValue,
                                                mapOf(query.locale to listOf(query.label))
                                            )
                                        )
                    )
                }
                ?.also {
                    front.save(it)
                }
                ?: unauthorized()
        })

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
            , handler = toRequestHandler { context ->
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
        })

        blockingJsonPost(
            "/translation/sentence",
            admin,
            logger<TranslateSentencesQuery>("Translate Sentences") { context, s ->
                s?.applicationName?.let {
                    front.getApplicationByNamespaceAndName(context.organization, it)?._id
                }
            }
            , handler = toRequestHandler { context, s: TranslateSentencesQuery ->
            if (context.organization == s.namespace &&
                (s.searchQuery == null || context.organization == s.searchQuery.namespace)
            ) {
                service.translateSentences(s)
            } else {
                unauthorized()
            }
        })

        blockingJsonGet("/namespaces") {
            RequestSucceeded(front.getNamespaces(it.userLogin))
        }

        blockingJsonGet("/namespaces/:namespace") { context ->
            val n = context.path("namespace")
            if (front.isNamespaceOwner(context.userLogin, n)) {
                RequestSucceeded(front.getUsers(n))
            } else {
                unauthorized()
            }
        }

        blockingPost(
            "/namespace/:namespace",
            admin,
            simpleLogger("Create Namespace")
            , handler = toRequestHandler { context ->
            val n = context.path("namespace").trim()
            // get the namespace of the current user
            if (supportCreateNamespace) {
                if (front.isExistingNamespace(n)) {
                    badRequest("Namespace already exists")
                } else {
                    front.saveNamespace(UserNamespace(context.userLogin, n, true))
                }
            } else {
                unauthorized()
            }
        })

        blockingJsonPost(
            "/namespace",
            admin,
            simpleLogger("Add or Update Namespace for User")
            , handler = toRequestHandler { context, namespace: UserNamespace ->
            // get the namespace of the current user
            if (front.isNamespaceOwner(context.userLogin, namespace.namespace)) {
                front.saveNamespace(namespace)
            } else {
                unauthorized()
            }
        })

        blockingPost(
            "/namespace/select/:namespace"
            , handler = toRequestHandler { context ->
            val n = context.path("namespace").trim()
            if (front.hasNamespace(context.userLogin, n)) {
                front.setCurrentNamespace(context.userLogin, n)
                context.setUser(context.user!!.copy(namespace = n))
            } else {
                unauthorized()
            }
        })

        blockingDelete(
            "/namespace/:user/:namespace",
            logger = simpleLogger(
                "Remove Namespace from User",
                {
                    it.path("user") to it.path("namespace")
                }
            )
            , handler = toRequestHandler { context ->
            val user = context.path("user")
            val namespace = context.path("namespace")
            if (front.isNamespaceOwner(context.userLogin, namespace)) {
                front.deleteNamespace(user, namespace)
            } else {
                unauthorized()
            }
        })

        blockingJsonGet("/configuration/namespaces/shared") {
            RequestSucceeded(front.getSharableNamespaceConfiguration())
        }

        blockingJsonGet("/configuration/namespace/:namespace", handler = toRequestHandler { context ->
            val n = context.path("namespace")
            if (front.isNamespaceOwner(context.userLogin, n)) {
                front.getNamespaceConfiguration(n)
            } else {
                unauthorized()
            }
        })

        blockingJsonPost(
            "/configuration/namespace",
            admin,
            simpleLogger("Create or Update Namespace")
            , handler = toRequestHandler { context, conf: NamespaceConfiguration ->
            if (front.isNamespaceOwner(context.userLogin, conf.namespace)) {
                front.saveNamespaceConfiguration(conf)
            } else {
                unauthorized()
            }
        })
    }

    // cache index.html content
    @Volatile
    private var indexContent: Buffer? = null

    fun configureStaticHandling() {
        if (!devEnvironment) {
            // serve statics in docker image
            val webRoot = verticleProperty("content_path", "/maven/dist")
            // swagger yaml
            router.get("${baseHref}doc/nlp.yaml").handler { context ->
                context.vertx().fileSystem().readFile("$webRoot/doc/nlp.yaml") {
                    if (it.succeeded()) {
                        context.response().end(
                            it.result().toString(UTF_8).replace(
                                "_HOST_",
                                verticleProperty("nlp_external_host", "localhost:8888")
                            )
                        )
                    } else {
                        context.fail(it.cause())
                    }
                }
            }
            router.get("${baseHref}doc/admin.yaml").handler { context ->
                context.vertx().fileSystem().readFile("$webRoot/doc/admin.yaml") {
                    if (it.succeeded()) {
                        context.response().end(it.result().toString(UTF_8))
                    } else {
                        context.fail(it.cause())
                    }
                }
            }

            val indexContentHandler = Handler<RoutingContext> { context ->
                if (indexContent != null) {
                    context.response()
                        .putHeader(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
                        .end(indexContent)
                } else {
                    context.vertx().fileSystem().readFile("$webRoot/index.html") {
                        if (it.succeeded()) {
                            logger.info { "base href: $baseHref" }
                            val content = it.result()
                                .toString(UTF_8)
                                .replace("<base href=\"/\"", "<base href=\"$baseHref\"")
                            logger.debug { "content: $content" }
                            val result = Buffer.buffer(content)
                            if (!devEnvironment) {
                                indexContent = result
                            }
                            context.response()
                                .putHeader(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
                                .end(result)
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

            if (baseHref != "/") {
                router.route(GET, baseHref.substring(0, baseHref.length - 1)).handler(indexContentHandler)
            }
            router.route(GET, baseHref).handler(indexContentHandler)
            router.route(GET, "${baseHref}index.html").handler(indexContentHandler)

            router.route(GET, "$baseHref*")
                .handler(StaticHandler.create().setAllowRootFileSystemAccess(true).setWebRoot(webRoot))
                .handler(indexContentHandler)
        }
    }

    protected open fun deleteApplication(app: ApplicationDefinition) {
        front.deleteApplicationById(app._id)
    }

    protected open fun saveApplication(
        existingApp: ApplicationDefinition?,
        app: ApplicationDefinition
    ): ApplicationDefinition {
        return front.save(app)
    }

    override fun configure() {
        configureServices()
        configureStaticHandling()
    }

    override fun defaultHealthcheck(): (RoutingContext) -> Unit {
        return { it.response().end() }
    }

    override fun detailedHealthcheck(): (RoutingContext) -> Unit = detailedHealthcheck(
        listOf(
            Pair("duckling_service") { FrontClient.healthcheck() },
            Pair("tock_front_database") { pingMongoDatabase(TOCK_FRONT_DATABASE) },
            Pair("tock_model_database") { pingMongoDatabase(TOCK_MODEL_DATABASE) },
            Pair("tock_bot_database") { pingMongoDatabase(TOCK_BOT_DATABASE) }
        )
    )
}
