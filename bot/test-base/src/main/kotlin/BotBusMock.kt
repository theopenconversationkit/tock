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

package ai.tock.bot.test

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorMessageProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.action.ActionPriority
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendDebug
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.dialog.Snapshot
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.event.ExitEvent
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.entity.Value
import ai.tock.shared.defaultLocale
import ai.tock.shared.provide
import ai.tock.translator.EMPTY_TRANSLATED_STRING
import ai.tock.translator.I18nContext
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.TranslatedSequence
import ai.tock.translator.Translator
import ai.tock.translator.TranslatorEngine
import ai.tock.translator.UserInterfaceType
import ai.tock.translator.raw
import java.util.Locale
import mu.KotlinLogging

/**
 * A Bus mock used in unit tests.
 *
 * The answers of the bot are available in the [answers] property.
 */
open class BotBusMock(
    val context: BotBusMockContext,
    override val action: Action = context.firstAction
) : BotBus {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val logsRepository: MutableList<BotBusMockLog> = mutableListOf()

    /**
     * The list of all bot answers recorded.
     */
    val answers: List<BotBusMockLog> get() = checkEndCalled().run { context.answers }

    /**
     * The first answer recorded.
     */
    val firstAnswer: BotBusMockLog get() = checkEndCalled().run { context.firstAnswer }

    /**
     * The second answer recorded.
     */
    val secondAnswer: BotBusMockLog get() = checkEndCalled().run { context.secondAnswer }

    /**
     * The third answer recorded.
     */
    val thirdAnswer: BotBusMockLog get() = checkEndCalled().run { context.thirdAnswer }

    /**
     * The last answer recorded.
     */
    val lastAnswer: BotBusMockLog get() = checkEndCalled().run { context.lastAnswer }

    /**
     * The list of bot answers for this bus.
     */
    val busAnswers: List<BotBusMockLog> get() = checkEndCalled().run { logsRepository }

    /**
     * The first answer for this bus.
     */
    val firstBusAnswer: BotBusMockLog get() = checkEndCalled().run { logsRepository.first() }

    /**
     * The second answer for this bus.
     */
    val secondBusAnswer: BotBusMockLog get() = checkEndCalled().run { logsRepository[1] }

    /**
     * The third answer for this bus.
     */
    val thirdBusAnswer: BotBusMockLog get() = checkEndCalled().run { logsRepository[2] }

    /**
     * The last answer for this bus.
     */
    val lastBusAnswer: BotBusMockLog get() = checkEndCalled().run { logsRepository.last() }

    private var endCount: Int = 0

    private var _currentAnswerIndex: Int = 0
    override val currentAnswerIndex: Int get() = _currentAnswerIndex

    override fun isCompatibleWith(connectorType: ConnectorType): Boolean =
        context.connectorsCompatibleWith.contains(connectorType)

    override fun send(event: Event, delayInMs: Long): BotBus =
        if (event is Action) {
            answer(event, delayInMs)
        } else {
            if (event is ExitEvent) {
                endCount++
            }
            this
        }

    /**
     * Run the [StoryHandler] of the current [story].
     */
    fun run(): BotBusMock {
        context.testContext.storyHandlerListeners.forEach {
            if (!it.startAction(this, story.definition.storyHandler)) {
                return this
            }
        }

        story.definition.storyHandler.handle(this)

        context.testContext.storyHandlerListeners.forEach {
            it.endAction(this, story.definition.storyHandler)
        }

        return this
    }

    /**
     * Throws an exception if the end() is not called
     */
    fun checkEndCalled(): BotBusMock {
        if (endCount == 0) error("end() method not called")
        else if (endCount > 1) error("end() called $endCount times")
        return this
    }

    /**
     * Add an entity set in the current action.
     */
    fun addActionEntity(contextValue: EntityValue): BotBusMock {
        action.state.entityValues.add(contextValue)
        return this
    }

    /**
     * Add an entity set in the current action.
     */
    fun addActionEntity(entity: Entity, newValue: Value?): BotBusMock = addActionEntity(EntityValue(entity, newValue))

    /**
     * Simulate an action entity.
     */
    fun addActionEntity(entity: Entity, textContent: String): BotBusMock =
        addActionEntity(EntityValue(entity, null, textContent))

    override var userTimeline: UserTimeline
        get() = context.userTimeline
        set(value) {
            context.userTimeline = value
        }

    override var dialog: Dialog
        get() = context.dialog
        set(value) {
            context.dialog = value
        }

    override val currentDialog: Dialog get() = dialog

    override var story: Story
        get() = context.story
        set(value) {
            context.story = value
            dialog.stories.add(value)
        }

    override var botDefinition: BotDefinition
        get() = context.botDefinition
        set(value) {
            context.botDefinition = value
        }
    override var i18nProvider: I18nKeyProvider
        get() = context.i18nProvider
        set(value) {
            context.i18nProvider = value
        }

    override var userInterfaceType: UserInterfaceType
        get() = context.userInterfaceType
        set(value) {
            context.userInterfaceType = value
        }

    var connectorType: ConnectorType
        get() = context.connectorType
        set(value) {
            context.connectorType = value
        }

    override var connectorData: ConnectorData =
        ConnectorData(ConnectorCallbackBase(action.applicationId, connectorType))

    /**
     * The translator used to translate labels - default is NoOp.
     */
    val translator: TranslatorEngine get() = context.testContext.testInjector.provide()
    override val connectorId get() = action.applicationId
    override val botId get() = action.recipientId
    override val userId get() = action.playerId
    override val userPreferences: UserPreferences get() = userTimeline.userPreferences
    override val userLocale: Locale get() = userPreferences.locale
    override var sourceConnectorType: ConnectorType
        get() = action.state.sourceConnectorType ?: connectorType
        set(value) {
            action.state.sourceConnectorType = value
            connectorType = value
        }
    override var targetConnectorType: ConnectorType
        get() = action.state.targetConnectorType ?: connectorType
        set(value) {
            action.state.targetConnectorType = value
            connectorType = value
        }
    override val underlyingConnector: Connector get() = error("do not use underlyingConnector method")

    private val mockData: BusMockData = BusMockData()

    override val entities: Map<String, EntityStateValue>
        get() = dialog.state.entityValues

    override var intent: IntentAware?
        get() = dialog.state.currentIntent
        set(value) {
            dialog.state.currentIntent = value?.wrappedIntent()
        }

    override var nextUserActionState: NextUserActionState?
        get() = dialog.state.nextActionState
        set(value) {
            dialog.state.nextActionState = value
        }

    init {
        val a = action

        a.state.targetConnectorType?.let {
            context.connectorType = it
        }
        a.state.userInterface?.let {
            context.userInterfaceType = it
        }
        if (a is SendChoice) {
            context.dialog.state.currentIntent = context.botDefinition.findIntent(a.intentName, a.applicationId)
        }
        if (a.state.intent != null) {
            context.dialog.state.currentIntent = context.botDefinition.findIntent(a.state.intent!!, a.applicationId)
        }

        a.state.entityValues.forEach {
            dialog.state.changeValue(it)
        }

        if (context.dialog.state.currentIntent != null &&
            !context.story.supportIntent(context.dialog.state.currentIntent!!)
        ) {
            val storyDefinition =
                context.botDefinition.findStoryDefinition(context.dialog.state.currentIntent!!, a.applicationId)
            context.story = Story(storyDefinition, storyDefinition.mainIntent())
            context.dialog.stories.add(context.story)
        } else if (context.dialog.stories.isEmpty()) {
            context.dialog.stories.add(context.story)
        }

        context.story.computeCurrentStep(context.userTimeline, context.dialog, a, context.dialog.state.currentIntent)

        if (a != context.firstAction) {
            context.story.actions.add(a)
            // update action state
            a.state.intent = context.dialog.state.currentIntent?.name
            a.state.step = context.story.currentStep?.name
        }
        if (a.state.userInterface != null) {
            context.userInterfaceType = a.state.userInterface!!
        }
    }

    open fun sendAction(action: Action, delay: Long) {
        logsRepository.add(BotBusMockLog(action, delay))
        context.logsRepository.add(BotBusMockLog(action, delay))
    }

    private fun answer(action: Action, delay: Long = 0): BotBus {
        mockData.currentDelay += delay
        action.metadata.priority = mockData.priority
        action.metadata.visibility = mockData.visibility
        action.metadata.quoteMessage = mockData.quoteMessage
        action.metadata.replyMessage = mockData.replyMessage
        if (action is SendSentence) {
            action.messages.addAll(mockData.connectorMessages.values)
            if (action.text == null && !action.hasMessage(connectorType) && !action.hasMessage(context.connectorsCompatibleWith.toList())) {
                error("Error: No message specified when calling send() or end()")
            }
        }
        logger.trace { "send action $action $mockData" }
        mockData.clear()
        action.state.testEvent = userPreferences.test

        story.actions.add(action)

        if (action.metadata.lastAnswer) {
            endCount++
        }
        _currentAnswerIndex++

        if (endCount == 1) {
            addSnapshot()
        }

        sendAction(applyBotAnswerInterceptor(action), mockData.currentDelay)
        return this
    }

    private fun addSnapshot() {
        context.snapshots.add(Snapshot(dialog))
    }

    override fun changeUserLocale(locale: Locale) {
        userPreferences.locale = locale
    }

    /**
     * Returns the non persistent current value.
     */
    override fun <T> getBusContextValue(name: String): T? {
        @Suppress("UNCHECKED_CAST")
        return mockData.contextMap[name] as T
    }

    /**
     * Update the non persistent current value.
     */
    override fun setBusContextValue(key: String, value: Any?) {
        if (value == null) {
            mockData.contextMap.remove(key)
        } else {
            mockData.contextMap[key] = value
        }
    }

    override fun end(action: Action, delay: Long): BotBus {
        action.metadata.lastAnswer = true
        return answer(action, delay)
    }

    fun createBotSentence(plainText: CharSequence?): SendSentence =
        SendSentence(botId, connectorId, userId, plainText)

    override fun sendRawText(plainText: CharSequence?, delay: Long): BotBus {
        return answer(createBotSentence(plainText), delay)
    }

    override fun sendDebugData(title: String, data: Any?): BotBus {
        // The test connector is a rest connector (source),
        // but it invokes the engine with a target connector,
        // to receive the corresponding messages
        if(ConnectorType.rest == sourceConnectorType) {
            return answer(SendDebug(botId, connectorId, userId, title, data), 0)
        }
        return this
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

    override fun withVisibility(visibility: ActionVisibility): BotBus {
        mockData.visibility = visibility
        return this
    }

    override fun withMessage(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): BotBus {
        if (targetConnectorType == connectorType || isCompatibleWith(connectorType)) {
            mockData.addMessage(messageProvider.invoke())
        }
        return this
    }

    override fun withMessage(
        connectorType: ConnectorType,
        connectorId: String,
        messageProvider: () -> ConnectorMessage
    ): BotBus {
        if (this.connectorId == connectorId && (targetConnectorType == connectorType || isCompatibleWith(connectorType))) {
            mockData.addMessage(messageProvider.invoke())
        }
        return this
    }

    override fun reloadProfile() {
        userPreferences.fillWith(context.initialUserPreferences)
    }

    override fun translate(key: I18nLabelValue?): TranslatedSequence =
        if (key == null) EMPTY_TRANSLATED_STRING
        else Translator.formatMessage(
            I18nLabel.findLabel(key.defaultI18n, userLocale, userInterfaceType, targetConnectorType.id)?.label
                ?: translator.translate(
                    key.defaultLabel.toString(),
                    defaultLocale,
                    userLocale
                ),
            I18nContext(
                userLocale,
                userInterfaceType,
                targetConnectorType.id,
                dialog.id.toString()
            ),
            key.args.map { arg ->
                when (arg) {
                    is I18nLabelValue -> translate(arg)
                    is Pair<*, *> -> if (arg.second is I18nLabelValue) Pair(arg.first, translate(arg.second as I18nLabelValue)) else arg
                    else -> arg
                }
            }
        ).raw

    override fun markAsUnknown() {
        // do nothing
    }

    /**
     * Update Action using BotAnswerInterceptor
     */
    fun applyBotAnswerInterceptor(a: Action): Action {
        return context.testContext.botAnswerInterceptors.fold(a) { action, interceptor ->
            interceptor.handle(action, this)
        }
    }

    /**
     * Assert that logs contains specified messages.
     */
    fun assert(vararg messages: ConnectorMessageProvider): Unit =
        messages.forEachIndexed { i, m -> busAnswers[i].assert(m) }
}
