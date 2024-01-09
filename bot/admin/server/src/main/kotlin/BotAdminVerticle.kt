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

package ai.tock.bot.admin

import ai.tock.bot.admin.BotAdminService.createI18nRequest
import ai.tock.bot.admin.BotAdminService.dialogReportDAO
import ai.tock.bot.admin.BotAdminService.getBotConfigurationByApplicationIdAndBotId
import ai.tock.bot.admin.BotAdminService.getBotConfigurationsByNamespaceAndBotId
import ai.tock.bot.admin.BotAdminService.importStories
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.constants.Properties
import ai.tock.bot.admin.model.*
import ai.tock.bot.admin.module.satisfactionContentModule
import ai.tock.bot.admin.service.*
import ai.tock.bot.admin.story.dump.StoryDefinitionConfigurationDumpImport
import ai.tock.bot.admin.test.TestPlanService
import ai.tock.bot.admin.test.findTestService
import ai.tock.bot.admin.verticle.IndicatorVerticle
import ai.tock.bot.connector.ConnectorType.Companion.rest
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.rest.addRestConnector
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.config.SATISFACTION_MODULE_ID
import ai.tock.bot.engine.config.UploadedFilesService
import ai.tock.bot.engine.config.UploadedFilesService.downloadFile
import ai.tock.bot.engine.dialog.DialogFlowDAO
import ai.tock.bot.engine.message.Sentence
import ai.tock.nlp.admin.AdminVerticle
import ai.tock.nlp.admin.CsvCodec
import ai.tock.nlp.admin.model.ApplicationScopedQuery
import ai.tock.nlp.admin.model.TranslateReport
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.FaqSettingsQuery
import ai.tock.shared.*
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.NoEncryptionPassException
import ai.tock.shared.security.TockUserRole.*
import ai.tock.shared.vertx.ServerStatus
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.Translator
import ai.tock.translator.Translator.initTranslator
import ai.tock.translator.TranslatorEngine
import ch.tutteli.kbox.isNotNullAndNotBlank
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import io.vertx.core.http.HttpMethod.GET
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.toId

/**
 *
 */
open class BotAdminVerticle : AdminVerticle() {

    private val botAdminConfiguration = BotAdminConfiguration()

    private val indicatorVerticle = IndicatorVerticle()

    override val logger: KLogger = KotlinLogging.logger {}

    private val i18n: I18nDAO by injector.instance()

    private val dialogFlowDAO: DialogFlowDAO by injector.instance()

    private val front = FrontClient

    private val synchronizationService = SynchronizationService

    override val supportCreateNamespace: Boolean = !botAdminConfiguration.botApiSupport

    override fun protectedPaths(): Set<String> = setOf(rootPath)

    override fun configureServices() {
        vertx.eventBus().consumer<Boolean>(ServerStatus.SERVER_STARTED) {
            if (it.body() && booleanProperty(Properties.FAQ_MIGRATION_ENABLED, false)) {
                FaqAdminService.makeMigration()
            }
        }
        initTranslator()
        dialogFlowDAO.initFlowStatCrawl()
        super.configureServices()
    }

    private fun <R> measureTimeMillis(context: RoutingContext, function: () -> R): R {
        val before = System.currentTimeMillis()
        val result = function()
        logger.debug { "${context.normalizedPath()} took ${System.currentTimeMillis() - before} ms." }
        return result
    }

    private fun <R> checkAndMeasure(context: RoutingContext, request: ApplicationScopedQuery, function: () -> R): R =
        if (context.organization == request.namespace) {
            measureTimeMillis(context) {
                function()
            }
        } else {
            unauthorized()
        }

