/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.engine

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendLocation
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.config.BotDefinitionWrapper
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.nlp.NlpController
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import java.util.Locale

/**
 *
 */
internal class Bot(
    botDefinitionBase: BotDefinition,
    val configuration: BotApplicationConfiguration,
    val supportedLocales: Set<Locale> = emptySet()
) {

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

    val botDefinition: BotDefinitionWrapper = BotDefinitionWrapper(botDefinitionBase)

    fun support(
        action: Action,
        userTimeline: UserTimeline,
        connector: ConnectorController,
        connectorData: ConnectorData
    ): Double {
        connector as TockConnectorController

        if (action.state.targetConnectorType == null) {
            action.state.targetConnectorType = connector.connectorType
        }

        loadProfileIfNotSet(connectorData, action, userTimeline, connector)

        val dialog = getDialog(action, userTimeline)

        parseAction(action, userTimeline, dialog, connector)

        val story = getStory(userTimeline, dialog, action)

        val bus = TockBotBus(connector, userTimeline, dialog, action, connectorData, botDefinition)

        return story.support(bus)
    }

    /**
     * Handle the user action.
     */
    fun handle(
        action: Action,
        userTimeline: UserTimeline,
        connector: ConnectorController,
        connectorData: ConnectorData
    ) {
        connector as TockConnectorController

        if (action.state.targetConnectorType == null) {
            action.state.targetConnectorType = connector.connectorType
        }

        loadProfileIfNotSet(connectorData, action, userTimeline, connector)

        val dialog = getDialog(action, userTimeline)

        parseAction(action, userTimeline, dialog, connector)

        if (userTimeline.userState.botDisabled && botDefinition.enableBot(userTimeline, dialog, action)) {
            logger.debug { "Enable bot for $action" }
            userTimeline.userState.botDisabled = false
            botDefinition.botEnabledListener(action)
        } else if (!userTimeline.userState.botDisabled && botDefinition.disableBot(userTimeline, dialog, action)) {
            logger.debug { "Disable bot for $action" }
            userTimeline.userState.botDisabled = true
        }

        if (!botDefinition.hasToPersistAction(userTimeline, action)) {
            connectorData.saveTimeline = false
        }

        if (!botDefinition.hasToPersistAction(userTimeline, action)) {
            connectorData.saveTimeline = false
        }

        if (!userTimeline.userState.botDisabled) {
            connector.startTypingInAnswerTo(action, connectorData)
            val story = getStory(userTimeline, dialog, action)
            val bus = TockBotBus(connector, userTimeline, dialog, action, connectorData, botDefinition)

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
        return userTimeline.currentDialog ?: createDialog(action, userTimeline)
    }

    private fun createDialog(action: Action, userTimeline: UserTimeline): Dialog {
        val newDialog = Dialog(setOf(userTimeline.playerId, action.recipientId))
        userTimeline.dialogs.add(newDialog)
        return newDialog
    }

    private fun getStory(userTimeline: UserTimeline, dialog: Dialog, action: Action): Story {
        val newIntent = dialog.state.currentIntent
        val previousStory = dialog.currentStory

        val story =
            if (previousStory == null
                || (newIntent != null && !previousStory.definition.supportIntent(newIntent))
            ) {
                val storyDefinition = botDefinition.findStoryDefinition(newIntent?.name, action.applicationId)
                val newStory = Story(
                    storyDefinition,
                    if (newIntent != null && storyDefinition.isStarterIntent(newIntent)) newIntent
                    else storyDefinition.mainIntent()
                )
                dialog.stories.add(newStory)
                newStory
            } else {
                previousStory
            }

        story.computeCurrentStep(userTimeline, dialog, action, newIntent)

        story.actions.add(action)

        //update action state
        action.state.intent = dialog.state.currentIntent?.name
        action.state.step = story.step

        return story
    }

    private fun parseAction(
        action: Action,
        userTimeline: UserTimeline,
        dialog: Dialog,
        connector: TockConnectorController
    ) {
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
                    if (!action.hasEmptyText()) {
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
        botDefinition.handleAttachmentStory?.let { definition ->
            definition.mainIntent().let {
                dialog.state.currentIntent = it
            }
        }
    }


    private fun parseLocation(location: SendLocation, dialog: Dialog) {
        botDefinition.userLocationStory?.let { definition ->
            definition.mainIntent().let {
                dialog.state.currentIntent = it
            }
        }
    }

    private fun parseChoice(choice: SendChoice, dialog: Dialog) {
        botDefinition.findIntent(choice.intentName).let { intent ->
            //restore state if it's possible (old dialog choice case)
            if (intent != Intent.unknown) {
                //TODO use story id
                val previousIntentName = choice.previousIntent()
                if (previousIntentName != null) {
                    val previousStory = botDefinition.findStoryDefinition(previousIntentName)
                    if (previousStory != botDefinition.unknownStory && previousStory.supportIntent(intent)) {
                        //the previous intent is a primary intent that support the new intent
                        val storyDefinition = botDefinition.findStoryDefinition(choice.intentName, choice.applicationId)
                        if (storyDefinition == botDefinition.unknownStory) {
                            //the new intent is a secondary intent, may be we need to create a intermediate story
                            val currentStory = dialog.currentStory
                            if (currentStory == null
                                || !currentStory.definition.supportIntent(intent)
                                || !currentStory.definition.supportIntent(botDefinition.findIntent(previousIntentName))
                            ) {
                                dialog.stories.add(Story(previousStory, intent))
                            }
                        }
                    }
                }
            }
            dialog.state.currentIntent = intent
        }
    }

    private fun loadProfileIfNotSet(
        connectorData: ConnectorData,
        action: Action,
        userTimeline: UserTimeline,
        connector: TockConnectorController
    ) {
        with(userTimeline) {
            if (!userState.profileLoaded) {
                val pref = connector.loadProfile(connectorData, userTimeline.playerId)
                if (pref != null) {
                    userState.profileLoaded = true
                    userState.profileRefreshed = true
                    userPreferences.fillWith(pref)
                }
            } else if (!userState.profileRefreshed) {
                userState.profileRefreshed = true
                val pref = connector.refreshProfile(connectorData, userTimeline.playerId)
                if (pref != null) {
                    userPreferences.refreshWith(pref)
                }
            }
            action.state.testEvent = userPreferences.test
        }
    }

    fun markAsUnknown(sendSentence: SendSentence, userTimeline: UserTimeline) {
        nlp.markAsUnknown(sendSentence, userTimeline, botDefinition)
    }

    override fun toString(): String {
        return "$botDefinition - ${configuration.name}"
    }
}