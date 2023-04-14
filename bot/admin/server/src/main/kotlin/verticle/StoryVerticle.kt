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

import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.model.CreateStoryRequest
import ai.tock.bot.admin.model.StorySearchRequest
import ai.tock.bot.admin.service.BotAdminService
import ai.tock.bot.admin.service.StoryService
import ai.tock.bot.admin.story.dump.StoryDefinitionConfigurationDump
import ai.tock.nlp.front.client.FrontClient
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

/**
 * [StoryVerticle] contains all the routes and actions associated with the stories
 */
class StoryVerticle: ChildVerticle<AdminException> {

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}

        private const val baseURL = "/bot"
        private const val storyURL = "$baseURL/story"
    }

    /**
     * Declaration of routes and association to the appropriate handler
     */
    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {

            val deleteStoryByStoryDefinitionConfigurationId: (RoutingContext) -> Boolean = { context ->
                val storyDefinitionConfigurationId = context.pathParam("storyDefinitionConfigurationId")
                Companion.logger.debug { "request to delete story <$storyDefinitionConfigurationId>" }
                val namespace = (context.user() as TockUser).namespace
                StoryService.deleteStoryByNamespaceAndStoryDefinitionConfigurationId(namespace, storyDefinitionConfigurationId)
            }

            blockingJsonDelete(
                "$storyURL/:storyDefinitionConfigurationId",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler(deleteStoryByStoryDefinitionConfigurationId)
            )

            blockingJsonGet("/bot/story/:appName/export", TockUserRole.botUser, handler= toRequestHandler { context ->
                BotAdminService.exportStories(context.organization, context.path("appName"))
            })

            blockingJsonGet("/bot/story/:appName/export/:storyConfigurationId",
                TockUserRole.botUser, handler= toRequestHandler { context ->
                val exportStory = BotAdminService.exportStory(
                    context.organization,
                    context.path("appName"),
                    context.path("storyConfigurationId")
                )
                exportStory?.let { listOf(it) } ?: emptyList()
            })

            blockingJsonPost(
                "/bot/story/load",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: StorySearchRequest ->
                    if (context.organization == request.namespace) {
                        BotAdminService.loadStories(request)
                    } else {
                        WebVerticle.unauthorized()
                    }
                })

            blockingJsonPost("/bot/story/search",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, request: StorySearchRequest ->
                    if (context.organization == request.namespace) {
                        BotAdminService.searchStories(request)
                    } else {
                        WebVerticle.unauthorized()
                    }
                })

            blockingJsonGet("/bot/story/:storyId",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context ->
                    BotAdminService.findStory(context.organization, context.path("storyId"))
                })

            blockingJsonGet("/bot/story/:botId/settings",
                TockUserRole.botUser,
                handler = toRequestHandler { context ->
                    BotAdminService.findRuntimeStorySettings(context.organization, context.path("botId"))
                })

            blockingJsonGet("/bot/story/:botId/:intent",
                TockUserRole.botUser,
                handler = toRequestHandler { context ->
                    BotAdminService.findConfiguredStoryByBotIdAndIntent(
                        context.organization,
                        context.path("botId"),
                        context.path("intent")
                    )
                })

            blockingJsonPost(
                "/bot/story/new",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                requestLogger<CreateStoryRequest, AdminException>("Create Story") { context, r ->
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
                },
                handler = toRequestHandler { context, query: CreateStoryRequest ->
                    BotAdminService.createStory(context.organization, query, context.userLogin) ?: WebVerticle.unauthorized()
                })

            blockingUploadJsonPost(
                "/bot/story/:appName/:locale/import",
                TockUserRole.botUser,
                simpleLogger("JSON Import Response Labels"),
                handler = toRequestHandler { context, stories: List<StoryDefinitionConfigurationDump> ->
                    BotAdminService.importStories(
                        context.organization,
                        context.path("appName"),
                        context.pathToLocale("locale"),
                        stories,
                        context.userLogin
                    )
                })

            blockingJsonPost(
                "/bot/story",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                requestLogger<BotStoryDefinitionConfiguration, AdminException>("Update Story") { context, r ->
                    r?.let { s ->
                        BotAdminService.getBotConfigurationsByNamespaceAndBotId(context.organization, s.botId)
                            .firstOrNull()
                            ?.let {
                                FrontClient.getApplicationByNamespaceAndName(
                                    context.organization,
                                    it.nlpModel
                                )?._id
                            }
                    }
                },
                handler = toRequestHandler { context, story: BotStoryDefinitionConfiguration ->
                    BotAdminService.saveStory(context.organization, story, context.userLogin) ?: WebVerticle.unauthorized()
                })
        }

    }
}