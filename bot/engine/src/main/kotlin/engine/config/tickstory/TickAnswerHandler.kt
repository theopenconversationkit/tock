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
import ai.tock.bot.processor.Success
import ai.tock.bot.processor.TickStoryProcessor
import java.time.Instant

object TickAnswerHandler {

    internal fun handle(
        botBus: BotBus,
        container: StoryDefinitionAnswersContainer,
        configuration: TickAnswerConfiguration,
        redirectFn : (String) -> Unit,
    ) {
        with(botBus) {
            val intentName = botBus.currentIntent?.intentWithoutNamespace()?.name!!
            val story = container as StoryDefinitionConfiguration
            val endingStoryRuleExists = story.findEnabledEndWithStoryId(applicationId) != null

            // Get a stored tick state. Start a new session if it doesn't exist
            val tickSession = initTickSession(dialog, story._id.toString())

            // Call the tick story processor
            val result =
                TickStoryProcessor(
                    tickSession,
                    configuration.toTickConfiguration(),
                    TickSenderBotBus(botBus),
                    endingStoryRuleExists
                ).process(
                    TickUserAction(
                        intentName,
                        parseEntities(entities, tickSession.init))
                )

            when (result) {
                is Success -> updateDialog(dialog, result.isFinal, story._id.toString(), result.session)
                is Redirect -> result.storyId?.let { redirectFn(it) }
            }

        }
    }

    /**
     * If action is final, then remove a given tick story from a Dialog
     * else update a tick state
     */
    private fun updateDialog(dialog: Dialog, isFinal: Boolean, storyId: String, tickSession: TickSession) {
        dialog.tickStates[storyId] = TickState(
            tickSession.currentState!!,
            tickSession.contexts,
            tickSession.ranHandlers,
            tickSession.objectivesStack,
            tickSession.init,
            tickSession.unknownHandlingStep,
            isFinal
        )
    }

    /**
     * Initialize a tick session
     */
    private fun initTickSession(dialog: Dialog, storyId: String): TickSession {
        val tickState = dialog.tickStates[storyId]
        return with(tickState) {
            if (this == null) {
                TickSession(init = dialog.lastDateUpdate)
            } else if (finished) {
                TickSession(init = dialog.lastDateUpdate)
            } else {
                TickSession(currentState, contexts, ranHandlers, objectivesStack, init, unknownHandlingStep)
            }
        }
    }
    /**
     * Feed the contexts by the entities provided from the initialization date
     */
    private fun parseEntities(entities: Map<String, EntityStateValue>, init: Instant): Map<String, String?> {
        return entities
            .filterValues { it.lastUpdate.isAfter(init) }
            .mapValues { it.value.value?.content }
    }
}