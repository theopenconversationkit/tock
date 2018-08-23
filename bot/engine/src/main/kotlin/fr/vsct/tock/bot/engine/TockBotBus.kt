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

import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionNotificationType
import fr.vsct.tock.bot.engine.action.ActionPriority
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.NextUserActionState
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.UserInterfaceType
import java.util.Locale

/**
 *
 */
internal class TockBotBus(
    private val connector: TockConnectorController,
    override val userTimeline: UserTimeline,
    override val dialog: Dialog,
    override val action: Action,
    override val connectorData: ConnectorData,
    override var i18nProvider: I18nKeyProvider
) : BotBus {

    private val bot = connector.bot

    private val currentDialog: Dialog get() = userTimeline.currentDialog ?: dialog

    override var story: Story
        get() = currentDialog.currentStory ?: dialog.currentStory!!
        set(value) {
            currentDialog.stories.add(value)
        }
    override val botDefinition: BotDefinition = bot.botDefinition
    override val applicationId = action.applicationId
    override val botId = action.recipientId
    override val userId = action.playerId
    override val userPreferences: UserPreferences = userTimeline.userPreferences
    override val userLocale: Locale = userPreferences.locale
    override val userInterfaceType: UserInterfaceType =
        action.state.userInterface ?: connector.connectorType.userInterfaceType
    override val targetConnectorType: ConnectorType = action.state.targetConnectorType ?: connector.connectorType

    private val context: BusContext = BusContext()

    override val entities: Map<String, EntityStateValue> = currentDialog.state.entityValues
    override val intent: Intent? = currentDialog.state.currentIntent

    override var nextUserActionState: NextUserActionState?
        get() = currentDialog.state.nextActionState
        set(value) {
            currentDialog.state.nextActionState = value
        }

    private var _currentAnswerIndex: Int = 0
    override val currentAnswerIndex: Int get() = _currentAnswerIndex

    /**
     * Returns the non persistent current context value.
     */
    override fun getBusContextValue(name: String): Any? {
        return context.contextMap[name]
    }

    /**
     * Updates the non persistent current context value.
     */
    override fun setBusContextValue(key: String, value: Any?) {
        if (value == null) {
            context.contextMap - key
        } else {
            context.contextMap.put(key, value)
        }
    }

    private fun answer(a: Action, delay: Long = 0): BotBus {
        context.currentDelay += delay
        a.metadata.priority = context.priority
        if (a is SendSentence) {
            a.messages.addAll(context.connectorMessages.values)
        }
        context.clear()
        a.state.testEvent = userPreferences.test

        _currentAnswerIndex++

        story.actions.add(a)
        connector.send(connectorData, action, a, context.currentDelay)
        return this
    }

    override fun end(action: Action, delay: Long): BotBus {
        action.metadata.lastAnswer = true
        return answer(action, delay)
    }

    override fun sendRawText(plainText: CharSequence?, delay: Long): BotBus {
        return answer(SendSentence(botId, applicationId, userId, plainText), delay)
    }

    override fun send(action: Action, delay: Long): BotBus {
        return answer(action, delay)
    }

    override fun withPriority(priority: ActionPriority): BotBus {
        context.priority = priority
        return this
    }

    override fun withNotificationType(notificationType: ActionNotificationType): BotBus {
        context.notificationType = notificationType
        return this
    }

    override fun withMessage(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): BotBus {
        if (targetConnectorType == connectorType) {
            context.addMessage(messageProvider.invoke())
        }
        return this
    }

    override fun reloadProfile() {
        val newUserPref = connector.loadProfile(connectorData, userId)
        if (newUserPref != null) {
            userTimeline.userState.profileLoaded = true
            userPreferences.fillWith(newUserPref)
        } else {
            userPreferences.fillWith(UserPreferences())
        }
    }

    override fun markAsUnknown() {
        if (action is SendSentence) {
            bot.markAsUnknown(action, userTimeline)
        }
    }

}