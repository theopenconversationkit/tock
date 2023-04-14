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

import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.service.BotAdminService
import ai.tock.nlp.front.client.FrontClient
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler


class BotVerticle : ChildVerticle<AdminException>{
    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {

            blockingJsonGet("/bots/:botId", setOf(
                TockUserRole.botUser,
                TockUserRole.faqNlpUser,
                TockUserRole.faqBotUser
            ),
                handler = toRequestHandler { context ->
                    BotAdminService.getBots(context.organization, context.path("botId"))
                })

            blockingJsonPost(
                "/bot", TockUserRole.admin,
                logger = requestLogger<BotConfiguration, AdminException>(" Create or Update Bot Configuration") { _, c ->
                    c?.let { FrontClient.getApplicationByNamespaceAndName(it.namespace, it.nlpModel)?._id }
                },
                handler = toRequestHandler { context, bot: BotConfiguration ->
                    if (context.organization == bot.namespace) {
                        BotAdminService.save(bot)
                    } else {
                        WebVerticle.unauthorized()
                    }
                })
        }
    }

}