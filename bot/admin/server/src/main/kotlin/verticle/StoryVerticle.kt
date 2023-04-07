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

import ai.tock.bot.admin.service.StoryService
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
    override fun configure(verticle: WebVerticle<AdminException>) {
        logger.info { "configure StoryVerticle" }

        with(verticle) {

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
        }
            // TODO : migrate all story vert.x handlers to here  (ex BotAdminVerticle)
    }
}