/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

package engine.config

import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.config.ProactiveConversationStatus
import ai.tock.bot.engine.config.ProactiveConversationStatus.*
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.Executor
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging

private const val PROACTIVE_CONVERSATION_STATUS: String = "PROACTIVE_CONVERSATION_STATUS"
private val executor: Executor by injector.instance()
private val userTimelineDAO: UserTimelineDAO by injector.instance()
private val logger = KotlinLogging.logger {}

interface AbstractProactiveAnswerHandler {

    fun handleProactiveAnswer(botBus: BotBus): StoryDefinition? = null
    fun handle(botBus: BotBus) {
        with(botBus) {
            startProactiveConversation()
            executor.executeBlocking {
                val noAnswerStory = handleProactiveAnswer(this)

                if (noAnswerStory != null) {
                    logger.info { "No-answer story detected → switching without flushing (already handled in handleAndSwitchStory)" }

                    // Mark the conversation as closed
                    setBusContextValue(PROACTIVE_CONVERSATION_STATUS, CLOSED)

                    // Switch to the story
                    logger.info { "Run the story intent=${noAnswerStory.mainIntent()}, id=${noAnswerStory.id}" }
                    handleAndSwitchStory(noAnswerStory, noAnswerStory.mainIntent())
                } else {
                    // Standard case : flush & clean properly
                    logger.info { "Handling standard proactive answer : flush + end" }
                    flushProactiveConversation()
                    endProactiveConversation()
                }

                // Save the dialog
                if (connectorData.saveTimeline) {
                    userTimelineDAO.save(userTimeline, botDefinition)
                }
            }
        }
    }

    private fun BotBus.startProactiveConversation() {
        if(getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS) == null) {
            setBusContextValue(PROACTIVE_CONVERSATION_STATUS, LUNCHED)
            if(underlyingConnector.startProactiveConversation(connectorData.callback, this)){
                setBusContextValue(PROACTIVE_CONVERSATION_STATUS, STARTED)
            }
        }
    }

    fun BotBus.flushProactiveConversation() {
        if(getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS) == STARTED) {
            underlyingConnector.flushProactiveConversation(connectorData.callback, connectorData.metadata)
        }
    }

    private fun BotBus.endProactiveConversation() {
        if(getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS) == STARTED) {
            setBusContextValue(PROACTIVE_CONVERSATION_STATUS, CLOSED)
            underlyingConnector.endProactiveConversation(connectorData.callback, connectorData.metadata)
        } else {
            end()
        }
    }
}