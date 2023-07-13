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

package ai.tock.bot.engine.config.tickstory

import ai.tock.bot.admin.answer.TickAnswerConfiguration
import ai.tock.bot.admin.story.StoryDefinitionAnswersContainer
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.bean.TickSession
import ai.tock.bot.bean.TickUserAction
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.TickState
import ai.tock.bot.processor.Redirect
import ai.tock.bot.processor.TickStoryProcessor
import mu.KotlinLogging
import java.time.Instant

/**
 * Handler of a tick story answer
 */
object TickAnswerHandler {

    private val logger = KotlinLogging.logger {}

    internal fun handle(
        botBus: BotBus,
        container: StoryDefinitionAnswersContainer,
        configuration: TickAnswerConfiguration,
        redirectFn : (String) -> Unit,
    ) {
        with(botBus) {
            val intentName = botBus.currentIntent?.intentWithoutNamespace()?.name!!
            val story = container as StoryDefinitionConfiguration
            val storyId = story._id.toString()
            val endingStoryRuleExists = story.findEnabledEndWithStoryId(applicationId) != null

            // Get a stored tick state. Start a new session if it doesn't exist
            val tickSession = initTickSession(dialog, storyId, connectorData.conversationData)

            // Call the tick story processor
            val processingResult =
                TickStoryProcessor(
                    tickSession,
                    configuration.toTickConfiguration(),
                    TickSenderBotBus(botBus),
                    endingStoryRuleExists
                ).process(
                    TickUserAction(
                        intentName,
                        parseEntities(entities, tickSession.initDate))
                )

            // update tick state dialog
            updateDialog(dialog, storyId, processingResult.session)

            // Redirect to new story if processingResult is Redirect
            if(processingResult is Redirect) redirectFn(processingResult.storyId)
        }
    }

    /**
     * If action is final, then remove a given tick story from a Dialog
     * else update a tick state
     */
    private fun updateDialog(dialog: Dialog, storyId: String, tickSession: TickSession) {
        logger.debug { "updating dialog tick state with session data... " }
        dialog.tickStates[storyId] =
            with(tickSession) {
                TickState(
                    currentState!!,
                    contexts,
                    ranHandlers,
                    objectivesStack,
                    initDate,
                    unknownHandlingStep,
                    handlingStep,
                    finished
                )
            }
    }

    /**
     * Initialize a tick session
     */
    fun initTickSession(dialog: Dialog, storyId: String, conversationData: Map<String, String>): TickSession {
        val tickState = dialog.tickStates[storyId]
        return with(tickState) {
            if (this == null || finished) {
                logger.debug { "start a new session... " }
                TickSession(initDate = dialog.lastDateUpdate, contexts = conversationData)
            } else {
                logger.debug { "continue the session already started..." }
                var sessionCtx = contexts
                conversationData.forEach { (t, u) ->
                    if (!contexts.containsKey(t))
                        sessionCtx = contexts + (t to u)
                }
                TickSession(currentState, sessionCtx, ranHandlers, objectivesStack, initDate, unknownHandlingStep, lastExecutedAction)
            }
        }
    }
    /**
     * Feed the contexts with the [entities][Map<String, EntityStateValue>] provided from the initialization date
     */
    private fun parseEntities(entities: Map<String, EntityStateValue>, initDate: Instant): Map<String, String?> {
        return entities
            .filterValues { it.lastUpdate.isAfter(initDate) }
            .mapValues { it.value.value?.content }
    }
}