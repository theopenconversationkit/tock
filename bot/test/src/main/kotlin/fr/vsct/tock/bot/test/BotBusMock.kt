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

package fr.vsct.tock.bot.test

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionSignificance
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.NextUserActionState
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.I18nLabelKey
import fr.vsct.tock.translator.Translator
import fr.vsct.tock.translator.UserInterfaceType
import java.util.Locale

/**
 * A Bus mock used in unit tests.
 *
 * The send result actions are available in the [logs] property.
 */
open class BotBusMock(override val userTimeline: UserTimeline,
                      override val dialog: Dialog,
                      override val story: Story,
                      override var action: Action,
                      val botDefinition: BotDefinition,
                      override var i18nProvider: I18nKeyProvider,
                      override val userInterfaceType: UserInterfaceType = UserInterfaceType.textChat,
                      internal val initialUserPreferences: UserPreferences,
                      internal val connectorType: ConnectorType) : BotBus {

    constructor(context: BotBusMockContext,
                action: Action = context.action)
            : this(
            context.userTimeline,
            if (action is SendChoice)
                context.dialog.copy(
                        state = context.dialog.state.copy(
                                currentIntent = context.botDefinition.findIntent(action.intentName)))
            else context.dialog,
            context.story.apply {
                if (action is SendChoice && action.step() != null) {
                    currentStep = action.step()
                }
            },
            action,
            context.botDefinition,
            context.storyDefinition.storyHandler as I18nKeyProvider,
            initialUserPreferences = context.userPreferences.copy(),
            connectorType = context.connectorType
    )

    val logs: List<BotBusMockLog> = mutableListOf()

    val firstAnswer: BotBusMockLog get() = logs.first()

    val secondAnswer: BotBusMockLog get() = logs[1]

    val thirdAnswer: BotBusMockLog get() = logs[2]

    val lastAnswer: BotBusMockLog get() = logs.last()

    override val applicationId = action.applicationId
    override val botId = action.recipientId
    override val userId = action.playerId
    override val userPreferences: UserPreferences = userTimeline.userPreferences
    override val userLocale: Locale = userPreferences.locale
    override val targetConnectorType: ConnectorType = action.state.targetConnectorType ?: connectorType


    private val mockData: BusMockData = BusMockData()

    override val entities: Map<String, EntityStateValue> = dialog.state.entityValues
    override val intent: Intent? = dialog.state.currentIntent

    override var nextUserActionState: NextUserActionState?
        get() = dialog.state.nextActionState
        set(value) {
            dialog.state.nextActionState = value
        }

    open fun sendAction(action: Action, delay: Long) {
        (logs as MutableList).add(BotBusMockLog(action, delay))
    }

    private fun answer(action: Action, delay: Long = 0): BotBus {
        mockData.currentDelay += delay
        action.metadata.significance = mockData.significance
        if (action is SendSentence) {
            action.messages.addAll(mockData.connectorMessages.values)
        }
        mockData.clear()
        action.state.testEvent = userPreferences.test

        story.actions.add(action)

        sendAction(action, mockData.currentDelay)
        return this
    }

    /**
     * Returns the non persistent current mockData value.
     */
    override fun getBusContextValue(name: String): Any? {
        return mockData.contextMap[name]
    }

    /**
     * Update the non persistent current mockData value.
     */
    override fun setBusContextValue(key: String, value: Any?) {
        if (value == null) {
            mockData.contextMap - key
        } else {
            mockData.contextMap.put(key, value)
        }
    }

    override fun end(action: Action, delay: Long): BotBus {
        action.metadata.lastAnswer = true
        return answer(action, delay)
    }

    override fun sendRawText(plainText: CharSequence?, delay: Long): BotBus {
        return answer(SendSentence(botId, applicationId, userId, plainText?.toString()), delay)
    }

    override fun send(action: Action, delay: Long): BotBus {
        return answer(action, delay)
    }

    override fun with(significance: ActionSignificance): BotBus {
        mockData.significance = significance
        return this
    }

    override fun with(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): BotBus {
        if (targetConnectorType == connectorType) {
            mockData.addMessage(messageProvider.invoke())
        }
        return this
    }

    override fun translate(key: I18nLabelKey?): CharSequence {
        return if (key == null) ""
        else Translator.formatMessage(key.defaultLabel.toString(), userLocale, userInterfaceType, key.args)
    }

    override fun reloadProfile() {
        userPreferences.fillWith(initialUserPreferences)
    }
}