    override fun configure() {
        configureServices()

        indicatorVerticle.configure(this)

        blockingJsonPost("/users/search", botUser) { context, query: UserSearchQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.searchUsers(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/analytics/messages", setOf(botUser)) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByType(request)
            }
        }

        blockingJsonPost("/analytics/users", setOf(botUser)) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportUsersByType(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byConnectorType",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.countMessagesByConnectorType(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byConfiguration",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByConfiguration(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byConnectorType",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByConnectorType(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byDayOfWeek",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByDayOfWeek(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byHour",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByHour(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byIntent",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByIntent(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byDateAndIntent",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByDateAndIntent(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byDateAndStory",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByDateAndStory(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byStory",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByStory(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byStoryCategory",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByStoryCategory(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byStoryType",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByStoryType(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byStoryLocale",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByStoryLocale(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byActionType",
            setOf(botUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByActionType(request)
            }
        }

        blockingJsonPost(
            "/analytics/satisfaction/active",
            setOf(botUser)
        ) { context, query: ApplicationScopedQuery ->
            val botConf =
                getBotConfigurationsByNamespaceAndBotId(query.namespace, query.applicationName).firstOrNull()
                    ?: badRequest("No bot configuration detected.")
            val story = BotAdminService.findConfiguredStoryByBotIdAndIntent(
                query.namespace,
                botConf.botId,
                SATISFACTION_MODULE_ID
            )
            story != null
        }

        blockingJsonPost(
            "/analytics/satisfaction/init",
            setOf(botUser)
        ) { context, query: ApplicationScopedQuery ->
            val botConf =
                getBotConfigurationsByNamespaceAndBotId(query.namespace, query.applicationName).firstOrNull()
                    ?: badRequest("No bot configuration detected.")
            val story = BotAdminService.findConfiguredStoryByBotIdAndIntent(
                query.namespace,
                botConf.botId,
                SATISFACTION_MODULE_ID
            )
            if (story == null) {
                satisfactionContentModule.setupContent(
                    botConf,
                    query.language ?: defaultLocale,
                    context.userLogin
                )
            }
        }

        blockingJsonPost(
            "/analytics/satisfaction",
            setOf(botUser)
        ) { context, query: DialogsSearchQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.searchRating(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/dialogs/ratings/export",
            setOf(botUser)
        ) { context, query: DialogsSearchQuery ->
            if (context.organization == query.namespace) {
                val sb = StringBuilder()
                val printer = CsvCodec.newPrinter(sb)
                printer.printRecord(listOf("Timestamp", "Dialog ID", "Note", "Commentaire"))
                BotAdminService.search(query)
                    .dialogs
                    .forEach { label ->
                        printer.printRecord(
                            listOf(
                                label.actions.first().date,
                                label.id,
                                label.rating,
                                label.review,
                            )
                        )
                    }
                sb.toString()
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/dialogs/ratings/intents/export",
            setOf(botUser)
        ) { context, query: DialogsSearchQuery ->
            if (context.organization == query.namespace) {
                val sb = StringBuilder()
                val printer = CsvCodec.newPrinter(sb)
                printer.printRecord(
                    listOf(
                        "Timestamp",
                        "Intent",
                        "Dialog ID",
                        "Player Type",
                        "Application ID",
                        "Message"
                    )
                )
                BotAdminService.search(query)
                    .dialogs
                    .forEach { dialog ->
                        dialog.actions.forEach {
                            printer.printRecord(
                                listOf(
                                    it.date,
                                    it.intent,
                                    dialog.id,
                                    it.playerId.type,
                                    it.applicationId,
                                    if (it.message.isSimpleMessage()) it.message.toPrettyString().replace(
                                        "\n",
                                        " "
                                    ) else (it.message as Sentence).messages.joinToString { it.texts.values.joinToString() }
                                        .replace("\n", " ")
                                )
                            )
                        }
                    }
                sb.toString()

            } else {
                unauthorized()
            }
        }


        blockingJsonGet("/dialog/:applicationId/:dialogId", setOf(botUser)) { context ->
            val app = FrontClient.getApplicationById(context.pathId("applicationId"))
            if (context.organization == app?.namespace) {
                dialogReportDAO.getDialog(context.path("dialogId").toId())
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/dialog/:applicationId/:dialogId/satisfaction",
            setOf(botUser)
        ) { context, query: Set<String> ->
            val app = FrontClient.getApplicationById(context.pathId("applicationId"))
            if (context.organization == app?.namespace) {
                BotAdminService.getDialogObfuscatedById(context.pathId("dialogId"), query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/dialogs/search",
            setOf(botUser)
        ) { context, query: DialogsSearchQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.search(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet(
            "/dialogs/intents/:applicationId",
            setOf(botUser)
        ) { context ->
            val app = FrontClient.getApplicationById(context.path("applicationId").toId())
            app?.let { BotAdminService.getIntentsInDialogs(app.namespace, app.name) }
        }

        blockingJsonGet("/bots/:botId", setOf(botUser)) { context ->
            BotAdminService.getBots(context.organization, context.path("botId"))
        }

        blockingJsonPost(
            "/bot", admin,
            logger = logger<BotConfiguration>(" Create or Update Bot Configuration") { _, c ->
                c?.let { FrontClient.getApplicationByNamespaceAndName(it.namespace, it.nlpModel)?._id }
            }
        ) { context, bot: BotConfiguration ->
            if (context.organization == bot.namespace) {
                BotAdminService.save(bot)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/configuration/bots/:botId", setOf(botUser)) { context ->
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(context.organization, context.path("botId"))
        }

        blockingJsonPost("/configuration/bots") { context, query: ApplicationScopedQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.getBotConfigurationsByNamespaceAndNlpModel(query.namespace, query.applicationName)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/configuration/bots/:botId/rag", admin) { context, request: BotRAGConfigurationDTO  ->
            if (context.organization == request.namespace) {
                BotRAGConfigurationDTO(RAGService.saveRag(request))
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/configuration/bots/:botId/rag", admin) { context  ->
            RAGService.getRAGConfiguration(context.organization, context.path("botId"))
                ?.let { BotRAGConfigurationDTO(it) }
        }

        blockingDelete("/configuration/bots/:botId/rag", admin) { context  ->
            RAGService.deleteConfig(context.organization, context.path("botId"))
        }

        blockingJsonPost("/configuration/bots/:botId/observability", admin) { context, configuration: BotObservabilityConfigurationDTO  ->
            if (context.organization == configuration.namespace) {
                BotObservabilityConfigurationDTO(ObservabilityService.saveObservability(configuration))
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/configuration/bots/:botId/observability", admin) { context  ->
            ObservabilityService.getObservabilityConfiguration(context.organization, context.path("botId"))
                ?.let {
                    BotObservabilityConfigurationDTO(it)
                }
        }

        blockingDelete("/configuration/bots/:botId/observability", admin) { context  ->
            ObservabilityService.deleteConfig(context.organization, context.path("botId"))
        }

        blockingJsonPost("/configuration/bots/:botId/vector-store", admin) { context, configuration: BotVectorStoreConfigurationDTO  ->
            if (context.organization == configuration.namespace) {
                BotVectorStoreConfigurationDTO(VectorStoreService.saveVectorStore(configuration))
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/configuration/bots/:botId/vector-store", admin) { context  ->
            VectorStoreService.getVectorStoreConfiguration(context.organization, context.path("botId"))
                ?.let {
                    BotVectorStoreConfigurationDTO(it)
                }
        }

        blockingDelete("/configuration/bots/:botId/vector-store", admin) { context  ->
            VectorStoreService.deleteConfig(context.organization, context.path("botId"))
        }

        blockingJsonPost(
            "/configuration/bot", admin,
            logger = logger<BotConnectorConfiguration>("Create or Update Bot Connector Configuration") { _, c ->
                c?.let { FrontClient.getApplicationByNamespaceAndName(it.namespace, it.nlpModel)?._id }
            }
        ) { context, bot: BotConnectorConfiguration ->
            if (context.organization == bot.namespace) {
                if (bot._id != null) {
                    val conf = BotAdminService.getBotConfigurationById(bot._id)
                    if (conf == null || bot.namespace != conf.namespace || bot.botId != conf.botId) {
                        unauthorized()
                    }
                    if (getBotConfigurationByApplicationIdAndBotId(bot.namespace, bot.applicationId, bot.botId)
                            ?.run { _id != conf._id } == true
                    ) {
                        badRequest("Connector identifier already exists")
                    }
                } else {
                    if (getBotConfigurationByApplicationIdAndBotId(
                            bot.namespace,
                            bot.applicationId,
                            bot.botId
                        ) != null
                    ) {
                        badRequest("Connector identifier already exists")
                    }
                }
                bot.path?.let {
                    if (getBotConfigurationsByNamespaceAndBotId(
                            bot.namespace,
                            bot.botId
                        ).any { conf -> conf._id != bot._id && conf.path?.lowercase() == it.lowercase() }
                    )
                        badRequest("Connector path already exists (case-insensitive)")
                }
                val conf = bot.toBotApplicationConfiguration()
                val connectorProvider = BotRepository.findConnectorProvider(conf.connectorType)
                if (connectorProvider != null) {
                    val filledConf = if (bot.fillMandatoryValues) {
                        val additionalProperties = connectorProvider
                            .configuration()
                            .fields
                            .filter { it.mandatory && !bot.parameters.containsKey(it.key) }
                            .map {
                                it.key to "Please fill a value"
                            }
                            .toMap()
                        conf.copy(parameters = conf.parameters + additionalProperties)
                    } else {
                        conf
                    }
                    connectorProvider.check(filledConf.toConnectorConfiguration())
                        .apply {
                            if (isNotEmpty()) {
                                badRequest(joinToString())
                            }
                        }
                    try {
                        BotAdminService.saveApplicationConfiguration(filledConf)
                        // add rest connector
                        if (bot._id == null && bot.connectorType != rest) {
                            addRestConnector(filledConf).apply {
                                BotAdminService.saveApplicationConfiguration(
                                    BotApplicationConfiguration(
                                        connectorId,
                                        filledConf.botId,
                                        filledConf.namespace,
                                        filledConf.nlpModel,
                                        type,
                                        ownerConnectorType,
                                        getName(),
                                        getBaseUrl(),
                                        path = path,
                                        targetConfigurationId = conf._id
                                    )
                                )
                            }
                        }
                    } catch (t: Throwable) {
                        badRequest("Error creating/updating configuration: ${t.message}")
                    }
                } else {
                    badRequest("unknown connector provider ${conf.connectorType}")
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete(
            "/configuration/bot/:confId",
            admin,
            simpleLogger("Delete Bot Configuration", { it.path("confId") to true })
        ) { context ->
            BotAdminService.getBotConfigurationById(context.pathId("confId"))
                ?.let {
                    if (context.organization == it.namespace) {
                        BotAdminService.deleteApplicationConfiguration(it)
                        true
                    } else {
                        false
                    }
                } ?: unauthorized()
        }

        blockingJsonPost(
            "/configuration/synchronization", admin, simpleLogger("Overwrite Bot Configuration")
        ) { context, syncConfig: BotSynchronization ->
            if(allowAccessToAllNamespaces) {
                synchronizationService.synchronize(
                    syncConfig.source.namespace,
                    syncConfig.source.applicationName,
                    syncConfig.source.applicationId.toId(),
                    syncConfig.target.namespace,
                    syncConfig.target.applicationName,
                    syncConfig.withInboxMessages,
                    context.userLogin
                )
            } else {
                badRequest("The synchronization is disabled - ask your administrator to set the tock_namespace_open_access property")
            }
        }

        blockingJsonGet("/action/nlp-stats/:actionId", setOf(botUser)) { context ->
            dialogReportDAO.getNlpCallStats(context.pathId("actionId"), context.organization)
        }

        blockingJsonGet("/feature/:applicationId", setOf(botUser)) { context ->
            val applicationId = context.path("applicationId")
            BotAdminService.getFeatures(applicationId, context.organization)
        }

        blockingPost(
            "/feature/:applicationId/toggle",
            setOf(botUser),
            simpleLogger("Toogle Application Feature", { it.body().asString() })
        ) { context ->
            val applicationId = context.path("applicationId")
            val body = context.body().asString()
            val feature: Feature = mapper.readValue(body)
            BotAdminService.toggleFeature(applicationId, context.organization, feature)
        }

        blockingPost(
            "/feature/:applicationId/update",
            setOf(botUser),
            simpleLogger("Update Application Feature", { it.body().asString() })
        ) { context ->
            val applicationId = context.path("applicationId")
            val body = context.body().asString()
            val feature: Feature = mapper.readValue(body)
            BotAdminService.updateDateAndEnableFeature(
                applicationId,
                context.organization,
                feature
            )
        }

        blockingPost(
            "/feature/:applicationId/add",
            setOf(botUser),
            simpleLogger("Create Application Feature", { it.body().asString() })
        ) { context ->
            val applicationId = context.path("applicationId")
            val body = context.body().asString()
            val feature: Feature = mapper.readValue(body)
            BotAdminService.addFeature(applicationId, context.organization, feature)
        }

        blockingDelete(
            "/feature/:botId/:category/:name/",
            botUser,
            simpleLogger(
                "Delete Application Feature",
                { listOf(it.path("botId"), it.path("category"), it.path("name")) }
            )
        ) { context ->
            val category = context.path("category")
            val name = context.path("name")
            val botId = context.path("botId")
            BotAdminService.deleteFeature(botId, context.organization, category, name, null)
        }

        blockingDelete(
            "/feature/:botId/:category/:name/:applicationId",
            botUser,
            simpleLogger(
                "Delete Application Feature",
                { listOf(it.path("botId"), it.path("category"), it.path("name"), it.path("applicationId")) }
            )
        ) { context ->
            val applicationId = context.path("applicationId")
            val category = context.path("category")
            val name = context.path("name")
            val botId = context.path("botId")
            BotAdminService.deleteFeature(
                botId,
                context.organization,
                category,
                name,
                applicationId.takeUnless { it.isBlank() }
            )
        }

        blockingJsonGet("/application/:applicationId/plans", botUser) { context ->
            val applicationId = context.path("applicationId")
            TestPlanService.getTestPlansByApplication(applicationId).filter { it.namespace == context.organization }
        }

        blockingJsonPost("/application/plans", botUser) { context, query: ApplicationScopedQuery ->
            if (context.organization == query.namespace) {
                TestPlanService.getTestPlansByNamespaceAndNlpModel(query.namespace, query.applicationName)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/bot/story/new",
            setOf(botUser),
            logger<CreateStoryRequest>("Create Story") { context, r ->
                r?.story?.let { s ->
                    BotAdminService.getBotConfigurationsByNamespaceAndBotId(context.organization, s.botId)
                        .firstOrNull()
                        ?.let {
                            FrontClient.getApplicationByNamespaceAndName(
                                context.organization,
                                it.nlpModel
                            )?._id
                        }
                }
            }
        ) { context, query: CreateStoryRequest ->
            BotAdminService.createStory(context.organization, query, context.userLogin) ?: unauthorized()
        }

        blockingJsonGet("/bot/story/:appName/export", botUser) { context ->
            BotAdminService.exportStories(context.organization, context.path("appName"))
        }

        blockingJsonGet("/bot/story/:appName/export/:storyConfigurationId", botUser) { context ->
            val exportStory = BotAdminService.exportStory(
                context.organization,
                context.path("appName"),
                context.path("storyConfigurationId")
            )
            exportStory?.let { listOf(it) } ?: emptyList()
        }

        blockingJsonPost(
            "/bot/story/:appName/:locale/import",
            botUser,
            simpleLogger("JSON Import Response Labels")
        ) { context, dump: StoryDefinitionConfigurationDumpImport ->
            importStories(
                context.organization,
                context.path("appName"),
                context.pathToLocale("locale"),
                dump,
                context.userLogin
            )
        }

        blockingJsonPost(
            "/bot/story",
            setOf(botUser),
            logger<BotStoryDefinitionConfiguration>("Update Story") { context, r ->
                r?.let { s ->
                    getBotConfigurationsByNamespaceAndBotId(context.organization, s.botId)
                        .firstOrNull()
                        ?.let {
                            FrontClient.getApplicationByNamespaceAndName(
                                context.organization,
                                it.nlpModel
                            )?._id
                        }
                }
            }
        ) { context, story: BotStoryDefinitionConfiguration ->
            BotAdminService.saveStory(context.organization, story, context.userLogin) ?: unauthorized()
        }

        blockingJsonPost("/bot/story/load", setOf(botUser)) { context, request: StorySearchRequest ->
            if (context.organization == request.namespace) {
                BotAdminService.loadStories(request)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/bot/story/search", setOf(botUser)) { context, request: StorySearchRequest ->
            if (context.organization == request.namespace) {
                BotAdminService.searchStories(request)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/bot/story/search/summary",
            setOf(botUser)
        ) { context, request: SummaryStorySearchRequest ->
            if (context.organization == request.namespace) {
                if (request.applicationName.isEmpty()) {
                    badRequest("applicationName is needed")
                }
                BotAdminService.searchSummaryStories(request)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/bot/story/:storyId", setOf(botUser)) { context ->
            BotAdminService.findStory(context.organization, context.path("storyId"))
        }

        blockingJsonGet("/bot/story/:botId/with_document", botUser) { context ->
            BotAdminService.findStoryDefinitionsByNamespaceAndBotIdWithFileAttached(
                context.organization,
                context.path("botId")
            )
        }

        blockingJsonGet("/bot/story/:botId/settings", botUser) { context ->
            BotAdminService.findRuntimeStorySettings(context.organization, context.path("botId"))
        }

        blockingJsonGet("/bot/story/:botId/:intent", botUser) { context ->
            BotAdminService.findConfiguredStoryByBotIdAndIntent(
                context.organization,
                context.path("botId"),
                context.path("intent")
            )
        }

        blockingJsonDelete(
            "/bot/story/:storyId",
            setOf(botUser),
            simpleLogger("Delete Story", { it.path("storyId") })
        ) { context ->
            BotAdminService.deleteStory(context.organization, context.path("storyId"))
        }

        blockingJsonPost("/flow", botUser) { context, request: DialogFlowRequest ->
            if (context.organization == request.namespace) {
                measureTimeMillis(
                    context,
                    {
                        BotAdminService.loadDialogFlow(request)
                    }
                )
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/i18n", setOf(botUser)) { context ->
            val stats = i18n.getLabelStats(context.organization).groupBy { it.labelId }
            BotI18nLabels(
                i18n
                    .getLabels(context.organization)
                    .map {
                        BotI18nLabel(
                            it,
                            stats[it._id] ?: emptyList()
                        )
                    }
            )
        }

        blockingJsonPost(
            "/i18n/complete",
            setOf(botUser),
            simpleLogger("Complete Responses Labels")
        ) { context, labels: List<I18nLabel> ->
            if (!injector.provide<TranslatorEngine>().supportAdminTranslation) {
                badRequest("Translation is not activated for this account")
            }
            TranslateReport(Translator.completeAllLabels(labels.filter { it.namespace == context.organization }))
        }

        blockingJsonPost(
            "/i18n/saveAll",
            setOf(botUser),
            simpleLogger("Save Responses Labels")
        ) { context, labels: List<BotI18nLabelUpdate> ->
            i18n.save(labels.filter { it.namespace == context.organization })
        }

        blockingJsonPost(
            "/i18n/save",
            setOf(botUser),
            simpleLogger("Save Response Label")
        ) { context, label: BotI18nLabelUpdate ->
            if (label.namespace == context.organization) {
                i18n.save(label)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/i18n/create",
            setOf(botUser),
            simpleLogger("Create Response Label")
        ) { context, request: CreateI18nLabelRequest ->
            createI18nRequest(context.organization, request)
        }

        blockingDelete(
            "/i18n/:id",
            setOf(botUser),
            simpleLogger("Delete Response Label", { it.path("id") })
        ) { context ->
            i18n.deleteByNamespaceAndId(context.organization, context.pathId("id"))
        }

        blockingJsonGet("/i18n/export/csv", setOf(botUser)) { context ->
            I18nCsvCodec.exportCsv(context.organization)
        }

        blockingJsonPost("/i18n/export/csv", setOf(botUser)) { context, query: I18LabelQuery ->
            I18nCsvCodec.exportCsv(context.organization, query)
        }

        blockingUploadPost(
            "/i18n/import/csv",
            botUser,
            simpleLogger("CSV Import Response Labels")
        ) { context, content ->
            measureTimeMillis(context) {
                I18nCsvCodec.importCsv(context.organization, content)
            }
        }

        blockingJsonGet("/i18n/export/json", setOf(botUser)) { context ->
            mapper.writeValueAsString(i18n.getLabels(context.organization))
        }

        blockingJsonPost("/i18n/export/json", setOf(botUser)) { context, query: I18LabelQuery ->
            val labels = i18n.getLabels(context.organization, query.toI18nLabelFilter())
            mapper.writeValueAsString(labels)
        }

        blockingUploadJsonPost(
            "/i18n/import/json",
            setOf(botUser),
            simpleLogger("JSON Import Response Labels")
        ) { context, labels: List<I18nLabel> ->
            measureTimeMillis(context) {
                labels
                    .filter { it.i18n.any { i18n -> i18n.validated } }
                    .map {
                        it.copy(
                            _id = it._id.toString().replaceFirst(it.namespace, context.organization).toId(),
                            namespace = context.organization
                        )
                    }.apply {
                        i18n.save(this)
                    }
                    .size
            }
        }

        blockingUploadBinaryPost("/file", botUser) { context, (fileName, bytes) ->
            val file = UploadedFilesService.uploadFile(context.organization, fileName, bytes)
                ?: badRequest("file must have an extension (ie file.png)")
            file
        }

        blocking(GET, "/file/:id.:suffix", botUser) { context ->
            val id = context.path("id")
            if (!id.startsWith(context.organization)) {
                unauthorized()
            } else {
                downloadFile(context, id, context.path("suffix"))
            }
        }

        blockingJsonGet("/connectorTypes", setOf(botUser)) {
            ConnectorTypeConfiguration.connectorConfigurations
        }

        blockingGet("/connectorIcon/:connectorType/icon.svg", null, basePath) { context ->
            val connectorType = context.path("connectorType")
            context.response().putHeader("Content-Type", "image/svg+xml")
            context.response().putHeader("Cache-Control", "max-age=84600, public")
            ConnectorTypeConfiguration.connectorConfigurations.firstOrNull { it.connectorType.id == connectorType }?.svgIcon
                ?: ""
        }

        blockingJsonPost(
            "/faq",
            setOf(botUser),
            logger<FaqDefinitionRequest>("Save FAQ")
        ) { context, query: FaqDefinitionRequest ->
            if (query.utterances.isEmpty() && query.title.isBlank()) {
                badRequest("Missing argument or trouble in query: $query")
            } else {
                val applicationDefinition = front.getApplicationByNamespaceAndName(
                    namespace = context.organization,
                    name = query.applicationName
                )
                if (context.organization == applicationDefinition?.namespace) {
                    return@blockingJsonPost FaqAdminService.saveFAQ(query, context.userLogin, applicationDefinition)
                } else {
                    unauthorized()
                }
            }
        }

        blockingJsonDelete(
            "/faq/:faqId",
            setOf(botUser),
            simpleLogger("Delete Story", { it.path("faqId") })
        ) { context ->
            FaqAdminService.deleteFaqDefinition(context.organization, context.path("faqId"))
        }

        blockingJsonPost("/faq/tags", setOf(botUser)) { context, applicationId: String ->
            val applicationDefinition = front.getApplicationById(applicationId.toId())
            if (context.organization == applicationDefinition?.namespace) {
                try {
                    FaqAdminService.searchTags(applicationDefinition._id.toString())
                } catch (t: Exception) {
                    logger.error(t)
                    badRequest("Error searching faq tags: ${t.message}")
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/faq/search",
            setOf(botUser),
            logger<FaqSearchRequest>("Search FAQ")
        )
        { context, request: FaqSearchRequest ->
            val applicationDefinition =
                front.getApplicationByNamespaceAndName(request.namespace, request.applicationName)
            if (context.organization == applicationDefinition?.namespace) {
                try {
                    measureTimeMillis(context) {
                        FaqAdminService.searchFAQ(request, applicationDefinition)
                    }
                } catch (t: NoEncryptionPassException) {
                    logger.error(t)
                    badRequest("Error obfuscating faq: ${t.message}")
                } catch (t: Exception) {
                    logger.error(t)
                    badRequest("Error searching faq: ${t.message}")
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonGet(
            "/faq/settings/:applicationId",
            setOf(botUser)
        ) { context ->
            val applicationDefinition = front.getApplicationById(context.pathId("applicationId"))
            if (context.organization == applicationDefinition?.namespace) {
                FaqAdminService.getSettings(applicationDefinition)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/faq/settings/:applicationId",
            setOf(botUser)
        ) { context, faqSettingsQuery: FaqSettingsQuery ->
            val applicationDefinition = front.getApplicationById(context.pathId("applicationId"))
            if (context.organization == applicationDefinition?.namespace) {
                FaqAdminService.saveSettings(applicationDefinition, faqSettingsQuery)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/gen-ai/bot/:botId/sentence-generation",
            setOf(botUser)
        ) { context, request: SentenceGenerationRequest ->
            CompletionService.generateSentences(
                request,
                namespace = context.organization,
                botId = context.path("botId")
            )
        }

        blockingJsonPost(
            "/configuration/bots/:botId/sentence-generation/configuration",
            admin
        ) { context, configuration: BotSentenceGenerationConfigurationDTO ->
            if (context.organization == configuration.namespace) {
                BotSentenceGenerationConfigurationDTO(
                    SentenceGenerationService.saveSentenceGeneration(configuration)
                )
            } else {
                unauthorized()
            }
        }

        blockingJsonGet(
            "/configuration/bots/:botId/sentence-generation/configuration",
            admin
        ) { context ->
            SentenceGenerationService.getSentenceGenerationConfiguration(context.organization,
                context.path("botId"))
                ?.let {
                    BotSentenceGenerationConfigurationDTO(it)
                }
        }

        blockingJsonGet(
            "/configuration/bots/:botId/sentence-generation/info",
            nlpUser
        ) { context ->
            SentenceGenerationService.getSentenceGenerationConfiguration(context.organization,
                context.path("botId"))
                ?.let {
                    BotSentenceGenerationInfoDTO(it)
                } ?: BotSentenceGenerationInfoDTO()
        }

        blockingDelete(
            "/configuration/bots/:botId/sentence-generation/configuration",
            admin
        ) { context ->
            SentenceGenerationService.deleteConfig(context.organization, context.path("botId"))
        }

        blockingJsonGet("/configuration") {
            botAdminConfiguration
        }

        findTestService().registerServices().invoke(this)

        configureStaticHandling()
    }

    override fun deleteApplication(app: ApplicationDefinition) {
        super.deleteApplication(app)
        BotAdminService.deleteApplication(app)
    }

    override fun saveApplication(
        existingApp: ApplicationDefinition?,
        app: ApplicationDefinition
    ): ApplicationDefinition {
        if (existingApp != null && existingApp.name != app.name) {
            BotAdminService.changeApplicationName(existingApp, app)
        }
        if (app.supportedLocales != existingApp?.supportedLocales) {
            BotAdminService.changeSupportedLocales(app)
        }
        return super.saveApplication(existingApp, app)
    }
}
