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

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendLocation
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.config.BotDefinitionWrapper
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.nlp.NlpController
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.shared.injector
import mu.KotlinLogging

/**
 *
 */
class Bot(botDefinitionBase: BotDefinition) {

    private val logger = KotlinLogging.logger {}

    private val nlp: NlpController by injector.instance()

    internal val botDefinition: BotDefinitionWrapper = BotDefinitionWrapper(botDefinitionBase)

    fun handle(action: Action, userTimeline: UserTimeline, connector: ConnectorController) {
        val realConnector = connector as TockConnectorController
        loadProfileIfNotSet(action, userTimeline, connector)

        val dialog = getDialog(action, userTimeline)

        parseAction(action, userTimeline, dialog, realConnector)

        if (botDefinition.isEnabledIntent(dialog.state.currentIntent)) {
            logger.debug { "Enable bot for $action" }
            userTimeline.userState.botDisabled = false
        }

        if (!userTimeline.userState.botDisabled) {
            realConnector.startTypingInAnswerTo(action)
            val story = getStory(action, dialog)
            val bus = TockBotBus(realConnector, userTimeline, dialog, story, action, botDefinition)

            story.handle(bus)
        } else {
            //refresh intent flag
            userTimeline.userState.botDisabled = true
            logger.debug { "bot is disabled" }
        }
    }

    private fun getDialog(action: Action, userTimeline: UserTimeline): Dialog {
        return userTimeline.currentDialog() ?: createDialog(action, userTimeline)
    }

    private fun createDialog(action: Action, userTimeline: UserTimeline): Dialog {
        val newDialog = Dialog(setOf(userTimeline.playerId, action.recipientId))
        userTimeline.dialogs.add(newDialog)
        return newDialog
    }

    private fun getStory(action: Action, dialog: Dialog): Story {
        val newIntent = dialog.state.currentIntent
        val previousStory = dialog.currentStory()

        val story =
                if (previousStory == null
                        || (newIntent != null && !previousStory.definition.supportIntent(newIntent))) {
                    val storyDefinition = botDefinition.findStoryDefinition(newIntent?.name)
                    val newStory = Story(
                            storyDefinition,
                            if (newIntent != null && storyDefinition.isStarterIntent(newIntent)) newIntent
                            else storyDefinition.mainIntent())
                    dialog.stories.add(newStory)
                    newStory
                } else {
                    previousStory
                }

        //set current step if necessary
        if (action is SendChoice) {
            action.step()?.apply {
                story.currentStep = this
            }
        }

        //revalidate step
        story.currentStep = story.findCurrentStep()?.name

        story.actions.add(action)
        return story
    }

    private fun parseAction(action: Action,
                            userTimeline: UserTimeline,
                            dialog: Dialog,
                            connector: TockConnectorController) {
        try {
            when (action) {
                is SendChoice -> {
                    parseChoice(action, dialog)
                }
                is SendLocation -> {
                    parseLocation(action, dialog)
                }
                is SendAttachment -> {
                    parseAttachment(action, dialog)
                }
                is SendSentence -> {
                    if (!userTimeline.userState.waitingRawInput && !action.text.isNullOrBlank()) {
                        nlp.parseSentence(action, userTimeline, dialog, connector, botDefinition)
                    }
                }
                else -> logger.warn { "${action::class.simpleName} not yet supported" }
            }
        } finally {
            //reinitialize lastActionState
            dialog.state.nextActionState = null
        }
    }

    private fun parseAttachment(attachment: SendAttachment, dialog: Dialog) {
        botDefinition.handleAttachmentStory?.let {
            it.mainIntent().let {
                dialog.state.currentIntent = it
            }
        }
    }


    private fun parseLocation(location: SendLocation, dialog: Dialog) {
        botDefinition.userLocationStory?.let {
            it.mainIntent().let {
                dialog.state.currentIntent = it
            }
        }
    }

    private fun parseChoice(choice: SendChoice, dialog: Dialog) {
        botDefinition.findIntent(choice.intentName).let { intent ->
            //restore state if it's possible (old dialog choice case)
            if (intent != Intent.unknown) {
                val previousIntentName = choice.previousIntent()
                if (previousIntentName != null) {
                    val previousStory = botDefinition.findStoryDefinition(previousIntentName)
                    if (previousStory != botDefinition.unknownStory && previousStory.supportIntent(intent)) {
                        //the previous intent is a primary intent that support the new intent
                        val storyDefinition = botDefinition.findStoryDefinition(choice.intentName)
                        if (storyDefinition == botDefinition.unknownStory) {
                            //the new intent is a secondary intent, may be we need to create a intermediate story
                            val currentStory = dialog.currentStory()
                            if (currentStory == null
                                    || !currentStory.definition.supportIntent(intent)
                                    || !currentStory.definition.supportIntent(botDefinition.findIntent(previousIntentName))) {
                                dialog.stories.add(Story(previousStory, intent))
                            }
                        }
                    }
                }
            }
            dialog.state.currentIntent = intent
        }
    }

    internal fun errorActionFor(userAction: Action): Action {
        return botDefinition.errorActionFor(userAction)
    }

    private fun loadProfileIfNotSet(action: Action, userTimeline: UserTimeline, connector: TockConnectorController) {
        with(userTimeline) {
            if (!userState.profileLoaded) {
                val pref = connector.loadProfile(action.applicationId, userTimeline.playerId)
                if (pref != null) {
                    userState.profileLoaded = true
                    userPreferences.fillWith(pref)
                }
            }
            action.state.testEvent = userPreferences.test
        }
    }

    internal fun handleEvent(connector: Connector, event: Event) {
        botDefinition.eventListener.listenEvent(this, connector, event)
    }

    override fun toString(): String {
        return "$botDefinition"
    }

}