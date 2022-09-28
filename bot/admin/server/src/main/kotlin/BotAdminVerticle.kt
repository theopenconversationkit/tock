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
import ai.tock.bot.admin.dialog.DialogReportQuery
import ai.tock.bot.admin.model.BotAdminConfiguration
import ai.tock.bot.admin.model.BotConnectorConfiguration
import ai.tock.bot.admin.model.BotI18nLabel
import ai.tock.bot.admin.model.BotI18nLabels
import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.model.CreateI18nLabelRequest
import ai.tock.bot.admin.model.CreateStoryRequest
import ai.tock.bot.admin.model.DialogFlowRequest
import ai.tock.bot.admin.model.DialogsSearchQuery
import ai.tock.bot.admin.model.FaqDefinitionRequest
import ai.tock.bot.admin.model.FaqSearchRequest
import ai.tock.bot.admin.model.Feature
import ai.tock.bot.admin.model.I18LabelQuery
import ai.tock.bot.admin.model.StorySearchRequest
import ai.tock.bot.admin.model.UserSearchQuery
import ai.tock.bot.admin.story.dump.StoryDefinitionConfigurationDump
import ai.tock.bot.admin.test.TestPlanService
import ai.tock.bot.admin.test.findTestService
import ai.tock.bot.connector.ConnectorType.Companion.rest
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.rest.addRestConnector
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.config.UploadedFilesService
import ai.tock.bot.engine.config.UploadedFilesService.downloadFile
import ai.tock.bot.engine.dialog.DialogFlowDAO
import ai.tock.nlp.admin.AdminVerticle
import ai.tock.nlp.admin.model.ApplicationScopedQuery
import ai.tock.nlp.admin.model.TranslateReport
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.FaqSettingsQuery
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.provide
import ai.tock.shared.security.NoEncryptionPassException
import ai.tock.shared.security.TockUserRole.admin
import ai.tock.shared.security.TockUserRole.botUser
import ai.tock.shared.security.TockUserRole.faqBotUser
import ai.tock.shared.security.TockUserRole.faqNlpUser
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.Translator
import ai.tock.translator.Translator.initTranslator
import ai.tock.translator.TranslatorEngine
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

    override val logger: KLogger = KotlinLogging.logger {}

    private val i18n: I18nDAO by injector.instance()

    private val dialogFlowDAO: DialogFlowDAO by injector.instance()

    private val front = FrontClient

    override val supportCreateNamespace: Boolean = !botAdminConfiguration.botApiSupport

    override fun configureServices() {
        initTranslator()
        dialogFlowDAO.initFlowStatCrawl()
        super.configureServices()
    }

    private fun <R> measureTimeMillis(context: RoutingContext, function: () -> R): R {
        val before = System.currentTimeMillis()
        val result = function()
        logger.debug { "${context.normalisedPath()} took ${System.currentTimeMillis() - before} ms." }
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

        blockingJsonPost("/users/search", botUser) { context, query: UserSearchQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.searchUsers(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/analytics/messages", setOf(botUser, faqBotUser)) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByType(request)
            }
        }

        blockingJsonPost("/analytics/users", setOf(botUser, faqBotUser)) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportUsersByType(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byConnectorType",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.countMessagesByConnectorType(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byConfiguration",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByConfiguration(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byConnectorType",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByConnectorType(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byDayOfWeek",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByDayOfWeek(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byHour",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByHour(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byIntent",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByIntent(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byDateAndIntent",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByDateAndIntent(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byDateAndStory",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByDateAndStory(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byStory",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByStory(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byStoryCategory",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByStoryCategory(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byStoryType",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByStoryType(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byStoryLocale",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByStoryLocale(request)
            }
        }

        blockingJsonPost(
            "/analytics/messages/byActionType",
            setOf(botUser, faqBotUser)
        ) { context, request: DialogFlowRequest ->
            checkAndMeasure(context, request) {
                BotAdminAnalyticsService.reportMessagesByActionType(request)
            }
        }

        blockingJsonGet("/dialog/:applicationId/:dialogId", setOf(botUser, faqBotUser)) { context ->
            val app = FrontClient.getApplicationById(context.pathId("applicationId"))
            if (context.organization == app?.namespace) {
                dialogReportDAO
                    .search(
                        DialogReportQuery(
                            context.organization,
                            app.name,
                            dialogId = context.path("dialogId")
                        )
                    )
                    .run {
                        dialogs.firstOrNull()
                    }
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/dialogs/search",
            setOf(botUser, faqNlpUser, faqBotUser)
        ) { context, query: DialogsSearchQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.search(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/bots/:botId", setOf(botUser, faqNlpUser, faqBotUser)) { context ->
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

        blockingJsonGet("/configuration/bots/:botId", setOf(botUser, faqBotUser)) { context ->
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(context.organization, context.path("botId"))
        }

        blockingJsonPost("/configuration/bots") { context, query: ApplicationScopedQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.getBotConfigurationsByNamespaceAndNlpModel(query.namespace, query.applicationName)
            } else {
                unauthorized()
            }
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
                        null
                    }
                } ?: unauthorized()
        }

        blockingJsonGet("/action/nlp-stats/:actionId", setOf(botUser, faqBotUser)) { context ->
            dialogReportDAO.getNlpCallStats(context.pathId("actionId"), context.organization)
        }

        blockingJsonGet("/feature/:applicationId", setOf(botUser, faqBotUser)) { context ->
            val applicationId = context.path("applicationId")
            BotAdminService.getFeatures(applicationId, context.organization)
        }

        blockingPost(
            "/feature/:applicationId/toggle",
            setOf(botUser, faqBotUser),
            simpleLogger("Toogle Application Feature", { it.bodyAsString })
        ) { context ->
            val applicationId = context.path("applicationId")
            val body = context.bodyAsString
            val feature: Feature = mapper.readValue(body)
            BotAdminService.toggleFeature(applicationId, context.organization, feature)
        }

        blockingPost(
            "/feature/:applicationId/update",
            setOf(botUser, faqBotUser),
            simpleLogger("Update Application Feature", { it.bodyAsString })
        ) { context ->
            val applicationId = context.path("applicationId")
            val body = context.bodyAsString
            val feature: Feature = mapper.readValue(body)
            BotAdminService.updateDateAndEnableFeature(
                applicationId,
                context.organization,
                feature
            )
        }

        blockingPost(
            "/feature/:applicationId/add",
            setOf(botUser, faqBotUser),
            simpleLogger("Create Application Feature", { it.bodyAsString })
        ) { context ->
            val applicationId = context.path("applicationId")
            val body = context.bodyAsString
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
            setOf(botUser, faqBotUser),
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

        blockingUploadJsonPost(
            "/bot/story/:appName/:locale/import",
            botUser,
            simpleLogger("JSON Import Response Labels")
        ) { context, stories: List<StoryDefinitionConfigurationDump> ->
            importStories(
                context.organization,
                context.path("appName"),
                context.pathToLocale("locale"),
                stories,
                context.userLogin
            )
        }

        blockingJsonPost(
            "/bot/story",
            setOf(botUser, faqBotUser),
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

        blockingJsonPost("/bot/story/load", setOf(botUser, faqBotUser)) { context, request: StorySearchRequest ->
            if (context.organization == request.namespace) {
                BotAdminService.loadStories(request)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/bot/story/search", setOf(botUser, faqBotUser)) { context, request: StorySearchRequest ->
            if (context.organization == request.namespace) {
                BotAdminService.searchStories(request)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/bot/story/:storyId", setOf(botUser, faqBotUser)) { context ->
            BotAdminService.findStory(context.organization, context.path("storyId"))
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
            setOf(botUser, faqBotUser),
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

        blockingJsonGet("/i18n", setOf(botUser, faqBotUser)) { context ->
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
            setOf(botUser, faqBotUser),
            simpleLogger("Complete Responses Labels")
        ) { context, labels: List<I18nLabel> ->
            if (!injector.provide<TranslatorEngine>().supportAdminTranslation) {
                badRequest("Translation is not activated for this account")
            }
            TranslateReport(Translator.completeAllLabels(labels.filter { it.namespace == context.organization }))
        }

        blockingJsonPost(
            "/i18n/saveAll",
            setOf(botUser, faqBotUser),
            simpleLogger("Save Responses Labels")
        ) { context, labels: List<I18nLabel> ->
            i18n.save(labels.filter { it.namespace == context.organization })
        }

        blockingJsonPost(
            "/i18n/save",
            setOf(botUser, faqBotUser),
            simpleLogger("Save Response Label")
        ) { context, label: I18nLabel ->
            if (label.namespace == context.organization) {
                i18n.save(label)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost(
            "/i18n/create",
            setOf(botUser, faqBotUser),
            simpleLogger("Create Response Label")
        ) { context, request: CreateI18nLabelRequest ->
            createI18nRequest(context.organization, request)
        }

        blockingDelete(
            "/i18n/:id",
            setOf(botUser, faqBotUser),
            simpleLogger("Delete Response Label", { it.path("id") })
        ) { context ->
            i18n.deleteByNamespaceAndId(context.organization, context.pathId("id"))
        }

        blockingJsonGet("/i18n/export/csv", setOf(botUser, faqBotUser)) { context ->
            I18nCsvCodec.exportCsv(context.organization)
        }

        blockingJsonPost("/i18n/export/csv", setOf(botUser, faqBotUser)) { context, query: I18LabelQuery ->
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

        blockingJsonGet("/i18n/export/json", setOf(botUser, faqBotUser)) { context ->
            mapper.writeValueAsString(i18n.getLabels(context.organization))
        }

        blockingJsonPost("/i18n/export/json", setOf(botUser, faqBotUser)) { context, query: I18LabelQuery ->
            val labels = i18n.getLabels(context.organization, query.toI18nLabelFilter())
            mapper.writeValueAsString(labels)
        }

        blockingUploadJsonPost(
            "/i18n/import/json",
            setOf(botUser, faqBotUser),
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

        blockingJsonGet("/connectorTypes", setOf(botUser, faqBotUser, faqNlpUser)) {
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
            setOf(botUser, faqBotUser),
            logger<FaqDefinitionRequest>("Save FAQ")
        ) { context, query: FaqDefinitionRequest ->
            if (query.utterances.isEmpty() && query.title.isBlank() && query.answer.isBlank()) {
                badRequest("Missing argument or trouble in query: $query")
            } else {
                val applicationDefinition = front.getApplicationById(query.applicationId)
                if (context.organization == applicationDefinition?.namespace) {
                    return@blockingJsonPost FaqAdminService.saveFAQ(query, context.userLogin, applicationDefinition)
                } else {
                    unauthorized()
                }
            }
        }

        blockingJsonDelete(
            "/faq/:faqId",
            setOf(botUser, faqBotUser),
            simpleLogger("Delete Story", { it.path("faqId") })
        ) { context ->
            FaqAdminService.deleteFaqDefinition(context.organization, context.path("faqId"))
        }

        blockingJsonPost("/faq/tags", setOf(botUser, faqBotUser)) { context, applicationId: String ->
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
            setOf(botUser, faqBotUser),
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
            setOf(botUser, faqBotUser)
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
            setOf(botUser, faqBotUser)
        ) { context, faqSettingsQuery: FaqSettingsQuery ->
            val applicationDefinition = front.getApplicationById(context.pathId("applicationId"))
            if (context.organization == applicationDefinition?.namespace) {
                FaqAdminService.saveSettings(applicationDefinition, faqSettingsQuery)
            } else {
                unauthorized()
            }
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
