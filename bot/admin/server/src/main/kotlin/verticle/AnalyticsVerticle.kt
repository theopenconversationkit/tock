/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.admin.verticle

import ai.tock.bot.admin.model.DialogFlowRequest
import ai.tock.bot.admin.service.BotAdminAnalyticsService
import ai.tock.nlp.admin.model.ApplicationScopedQuery
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler
import io.vertx.ext.web.RoutingContext


class AnalyticsVerticle : ChildVerticle<AdminException>{

    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {

            fun <R> measureTimeMillis(context: RoutingContext, function: () -> R): R {
                val before = System.currentTimeMillis()
                val result = function()
                logger.debug { "${context.normalizedPath()} took ${System.currentTimeMillis() - before} ms." }
                return result
            }

            fun <R> checkAndMeasure(context: RoutingContext, request: ApplicationScopedQuery, function: () -> R): R =
                if (context.organization == request.namespace) {
                    measureTimeMillis(context){
                        function()
                    }
                } else {
                    WebVerticle.unauthorized()
                }

            blockingJsonPost(
                "/analytics/messages",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByType(request)
                    }
                })

            blockingJsonPost(
                "/analytics/users",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportUsersByType(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byConnectorType",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.countMessagesByConnectorType(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byConfiguration",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByConfiguration(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byConnectorType",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByConnectorType(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byDayOfWeek",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByDayOfWeek(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byHour",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByHour(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byIntent",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByIntent(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byDateAndIntent",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByDateAndIntent(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byDateAndStory",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByDateAndStory(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byStory",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByStory(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byStoryCategory",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByStoryCategory(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byStoryType",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByStoryType(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byStoryLocale",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByStoryLocale(request)
                    }
                })

            blockingJsonPost(
                "/analytics/messages/byActionType",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: DialogFlowRequest ->
                    checkAndMeasure(context, request) {
                        BotAdminAnalyticsService.reportMessagesByActionType(request)
                    }
                })

        }
    }

}