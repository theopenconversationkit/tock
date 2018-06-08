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
import fr.vsct.tock.bot.admin.BotAdminService.getBotConfigurationByApplicationIdAndBotId
import fr.vsct.tock.bot.admin.model.BotConfiguration
import fr.vsct.tock.bot.admin.model.BotDialogRequest
import fr.vsct.tock.bot.admin.model.BotIntentSearchRequest
import fr.vsct.tock.bot.admin.model.CreateBotIntentRequest
import fr.vsct.tock.bot.admin.model.DialogsSearchQuery
import fr.vsct.tock.bot.admin.model.TestPlanUpdate
import fr.vsct.tock.bot.admin.model.UpdateBotIntentRequest
import fr.vsct.tock.bot.admin.model.UserSearchQuery
import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.TestPlanService
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.nlp.admin.AdminVerticle
import fr.vsct.tock.nlp.admin.model.ApplicationScopedQuery
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.security.TockUserRole.admin
import fr.vsct.tock.shared.security.TockUserRole.botUser
import fr.vsct.tock.translator.I18nDAO
import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.Translator
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

/**
 *
 */
open class BotAdminVerticle : AdminVerticle() {

    override val logger: KLogger = KotlinLogging.logger {}

    val i18n: I18nDAO  by injector.instance()

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

        blockingJsonGet("/configuration/bots/:botId", botUser) { context ->
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(context.organization, context.pathParam("botId"))
        }

        blockingJsonPost("/configuration/bots", admin) { context, query: ApplicationScopedQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.getBotConfigurationsByNamespaceAndNlpModel(query.namespace, query.applicationName)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/configuration/bot", admin) { context, bot: BotConfiguration ->
            if (context.organization == bot.namespace) {
                if (bot._id != null) {
                    val conf = BotAdminService.getBotConfigurationById(bot._id)
                    if (conf == null || bot.namespace != conf.namespace || bot.botId != conf.botId) {
                        unauthorized()
                    }
                    if (getBotConfigurationByApplicationIdAndBotId(bot.applicationId, bot.botId)
                            ?.run { _id != conf._id } == true
                    ) {
                        badRequest("Connector identifier already exists")
                    }
                } else {
                    if (getBotConfigurationByApplicationIdAndBotId(bot.applicationId, bot.botId) != null
                    ) {
                        badRequest("Connector identifier already exists")
                    }
                }
                val conf = bot.toBotApplicationConfiguration()
                val connectorProvider = BotRepository.findConnectorProvider(conf.connectorType)
                connectorProvider.check(conf.toConnectorConfiguration())
                    .apply {
                        if (isNotEmpty()) {
                            badRequest(joinToString())
                        }
                    }
                BotAdminService.saveApplicationConfiguration(conf)
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

        blockingJsonGet("/test/plans", botUser) { context ->
            TestPlanService.getTestPlansByNamespace(context.organization)
        }

        blockingJsonGet("/test/plan/:planId/executions", botUser) { context ->
            TestPlanService.getPlanExecutions(context.loadTestPlan())
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
            BotAdminService.getBotConfiguration(testPlan.botApplicationConfigurationId, context.organization)
                .let {
                    TestPlanService.saveAndRunTestPlan(
                        BotAdminService.getRestClient(it),
                        testPlan
                    )
                }
        }

        blockingJsonPost("/test/plan/:planId/run", botUser) { context, _: ApplicationScopedQuery ->
            context.loadTestPlan().run {
                TestPlanService.runTestPlan(
                    BotAdminService.getRestClient(
                        BotAdminService.getBotConfiguration(
                            botApplicationConfigurationId,
                            namespace
                        )
                    ),
                    this
                )
            }
        }

        blockingJsonGet("/application/:applicationId/plans", botUser) { context ->
            val applicationId = context.pathParam("applicationId")
            TestPlanService.getTestPlansByApplication(applicationId).filter { it.namespace == context.organization }
        }

        blockingJsonPost("/application/plans", botUser) { context, query: ApplicationScopedQuery ->
            if (context.organization == query.namespace) {
                TestPlanService.getTestPlansByNamespaceAndNlpModel(query.namespace, query.applicationName)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/bot/intent/new", botUser) { context, query: CreateBotIntentRequest ->
            BotAdminService.createBotIntent(context.organization, query) ?: unauthorized()
        }

        blockingJsonPost("/bot/intent", botUser) { context, query: UpdateBotIntentRequest ->
            BotAdminService.updateBotIntent(context.organization, query) ?: unauthorized()
        }

        blockingJsonPost("/bot/intents/search", botUser) { context, request: BotIntentSearchRequest ->
            if (context.organization == request.namespace) {
                BotAdminService.loadBotIntents(request)
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete("/bot/intent/:intentId", botUser) { context ->
            BotAdminService.deleteBotIntent(context.organization, context.pathParam("intentId"))
        }

        blockingJsonGet("/i18n", botUser) { context ->
            i18n.getLabels().filter { it.namespace == context.organization }
        }

        blockingJsonPost("/i18n/complete", botUser) { context, labels: List<I18nLabel> ->
            Translator.completeAllLabels(labels.filter { it.namespace == context.organization })
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
            mapper.writeValueAsString(i18n.getLabels().filter { it.namespace == context.organization })
        }

        blockingUploadPost("/i18n/import/json", botUser) { context, content ->
            val labels: List<I18nLabel> = mapper.readValue(content)
            i18n.save(labels.filter { it.namespace == context.organization })
        }

        blockingJsonGet("/connectorTypes", botUser) {
            ConnectorType.connectorTypes
        }

        configureStaticHandling()
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