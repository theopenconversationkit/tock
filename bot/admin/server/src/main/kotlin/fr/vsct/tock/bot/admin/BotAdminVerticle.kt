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

package fr.vsct.tock.bot.admin

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.admin.BotAdminService.createI18nRequest
import fr.vsct.tock.bot.admin.BotAdminService.dialogReportDAO
import fr.vsct.tock.bot.admin.BotAdminService.getBotConfigurationByApplicationIdAndBotId
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.bot.BotConfiguration
import fr.vsct.tock.bot.admin.model.BotAdminConfiguration
import fr.vsct.tock.bot.admin.model.BotConnectorConfiguration
import fr.vsct.tock.bot.admin.model.BotDialogRequest
import fr.vsct.tock.bot.admin.model.BotI18nLabel
import fr.vsct.tock.bot.admin.model.BotI18nLabels
import fr.vsct.tock.bot.admin.model.BotStoryDefinitionConfiguration
import fr.vsct.tock.bot.admin.model.CreateI18nLabelRequest
import fr.vsct.tock.bot.admin.model.CreateStoryRequest
import fr.vsct.tock.bot.admin.model.DialogFlowRequest
import fr.vsct.tock.bot.admin.model.DialogsSearchQuery
import fr.vsct.tock.bot.admin.model.StorySearchRequest
import fr.vsct.tock.bot.admin.model.TestPlanUpdate
import fr.vsct.tock.bot.admin.model.UserSearchQuery
import fr.vsct.tock.bot.admin.model.XRayPlanExecutionConfiguration
import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.TestPlanService
import fr.vsct.tock.bot.admin.test.xray.XrayService
import fr.vsct.tock.bot.connector.ConnectorType.Companion.rest
import fr.vsct.tock.bot.connector.ConnectorTypeConfiguration
import fr.vsct.tock.bot.connector.rest.addRestConnector
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.config.UploadedFilesService
import fr.vsct.tock.bot.engine.config.UploadedFilesService.downloadFile
import fr.vsct.tock.nlp.admin.AdminVerticle
import fr.vsct.tock.nlp.admin.model.ApplicationScopedQuery
import fr.vsct.tock.nlp.admin.model.TranslateReport
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.provide
import fr.vsct.tock.shared.security.TockUserRole.admin
import fr.vsct.tock.shared.security.TockUserRole.botUser
import fr.vsct.tock.translator.I18nDAO
import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.Translator
import fr.vsct.tock.translator.Translator.initTranslator
import fr.vsct.tock.translator.TranslatorEngine
import io.vertx.core.http.HttpMethod.GET
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.newId

/**
 *
 */
open class BotAdminVerticle : AdminVerticle() {

    override val logger: KLogger = KotlinLogging.logger {}

    private val i18n: I18nDAO by injector.instance()

