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

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.model.BotDialogRequest
import fr.vsct.tock.bot.admin.model.DialogReportRequest
import fr.vsct.tock.bot.admin.model.UserSearchQuery
import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.TestPlanService
import fr.vsct.tock.nlp.admin.AdminVerticle
import fr.vsct.tock.nlp.admin.model.ApplicationScopedQuery
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

/**
 *
 */
class BotAdminVerticle : AdminVerticle(KotlinLogging.logger {}) {

    override fun configure() {
        configureServices()

        blockingJsonPost("/users/search") { context, query: UserSearchQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.searchUsers(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/user/dialogs") { context, query: DialogReportRequest ->
            //TODO check rights
            if (context.organization == query.namespace) {
                BotAdminService.lastDialog(query.playerId)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/configuration/bots") { context, query: ApplicationScopedQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.getApplicationConfigurations(query.namespace, query.applicationName)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/configuration/bot") { context, bot: BotApplicationConfiguration ->
            if (context.organization == bot.namespace) {
                if (bot._id != null) {
                    val conf = BotAdminService.getApplicationConfigurationById(bot._id!!)
                    if (conf == null || bot.namespace != conf.namespace || bot.botId != conf.botId || bot.applicationId != conf.applicationId) {
                        unauthorized()
                    }
                } else {
                    if (BotAdminService.getConfigurationByApplicationIdAndBotId(bot.applicationId, bot.botId) != null) {
                        unauthorized()
                    }
                }
                BotAdminService.saveApplicationConfiguration(bot)
            } else {
                unauthorized()
            }
        }

        blockingJsonDelete("/configuration/bot/:confId") { context ->
            BotAdminService.getApplicationConfigurationById(context.pathParam("confId"))
                    ?.let {
                        if (context.organization == it.namespace) {
                            BotAdminService.deleteApplicationConfiguration(it)
                            true
                        } else {
                            null
                        }
                    } ?: unauthorized()
        }

        blockingJsonPost("/test/talk") { context, query: BotDialogRequest ->
            if (context.organization == query.namespace) {
                BotAdminService.talk(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonGet("/test/plans") { context ->
            TestPlanService.getTestPlansByNamespace(context.organization)
        }

        blockingJsonGet("/test/plan/:planId/executions") { context ->
            TestPlanService.getPlanExecutions(context.loadTestPlan())
        }

        blockingJsonPost("/test/plan") { context, plan: TestPlan ->
            if (context.organization == plan.namespace) {
                TestPlanService.saveTestPlan(plan)
            } else {
                unauthorized()
            }
        }

        blockingDelete("/test/plan/:planId") { context ->
            TestPlanService.removeTestPlan(context.loadTestPlan())
        }

        blockingJsonPost("/test/plan/:planId/dialog/:dialogId") { context, _: ApplicationScopedQuery ->
            TestPlanService.addDialogToTestPlan(context.loadTestPlan(), context.pathParam("dialogId"))
        }

        blockingJsonPost("/test/plan/:planId/dialog/delete/:dialogId") { context, _: ApplicationScopedQuery ->
            TestPlanService.removeDialogFromTestPlan(
                    context.loadTestPlan(),
                    context.pathParam("dialogId"))
        }

        blockingJsonPost("/test/plan/:planId/run") { context, _: ApplicationScopedQuery ->
            context.loadTestPlan().run {
                TestPlanService.runTestPlan(
                        BotAdminService.getRestClient(BotAdminService.getBotConfiguration(botApplicationConfigurationId, namespace)),
                        this
                )
            }
        }

        blockingJsonGet("/application/:applicationId/plans") { context ->
            val applicationId = context.pathParam("applicationId")
            TestPlanService.getTestPlansByApplication(applicationId).filter { it.namespace == context.organization }
        }

        blockingJsonPost("/application/plans") { context, query: ApplicationScopedQuery ->
            if (context.organization == query.namespace) {
                TestPlanService.getTestPlansByNamespaceAndNlpModel(query.namespace, query.applicationName)
            } else {
                unauthorized()
            }
        }

        configureStaticHandling()
    }

    fun RoutingContext.loadTestPlan(): TestPlan {
        return TestPlanService.getTestPlan(pathParam("planId"))?.run {
            if (organization != namespace) {
                unauthorized()
            } else {
                this
            }
        } ?: notFound()
    }
}