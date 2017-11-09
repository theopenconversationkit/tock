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

    companion object {
        private val currentBus = ThreadLocal<BotBus>()

        /**
         * Helper method to returns the current bus,
         * linked to the thread currently used by the handler.
         * (warning: advanced usage only).
         */
        internal fun retrieveCurrentBus(): BotBus? = currentBus.get()
    }

    private val logger = KotlinLogging.logger {}

    private val nlp: NlpController by injector.instance()

    internal val botDefinition: BotDefinitionWrapper = BotDefinitionWrapper(botDefinitionBase)

    fun handle(action: Action, userTimeline: UserTimeline, connector: ConnectorController) {
        connector as TockConnectorController

        if (action.state.targetConnectorType == null) {
            action.state.targetConnectorType = connector.connectorType
        }

        loadProfileIfNotSet(action, userTimeline, connector)

        val dialog = getDialog(action, userTimeline)

        parseAction(action, userTimeline, dialog, connector)

        if (botDefinition.isEnabledIntent(dialog.state.currentIntent)) {
            logger.debug { "Enable bot for $action" }
            userTimeline.userState.botDisabled = false
        }

        if (!userTimeline.userState.botDisabled) {
            connector.startTypingInAnswerTo(action)
            val story = getStory(action, dialog)
            val bus = TockBotBus(connector, userTimeline, dialog, action, botDefinition)

            try {
                currentBus.set(bus)
                story.handle(bus)
            } finally {
                currentBus.remove()
            }
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
        var forced = false
        if (action is SendChoice) {
            action.step()?.apply {
                forced = true
                story.currentStep = this
            }
        }

        //revalidate step
        val step = story.findCurrentStep()
        story.currentStep = step?.name

        //check the step from the intent
        if (!forced && step == null && newIntent != null) {
            story.definition.steps.find { it.supportStarterIntent(newIntent) }
                    ?.apply {
                        forced = true
                        story.currentStep = name
                    }
        }

        //reset the step if applicable
        if (!forced && newIntent != null && step?.supportIntent(newIntent) != true) {
            story.currentStep = null
        }

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
                    if (!action.text.isNullOrBlank()) {
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

    internal fun handleEvent(controller: ConnectorController, event: Event) {
        if (!botDefinition.eventListener.listenEvent(controller, event)) {
            logger.warn { "unhandled event : $event" }
        }
    }

    override fun toString(): String {
        return "$botDefinition"
    }

}