    override fun configureServices() {
        initTranslator()
        super.configureServices()
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

        blockingJsonPost("/dialogs/search", botUser) { context, query: DialogsSearchQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.search(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/bots/:botId", botUser) { context ->
            BotAdminService.getBots(context.organization, context.path("botId"))
        }

        blockingJsonPost("/bot") { context, bot: BotConfiguration ->
            if (context.organization == bot.namespace) {
                BotAdminService.save(bot)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/configuration/bots/:botId", botUser) { context ->
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(context.organization, context.path("botId"))
        }

        blockingJsonPost("/configuration/bots") { context, query: ApplicationScopedQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.getBotConfigurationsByNamespaceAndNlpModel(query.namespace, query.applicationName)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/configuration/bot") { context, bot: BotConnectorConfiguration ->
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
                    if (getBotConfigurationByApplicationIdAndBotId(bot.namespace, bot.applicationId, bot.botId) != null
                    ) {
                        badRequest("Connector identifier already exists")
                    }
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
                    BotAdminService.saveApplicationConfiguration(filledConf)
                    //add rest connector
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
                } else {
                    badRequest("unknown connector provider ${conf.connectorType}")
                }
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete("/configuration/bot/:confId", admin) { context ->
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

        blockingJsonPost("/test/talk", botUser) { context, query: BotDialogRequest ->
            if (context.organization == query.namespace) {
                BotAdminService.talk(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/action/nlp-stats/:actionId", botUser) { context ->
            dialogReportDAO.getNlpCallStats(context.pathId("actionId"), context.organization)
        }


        /**
         * --------------------------------------------------------------------------------------
         *                                       TEST PART
         * --------------------------------------------------------------------------------------
         */
        blockingJsonGet("/test/plans", botUser) { context ->
            TestPlanService.getTestPlansByNamespace(context.organization)
        }

        blockingJsonGet("/test/plan/:planId", botUser) { context ->
            TestPlanService.getTestPlan(context.pathId("planId"))
        }

        blockingJsonGet("/test/plan/:planId/executions", botUser) { context ->
            TestPlanService.getPlanExecutions(context.loadTestPlan())
        }

        blockingJsonGet("/test/plan/:planId/executions/:executionId", botUser) { context ->
            TestPlanService.getTestPlanExecution(context.loadTestPlan(), context.pathId("executionId"))
        }

        blockingJsonPost("/test/plan", botUser) { context, plan: TestPlanUpdate ->
            if (context.organization == plan.namespace) {
                TestPlanService.saveTestPlan(plan.toTestPlan())
            } else {
                unauthorized()
            }
        }

        blockingDelete("/test/plan/:planId", botUser) { context ->
            TestPlanService.removeTestPlan(context.loadTestPlan())
        }

        blockingJsonPost("/test/plan/:planId/dialog/:dialogId", botUser) { context, _: ApplicationScopedQuery ->
            TestPlanService.addDialogToTestPlan(context.loadTestPlan(), context.pathId("dialogId"))
        }

        blockingJsonPost("/test/plan/:planId/dialog/delete/:dialogId", botUser) { context, _: ApplicationScopedQuery ->
            TestPlanService.removeDialogFromTestPlan(
                context.loadTestPlan(),
                context.pathId("dialogId")
            )
        }

        blockingJsonPost("/test/plan/execute", botUser) { context, testPlan: TestPlan ->
            BotAdminService.saveAndExecuteTestPlan(context.organization, testPlan, newId())
        }

        /**
         * Triggered on click on "Laucnh" button.
         */
        blockingJsonPost("/test/plan/:planId/run", botUser) { context, _: ApplicationScopedQuery ->
            context.loadTestPlan().run {
                BotAdminService.executeTestPlan(this)
            }
        }

        /**
         * Triggered on "Create" button, after providing connector and test plan key.
         */
        blockingJsonPost("/xray/execute", botUser) { context, configuration: XRayPlanExecutionConfiguration ->
            XrayService(
                listOfNotNull(configuration.configurationId),
                listOf(configuration.testPlanKey),
                listOf(),
                configuration.testedBotId
            ).executePlans(context.organization)
        }
        /**
         * --------------------------------------------------------------------------------------
         *                                     END TEST PART
         * --------------------------------------------------------------------------------------
         */


        blockingJsonGet("/feature/:applicationId", botUser) { context ->
            val applicationId = context.path("applicationId")
            BotAdminService.getFeatures(applicationId, context.organization)
        }

        blockingPost("/feature/:applicationId/toggle/:category/:name", botUser) { context ->
            val applicationId = context.path("applicationId")
            val category = context.path("category")
            val name = context.path("name")
            BotAdminService.toggleFeature(applicationId, context.organization, category, name)
        }

        blockingPost("/feature/:applicationId/add/:category/:name/:enabled", botUser) { context ->
            val applicationId = context.path("applicationId")
            val category = context.path("category")
            val name = context.path("name")
            val enabled = context.path("enabled") == "true"
            BotAdminService.addFeature(applicationId, context.organization, enabled, category, name)
        }

        blockingDelete("/feature/:applicationId/:category/:name", botUser) { context ->
            val applicationId = context.path("applicationId")
            val category = context.path("category")
            val name = context.path("name")
            BotAdminService.deleteFeature(applicationId, context.organization, category, name)
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

        blockingJsonPost("/bot/story/new", botUser) { context, query: CreateStoryRequest ->
            BotAdminService.createStory(context.organization, query) ?: unauthorized()
        }

        blockingJsonPost("/bot/story", botUser) { context, story: BotStoryDefinitionConfiguration ->
            BotAdminService.saveStory(context.organization, story) ?: unauthorized()
        }

        blockingJsonPost("/bot/story/search", botUser) { context, request: StorySearchRequest ->
            if (context.organization == request.namespace) {
                BotAdminService.loadStories(request)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/bot/story/:storyId", botUser) { context ->
            BotAdminService.findStory(context.organization, context.path("storyId"))
        }

        blockingJsonGet("/bot/story/:botId/:intent", botUser) { context ->
            BotAdminService.findStoryByBotIdAndIntent(context.organization, context.path("botId"), context.path("intent"))
        }

        blockingJsonDelete("/bot/story/:storyId", botUser) { context ->
            BotAdminService.deleteStory(context.organization, context.path("storyId"))
        }

        blockingJsonPost("/flow", botUser) { context, request: DialogFlowRequest ->
            if (context.organization == request.namespace) {
                BotAdminService.loadDialogFlow(request)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/i18n", botUser) { context ->
            val stats = i18n.getLabelStats(context.organization).groupBy { it.labelId }
            BotI18nLabels(i18n
                .getLabels(context.organization)
                .map {
                    BotI18nLabel(
                        it,
                        stats[it._id] ?: emptyList()
                    )
                })
        }

        blockingJsonPost("/i18n/complete", botUser) { context, labels: List<I18nLabel> ->
            if (!injector.provide<TranslatorEngine>().supportAdminTranslation) {
                badRequest("Translation is not activated for this account")
            }
            TranslateReport(Translator.completeAllLabels(labels.filter { it.namespace == context.organization }))
        }

        blockingJsonPost("/i18n/saveAll", botUser) { context, labels: List<I18nLabel> ->
            i18n.save(labels.filter { it.namespace == context.organization })
        }

        blockingJsonPost("/i18n/save", botUser) { context, label: I18nLabel ->
            if (label.namespace == context.organization) {
                i18n.save(label)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/i18n/create", botUser) { context, request: CreateI18nLabelRequest ->
            createI18nRequest(context.organization, request)
        }

        blockingDelete("/i18n/:id", botUser) { context ->
            i18n.deleteByNamespaceAndId(context.organization, context.pathId("id"))
        }

        blockingJsonGet("/i18n/export/csv", botUser) { context ->
            I18nCsvCodec.exportCsv(context.organization)
        }

        blockingUploadPost("/i18n/import/csv", botUser) { context, content ->
            I18nCsvCodec.importCsv(context.organization, content)
        }

        blockingJsonGet("/i18n/export/json", botUser) { context ->
            mapper.writeValueAsString(i18n.getLabels(context.organization))
        }

        blockingUploadPost("/i18n/import/json", botUser) { context, content ->
            val labels: List<I18nLabel> = mapper.readValue(content)
            i18n.save(labels.filter { it.namespace == context.organization })
        }

        blockingUploadBinaryPost("/file", botUser) { context, (fileName, bytes) ->
            val file = UploadedFilesService.uploadFile(context.organization, fileName, bytes)
            if (file == null) {
                badRequest("file must have an extension (ie file.png)")
            }
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

        blockingJsonGet("/connectorTypes", botUser) {
            ConnectorTypeConfiguration.connectorConfigurations
        }

        blockingGet("/connectorIcon/:connectorType/icon.svg", null, basePath) { context ->
            val connectorType = context.path("connectorType")
            context.response().putHeader("Content-Type", "image/svg+xml")
            context.response().putHeader("Cache-Control", "max-age=84600, public")
            ConnectorTypeConfiguration.connectorConfigurations.first { it.connectorType.id == connectorType }.svgIcon
                ?: ""
        }

        blockingJsonGet("/configuration") {
            BotAdminConfiguration()
        }

        configureStaticHandling()
    }

    override fun deleteApplication(app: ApplicationDefinition) {
        super.deleteApplication(app)
        BotAdminService.deleteApplication(app)
    }

    override fun saveApplication(existingApp: ApplicationDefinition?, app: ApplicationDefinition): ApplicationDefinition {
        if (existingApp != null && existingApp.name != app.name) {
            BotAdminService.changeApplicationName(existingApp, app)
        }
        return super.saveApplication(existingApp, app)
    }

    fun RoutingContext.loadTestPlan(): TestPlan {
        return TestPlanService.getTestPlan(pathId("planId"))?.run {
            if (organization != namespace) {
                unauthorized()
            } else {
                this
            }
        } ?: notFound()
    }

}