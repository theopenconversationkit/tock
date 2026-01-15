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
import ai.tock.bot.engine.config.ProactiveConversationStatus.CLOSED
import ai.tock.bot.engine.config.ProactiveConversationStatus.LUNCHED
import ai.tock.bot.engine.config.ProactiveConversationStatus.STARTED
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.Executor
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private const val PROACTIVE_CONVERSATION_STATUS: String = "PROACTIVE_CONVERSATION_STATUS"
private val executor: Executor by injector.instance()
private val userTimelineDAO: UserTimelineDAO by injector.instance()
private val logger = KotlinLogging.logger {}

interface AbstractProactiveAnswerHandler {
    fun handleProactiveAnswer(botBus: BotBus): StoryDefinition? = null

    fun handle(botBus: BotBus) {
        with(botBus) {
            val userId = userId.id
            logger.debug { "handle(): userId=$userId, BEFORE startProactiveConversation, thread=${Thread.currentThread().name}" }
            startProactiveConversation()
            val statusAfterStart = getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS)
            logger.debug { "handle(): userId=$userId, AFTER startProactiveConversation, status=$statusAfterStart, thread=${Thread.currentThread().name}" }

            executor.executeBlocking {
                logger.debug { "handle(): userId=$userId, INSIDE executeBlocking, thread=${Thread.currentThread().name}" }
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
                    val statusBeforeFlush = getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS)
                    logger.info { "Handling standard proactive answer : flush + end" }
                    logger.debug { "handle(): userId=$userId, BEFORE flush, status=$statusBeforeFlush, thread=${Thread.currentThread().name}" }
                    flushProactiveConversation()
                    logger.debug { "handle(): userId=$userId, AFTER flush, BEFORE endProactiveConversation, thread=${Thread.currentThread().name}" }
                    endProactiveConversation()
                    logger.debug { "handle(): userId=$userId, AFTER endProactiveConversation, thread=${Thread.currentThread().name}" }
                }

                // Save the dialog
                if (connectorData.saveTimeline) {
                    runBlocking {
                        userTimelineDAO.save(userTimeline, botDefinition)
                    }
                }
            }
            logger.debug { "handle(): userId=$userId, AFTER executeBlocking (returned immediately if async), thread=${Thread.currentThread().name}" }
        }
    }

    private fun BotBus.startProactiveConversation() {
        val currentStatus = getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS)
        logger.debug { "startProactiveConversation(): userId=${userId.id}, currentStatus=$currentStatus" }
        if (currentStatus == null) {
            setBusContextValue(PROACTIVE_CONVERSATION_STATUS, LUNCHED)
            val result = underlyingConnector.startProactiveConversation(connectorData.callback, this)
            logger.debug { "startProactiveConversation(): userId=${userId.id}, connectorResult=$result" }
            if (result) {
                setBusContextValue(PROACTIVE_CONVERSATION_STATUS, STARTED)
            }
        }
    }

    fun BotBus.flushProactiveConversation() {
        val status = getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS)
        logger.debug { "flushProactiveConversation(): userId=${userId.id}, status=$status, willFlush=${status == STARTED}" }
        if (status == STARTED) {
            underlyingConnector.flushProactiveConversation(connectorData.callback, connectorData.metadata)
            logger.debug { "flushProactiveConversation(): userId=${userId.id}, DONE" }
        }
    }

    private fun BotBus.endProactiveConversation() {
        val status = getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS)
        logger.debug { "endProactiveConversation(): userId=${userId.id}, status=$status, willCallConnector=${status == STARTED}, willCallEnd=${status != STARTED}" }
        if (status == STARTED) {
            setBusContextValue(PROACTIVE_CONVERSATION_STATUS, CLOSED)
            underlyingConnector.endProactiveConversation(connectorData.callback, connectorData.metadata)
            logger.debug { "endProactiveConversation(): userId=${userId.id}, connector.endProactiveConversation DONE (bus.end NOT called)" }
        } else {
            logger.debug { "endProactiveConversation(): userId=${userId.id}, calling bus.end()" }
            end()
        }
    }
}
