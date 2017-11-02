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
import fr.vsct.tock.bot.engine.action.ActionPriority
import fr.vsct.tock.bot.engine.action.ActionNotificationType
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.ContextValue
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.NextUserActionState
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.entity.Value
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.UserInterfaceType
import java.util.Locale

/**
 * A Bus mock used in unit tests.
 *
 * The send result actions are available in the [logsRepository] property.
 */
open class BotBusMock(override var userTimeline: UserTimeline,
                      override var dialog: Dialog,
                      override var story: Story,
                      override var action: Action,
                      override var botDefinition: BotDefinition,
                      override var i18nProvider: I18nKeyProvider,
                      override var userInterfaceType: UserInterfaceType = UserInterfaceType.textChat,
                      var initialUserPreferences: UserPreferences,
                      var connectorType: ConnectorType) : BotBus {


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
            context.i18nProvider,
            action.state.userInterface ?: context.userInterfaceType,
            context.userPreferences.copy(),
            context.connectorType
    )

    init {
        if (dialog.stories.isEmpty()) {
            dialog.stories.add(story)
        }
    }

    private val logsRepository: List<BotBusMockLog> = mutableListOf()

    val logs: List<BotBusMockLog> get() = checkEndCalled().run { logsRepository }

    val firstAnswer: BotBusMockLog get() = checkEndCalled().run { logsRepository.first() }

    val secondAnswer: BotBusMockLog get() = checkEndCalled().run { logsRepository[1] }

    val thirdAnswer: BotBusMockLog get() = checkEndCalled().run { logsRepository[2] }

    val lastAnswer: BotBusMockLog get() = checkEndCalled().run { logsRepository.last() }

    private var endCalled: Boolean = false

    /**
     * Throws an exception if the end() is not called
     */
    fun checkEndCalled(): BotBusMock {
        if (!endCalled) error("end() method not called")
        return this
    }

    /**
     * Add an entity set in the current action.
     */
    fun addActionEntity(contextValue: ContextValue) {
        action.state.entityValues.add(contextValue)
    }

    /**
     * Add an entity set in the current action.
     */
    fun addActionEntity(entity: Entity, newValue: Value?) {
        addActionEntity(ContextValue(entity, newValue))
    }

    /**
     * Simulate an action entity.
     */
    fun addActionEntity(entity: Entity, textContent: String) {
        addActionEntity(ContextValue(entity, null, textContent))
    }

    override var applicationId = action.applicationId
    override var botId = action.recipientId
    override var userId = action.playerId
    override var userPreferences: UserPreferences = userTimeline.userPreferences
    override var userLocale: Locale = userPreferences.locale
    override var targetConnectorType: ConnectorType = action.state.targetConnectorType ?: connectorType


    private val mockData: BusMockData = BusMockData()

    override var entities: Map<String, EntityStateValue> = dialog.state.entityValues
    override var intent: Intent? = dialog.state.currentIntent

    override var nextUserActionState: NextUserActionState?
        get() = dialog.state.nextActionState
        set(value) {
            dialog.state.nextActionState = value
        }

    open fun sendAction(action: Action, delay: Long) {
        (logsRepository as MutableList).add(BotBusMockLog(action, delay))
    }

    private fun answer(action: Action, delay: Long = 0): BotBus {
        mockData.currentDelay += delay
        action.metadata.priority = mockData.priority
        if (action is SendSentence) {
            action.messages.addAll(mockData.connectorMessages.values)
        }
        mockData.clear()
        action.state.testEvent = userPreferences.test

        story.actions.add(action)

        endCalled = action.metadata.lastAnswer

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
        return answer(SendSentence(botId, applicationId, userId, plainText), delay)
    }

    override fun send(action: Action, delay: Long): BotBus {
        return answer(action, delay)
    }

    override fun withPriority(priority: ActionPriority): BotBus {
        mockData.priority = priority
        return this
    }

    override fun withNotificationType(notificationType: ActionNotificationType): BotBus {
        mockData.notificationType = notificationType
        return this
    }

    override fun withMessage(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): BotBus {
        if (targetConnectorType == connectorType) {
            mockData.addMessage(messageProvider.invoke())
        }
        return this
    }

    override fun reloadProfile() {
        userPreferences.fillWith(initialUserPreferences)
    }
}