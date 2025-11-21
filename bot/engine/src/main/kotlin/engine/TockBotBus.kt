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

package ai.tock.bot.engine

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.action.ActionPriority
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.action.SendDebug
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.shared.Executor
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.defaultLocale
import ai.tock.shared.injector
import ai.tock.shared.longProperty
import ai.tock.shared.provide
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.UserInterfaceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.time.Duration
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private val cleanupTimeoutProperty = longProperty("tock_cleanup_delay_seconds", 60)

/**
 *
 */
@OptIn(ExperimentalTockCoroutines::class)
internal class TockBotBus(
    val connector: TockConnectorController,
    override val userTimeline: UserTimeline,
    override val dialog: Dialog,
    override val action: Action,
    override val connectorData: ConnectorData,
    override var i18nProvider: I18nKeyProvider
) : BotBus {

    private val bot = connector.bot

    override val currentDialog: Dialog get() = userTimeline.currentDialog ?: dialog

    override var story: Story
        get() = currentDialog.currentStory ?: dialog.currentStory!!
        set(value) {
            currentDialog.stories.add(value)
        }
    override val botDefinition: BotDefinition = bot.botDefinition
    override val connectorId = action.connectorId
    override val botId = action.recipientId
    override val userId = action.playerId
    override val userPreferences: UserPreferences = userTimeline.userPreferences
    override var userLocale: Locale = findSupportedLocale(userPreferences.locale)
        private set

    override val userInterfaceType: UserInterfaceType =
        action.state.userInterface ?: connector.connectorType.userInterfaceType

    override val sourceConnectorType: ConnectorType = action.state.sourceConnectorType ?: connector.connectorType
    override val targetConnectorType: ConnectorType = action.state.targetConnectorType ?: connector.connectorType

    override val underlyingConnector: Connector = connector.connector

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

    private val customActionSender = AtomicReference<((Action, Long) -> Unit)?>()

    private fun findSupportedLocale(locale: Locale): Locale {
        val supp = bot.supportedLocales
        return when {
            supp.contains(locale) || supp.isEmpty() -> locale
            supp.any { it.language == locale.language } -> Locale(locale.language)
            supp.contains(defaultLocale) -> defaultLocale
            else -> supp.first()
        }
    }

    override fun changeUserLocale(locale: Locale) {
        userPreferences.locale = locale
        userLocale = findSupportedLocale(locale)
    }

    /**
     * Returns the non persistent current context value.
     */
    override fun <T> getBusContextValue(name: String): T? {
        @Suppress("UNCHECKED_CAST")
        return context.contextMap[name] as T
    }

    /**
     * Updates the non persistent current context value.
     */
    override fun setBusContextValue(key: String, value: Any?) {
        if (value == null) {
            context.contextMap.remove(key)
        } else {
            context.contextMap[key] = value
        }
    }

    private fun answer(a: Action, delay: Long = 0): BotBus {
        context.currentDelay += delay
        a.metadata.priority = context.priority
        a.metadata.visibility = context.visibility

        if (a is SendSentence) {
            a.messages.addAll(context.connectorMessages.values)
        }
        context.clear()
        a.state.testEvent = userPreferences.test

        _currentAnswerIndex++

        val actionToSent = applyBotAnswerInterceptor(a)
        story.actions.add(actionToSent)

        // The test connector is a rest connector (source),
        // but it invokes the engine with a target connector,
        // to receive the corresponding messages
        if (actionToSent !is SendDebug || ConnectorType.rest == sourceConnectorType) {
            // If the action is not a SendDebug, or it is, but the source connector is the rest connector
            customActionSender.get()?.invoke(actionToSent, context.currentDelay)
                ?: doSend(actionToSent, context.currentDelay)
        }

        return this
    }

    fun doSend(actionToSend: Action, delay: Long) {
        connector.send(userTimeline, connectorData, action, actionToSend, delay)
    }

    /**
     * Update Action using BotAnswerInterceptor
     */
    fun applyBotAnswerInterceptor(a: Action): Action {
        return BotRepository.botAnswerInterceptors.fold(a) { action, interceptor ->
            interceptor.handle(action, this)
        }
    }

    override fun end(action: Action, delay: Long): BotBus {
        action.metadata.lastAnswer = true
        return answer(action, delay)
    }

    override fun sendRawText(plainText: CharSequence?, delay: Long): BotBus {
        return answer(SendSentence(botId, connectorId, userId, plainText), delay)
    }

    override fun sendDebugData(title: String, data: Any?): BotBus {
        return answer(SendDebug(botId, connectorId, userId, title, data), 0)
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

    override fun withVisibility(visibility: ActionVisibility): BotBus {
        context.visibility = visibility
        return this
    }

    override fun withMessage(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): BotBus {
        if (isCompatibleWith(connectorType)) {
            context.addMessage(messageProvider.invoke())
        }
        return this
    }

    override fun withMessage(
        connectorType: ConnectorType,
        connectorId: String,
        messageProvider: () -> ConnectorMessage
    ): BotBus {
        if (this.connectorId == connectorId && isCompatibleWith(connectorType)) {
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
        changeUserLocale(userPreferences.locale)
    }

    override fun markAsUnknown() {
        if (action is SendSentence) {
            bot.markAsUnknown(action, userTimeline)
        }
    }

    internal data class QueuedAction(val action: Action, val delay: Long)

    /**
     * @return a callback to force-close the message queue
     */
    @OptIn(ExperimentalAtomicApi::class)
    fun deferMessageSending(
        scope: CoroutineScope,
        messageChannel: Channel<QueuedAction> = Channel(Channel.BUFFERED),
        timeout: Duration = Duration.ofSeconds(cleanupTimeoutProperty)
    ): () -> Unit {

        val closed = AtomicBoolean(false)
        customActionSender.set { action, delay ->
            // we queue in the current thread to preserve message ordering
            scope.launch(start = CoroutineStart.UNDISPATCHED) {
                messageChannel.send(QueuedAction(action, delay))
                // the following code may happen in a different thread if the channel's buffer was full
                if (action.metadata.lastAnswer) {
                    closed.store(true)
                    messageChannel.close()
                }
            }
        }
        scope.launch(injector.provide<Executor>().asCoroutineDispatcher()) {
            for ((action, delay) in messageChannel) {
                doSend(action, delay)
            }
        }
        return {
            if (!closed.load()) {
                injector.provide<Executor>().executeBlocking(timeout) {
                    if (!closed.load()) {
                        logger.info("force-closing message channel")
                        messageChannel.close()
                    }
                }

            }
        }
    }
}

private val logger = KotlinLogging.logger {}
