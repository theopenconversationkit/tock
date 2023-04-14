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

import ai.tock.bot.admin.model.Feature
import ai.tock.bot.admin.service.BotAdminService
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler
import com.fasterxml.jackson.module.kotlin.readValue


class FeatureVerticle : ChildVerticle<AdminException>{

    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {
            blockingJsonGet("/feature/:applicationId", setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context ->
                    val applicationId = context.path("applicationId")
                    BotAdminService.getFeatures(applicationId, context.organization)
                })

            blockingPost(
                "/feature/:applicationId/toggle",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("Toogle Application Feature", { it.body().asString() }),
                handler = toRequestHandler { context ->
                    val applicationId = context.path("applicationId")
                    val body = context.body().asString()
                    val feature: Feature = mapper.readValue(body)
                    BotAdminService.toggleFeature(applicationId, context.organization, feature)
                })

            blockingPost(
                "/feature/:applicationId/update",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("Update Application Feature", { it.body().asString() }),
                handler = toRequestHandler { context ->
                    val applicationId = context.path("applicationId")
                    val body = context.body().asString()
                    val feature: Feature = mapper.readValue(body)
                    BotAdminService.updateDateAndEnableFeature(
                        applicationId,
                        context.organization,
                        feature
                    )
                })

            blockingPost(
                "/feature/:applicationId/add",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("Create Application Feature", { it.body().asString() }),
                handler = toRequestHandler { context ->
                    val applicationId = context.path("applicationId")
                    val body = context.body().asString()
                    val feature: Feature = mapper.readValue(body)
                    BotAdminService.addFeature(applicationId, context.organization, feature)
                })

            blockingDelete(
                "/feature/:botId/:category/:name/",
                TockUserRole.botUser,
                simpleLogger(
                    "Delete Application Feature",
                    { listOf(it.path("botId"), it.path("category"), it.path("name")) }
                ),
                handler = toRequestHandler { context ->
                    val category = context.path("category")
                    val name = context.path("name")
                    val botId = context.path("botId")
                    BotAdminService.deleteFeature(botId, context.organization, category, name, null)
                })

            blockingDelete(
                "/feature/:botId/:category/:name/:applicationId",
                TockUserRole.botUser,
                simpleLogger(
                    "Delete Application Feature",
                    { listOf(it.path("botId"), it.path("category"), it.path("name"), it.path("applicationId")) }
                ),
                handler = toRequestHandler { context ->
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
                })
        }
    }

}