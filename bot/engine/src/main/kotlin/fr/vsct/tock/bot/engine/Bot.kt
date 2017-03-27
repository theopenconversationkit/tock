/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendLocation
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.State
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.shared.error
import ft.vsct.tock.nlp.api.client.NlpClient
import ft.vsct.tock.nlp.api.client.model.EntityValue
import ft.vsct.tock.nlp.api.client.model.NlpQuery
import ft.vsct.tock.nlp.api.client.model.QueryContext
import ft.vsct.tock.nlp.api.client.model.QueryState
import mu.KotlinLogging
import java.time.ZonedDateTime

/**
 *
 */
class Bot(val botDefinition: BotDefinition) {

    private val logger = KotlinLogging.logger {}

    private val nlpClient = NlpClient()

    fun handle(action: Action, userTimeline: UserTimeline, connector: ConnectorController) {
        val dialog = getDialog(action, userTimeline)
        parseAction(action, userTimeline, dialog, connector)

        if (botDefinition.isEnabledIntent(action.state.currentIntent)) {
            logger.debug { "Enable bot for $action" }
            userTimeline.userState.botDisabled = false
        }

        if (!userTimeline.userState.botDisabled) {
            connector.startTypingAnswer(action)
            dialog.apply {
                state.currentIntent = action.state.currentIntent
                state.mergeEntityValues(action)
            }
            val story = getStory(action, dialog)
            story.actions.add(action)

            val bus = BotBus(connector, userTimeline, dialog, story, action)
            bus.loadProfileIfNotSet()

            story.handle(bus)
        } else {
            logger.debug { "bot is disabled" }
        }
    }

    private fun getDialog(action: Action, userTimeline: UserTimeline): Dialog {
        return if (userTimeline.currentDialog() == null) {
            val newDialog = Dialog(setOf(userTimeline.playerId, action.recipientId))
            userTimeline.dialogs.add(newDialog)
            newDialog
        } else {
            userTimeline.currentDialog()!!
        }
    }

    private fun getStory(action: Action, dialog: Dialog): Story {
        val newIntent = dialog.state.currentIntent
        val storyDefinition = botDefinition.findStoryDefinition(newIntent?.name)
        val previousStory = dialog.stories.lastOrNull()
        val story = if (previousStory == null || previousStory.currentIntent != newIntent) {
            val newStory = Story(storyDefinition, newIntent)
            dialog.stories.add(newStory)
            newStory
        } else {
            previousStory
        }
        story.actions.add(action)
        return story
    }

    private fun parseAction(action: Action,
                            userTimeline: UserTimeline,
                            dialog: Dialog,
                            connector: ConnectorController) {
        when (action) {
            is SendChoice -> {
                // do nothing
            }
            is SendLocation -> {
                //do nothing
            }
            is SendAttachment -> {
                //do nothing
            }
            is SendSentence -> {
                parseSentence(action, userTimeline, dialog, connector)
            }
            else -> logger.warn { "${action::class.simpleName} not yet supported" }
        }
    }


    private fun parseSentence(sentence: SendSentence,
                              userTimeline: UserTimeline,
                              dialog: Dialog,
                              connector: ConnectorController) {

        fun toNlpQuery(): NlpQuery {
            return NlpQuery(
                    listOf(sentence.text!!),
                    botDefinition.namespace,
                    botDefinition.nlpApplication,
                    QueryContext(
                            userTimeline.userPreferences.locale,
                            sentence.playerId.id,
                            dialog.id,
                            connector.connectorType.toString(),
                            referenceDate = ZonedDateTime.now(userTimeline.userPreferences.timezone),
                            engineType = botDefinition.engineType
                    ),
                    QueryState.noState)
        }

        logger.debug { "Parse sentence : $sentence" }
        if (userTimeline.currentDialog()?.isWaitingForInput() ?: false
                && sentence.text.isNullOrBlank()) {
            //do nothing
        } else {
            try {
                logger.debug { "Sending sentence '${sentence.text}' to NLP" }
                val nlpResult = nlpClient.parse(toNlpQuery()).body()
                sentence.state.currentIntent = botDefinition.findIntent(nlpResult.intent)
                sentence.state.copyEntityValues(sentence, nlpResult.entities)
            } catch(t: Throwable) {
                logger.error(t)
            }
        }
    }

    private fun State.copyEntityValues(action: Action, values: List<EntityValue>) {
        this.entityValues.putAll(
                values.groupBy { it.entity.role }
                        .mapValues {
                            when (it.value.size) {
                                1 -> EntityStateValue(action, it.value.first())
                                else -> mergeEntityValues(action, it.value)
                            }
                        }
        )
    }

    private fun mergeEntityValues(action: Action, newValues: List<EntityValue>, oldValues: EntityStateValue? = null): EntityStateValue {
        //TODO merge dates
        return if (oldValues == null) {
            EntityStateValue(action, newValues.first())
        } else {
            val newValue = newValues.first()
            oldValues.value = newValue
            oldValues.history.add(EntityStateValue.ArchivedEntityValue(newValue, action))
            oldValues
        }
    }

    private fun State.mergeEntityValues(action: Action) {
        entityValues.putAll(
                action.state.entityValues
                        .mapValues {
                            mergeEntityValues(action, listOf(it.value.value), entityValues.get(it.key))
                        }
        )
    }

    fun errorActionFor(userAction: Action): Action {
        return botDefinition.errorActionFor(userAction)
    }

}