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
import ai.tock.bot.bean.TickStory
import ai.tock.nlp.admin.AdminVerticle
import ai.tock.shared.injector
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole
import com.github.salomonbrys.kodein.instance
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging



class StoryVerticle {

    private val logger: KLogger = KotlinLogging.logger {}
    private val storyService: StoryService by injector.instance()

    private val baseURL = "/bot"
    private val storyURL = "$baseURL/story"
    private val tickURL = "$storyURL/tick"


    /**
     * Declaration of routes and association to the appropriate handler
     */
    fun configure(adminVerticle: AdminVerticle) {
        logger.info { "configure ScenarioVerticle" }

        with(adminVerticle) {
            blockingJsonPost(tickURL, setOf(TockUserRole.botUser), handler = createTickStory)

            blockingJsonDelete(
                "$storyURL/:storyDefinitionConfigurationId",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("Delete Story", { it.path("StoryDefinitionConfigurationId") }),
                handler = deleteStoryByStoryDefinitionConfigurationId
            )

            // TODO : migrate all story vert.x handlers to here  (ex BotAdminVerticle)
        }
    }

    private val createTickStory: (RoutingContext, TickStory) -> Unit = { context, tickStory ->
        logger.debug { "request to create tick story <${tickStory.storyId}>" }
        val namespace = (context.user() as TockUser).namespace
        storyService.createTickStory(namespace, tickStory)
    }

    private val deleteStoryByStoryDefinitionConfigurationId: (RoutingContext) -> Boolean = { context ->
        val namespace = (context.user() as TockUser).namespace
        storyService.deleteStoryByStoryDefinitionConfigurationId(namespace, context.pathParam("storyId"))
    }

}