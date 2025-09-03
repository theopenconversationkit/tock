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

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.AsyncStoryHandler
import ai.tock.bot.definition.AsyncStoryStep
import ai.tock.bot.definition.DialogContextKey
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.ParameterKey
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.feature.FeatureType
import ai.tock.bot.engine.message.MessagesList
import ai.tock.bot.engine.message.MessagesList.Companion.toMessageList
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.engine.user.UserState
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.entity.Value
import ai.tock.shared.Executor
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.safeCast
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

@ExperimentalTockCoroutines
open class AsyncBotBus(val botBus: BotBus) : AsyncBus {
    companion object {
        /**
         * Helper method to retrieve the current bus,
         * linked to the coroutine currently used by the handler.
         */
        suspend fun retrieveCurrentBus(): AsyncBotBus? = currentCoroutineContext()[Ref]?.bus
    }

    private val executor: Executor get() = injector.provide()
    private val featureDao: FeatureDAO get() = injector.provide()

    override val connectorId: String
        get() = botBus.connectorId
    override val targetConnectorType: ConnectorType
        get() = botBus.targetConnectorType
    override val botId: PlayerId
        get() = botBus.botId
    override val userId: PlayerId
        get() = botBus.userId
    override val userLocale: Locale
        get() = botBus.userLocale
    override val userInterfaceType: UserInterfaceType
        get() = botBus.userInterfaceType
    override val intent: IntentAware?
        get() = botBus.intent
    override val currentIntent: IntentAware?
        get() = botBus.currentIntent
    override val currentStoryDefinition: StoryDefinition
        get() = story.definition
    override var step: AsyncStoryStep<*>?
        get() = synchronized(botBus) { story.currentStep as? AsyncStoryStep<*> }
        set(step) {
            synchronized(botBus) {
                story.step = step?.name
            }
        }
    override val userInfo: UserPreferences
        get() = botBus.userPreferences
    override val userState: UserState
        get() = botBus.userTimeline.userState
    override var nextUserActionState: NextUserActionState?
        get() = synchronized(botBus) { botBus.nextUserActionState }
        set(value) {
            synchronized(botBus) {
                botBus.nextUserActionState = value
            }
        }
    val story: Story get() = synchronized(botBus) { botBus.story }

    override fun defaultAnswerDelay() = botBus.defaultDelay(botBus.currentAnswerIndex)

    override fun choice(key: ParameterKey): String? {
        // Not synchronized - action is immutable
        return botBus.choice(key)
    }

    override fun booleanChoice(key: ParameterKey): Boolean {
        // Not synchronized - action is immutable
        return botBus.booleanChoice(key)
    }

    override fun hasActionEntity(role: String): Boolean {
        return synchronized(botBus) { botBus.hasActionEntity(role) }
    }

    override fun <T : Value> entityValue(
        role: String,
        valueTransformer: (EntityValue) -> T?
    ): T? {
        return synchronized(botBus) { botBus.entityValue(role, valueTransformer) }
    }

    override fun entityValueDetails(role: String): EntityValue? {
        return synchronized(botBus) { botBus.entityValueDetails(role) }
    }

    override fun changeEntityValue(role: String, newValue: EntityValue?) {
        synchronized(botBus) { botBus.changeEntityValue(role, newValue) }
    }

    override fun changeEntityValue(entity: Entity, newValue: Value?) {
        return synchronized (botBus) { botBus.changeEntityValue(entity, newValue) }
    }

    override fun removeAllEntityValues() {
        synchronized(botBus) {
            botBus.removeAllEntityValues()
        }
    }

    override fun <T : Any> getContextValue(key: DialogContextKey<T>): T? {
        return synchronized(botBus) {
            botBus.dialog.state.context[key.name]?.let(key.type::safeCast)
        }
    }

    override fun <T : Any> setContextValue(key: DialogContextKey<T>, value: T?) {
        synchronized(botBus) {
            botBus.dialog.state.setContextValue(key, value)
        }
    }

    override fun <T : Any> setBusContextValue(key: DialogContextKey<T>, value: T?) {
        synchronized(botBus) {
            botBus.setBusContextValue(key.name, value)
        }
    }

    override fun <T : Any> getBusContextValue(key: DialogContextKey<T>): T? {
        return synchronized(botBus) {
            botBus.getBusContextValue<Any?>(key.name)?.let(key.type::safeCast)
        }
    }

    override suspend fun isFeatureEnabled(
        feature: FeatureType,
        default: Boolean
    ): Boolean {
        // TODO replace worker thread offloading with suspend variant of FeatureDao.isEnabled
        return withContext(executor.asCoroutineDispatcher()) {
            featureDao.isEnabled(botBus.botDefinition.botId, botBus.botDefinition.namespace, feature, connectorId, default, userId.id)
        }
    }

    override suspend fun handleAndSwitchStory(
        storyDefinition: StoryDefinition,
        starterIntent: Intent,
        step: StoryStepDef?,
    ) {
        synchronized(botBus) {
            botBus.stepDef = step
            botBus.switchStory(storyDefinition, starterIntent)
            botBus.hasCurrentSwitchStoryProcess = false
        }
        (storyDefinition.storyHandler as? AsyncStoryHandler)?.handle(this)
            ?: trackCoroutineScope { storyDefinition.storyHandler.handle(botBus) }
    }

    internal suspend fun trackCoroutineScope(op: suspend () -> Unit) {
        coroutineScope {
            val oldScope = (botBus as? CoroutineBridgeBus)?.coroutineScope?.getAndSet(this)
            try {
                op()
            } finally {
                (botBus as? CoroutineBridgeBus)?.coroutineScope?.set(oldScope)
            }
        }
    }

    override fun i18nWithKey(
        key: String,
        defaultLabel: String,
        vararg args: Any?
    ): I18nLabelValue {
        return botBus.i18nKey(key, defaultLabel, *args)
    }

    override fun i18nWithKey(
        key: String,
        defaultLabel: String,
        defaultI18n: Set<I18nLocalizedLabel>,
        vararg args: Any?
    ): I18nLabelValue {
        return botBus.i18nKey(key, defaultLabel, defaultI18n, *args)
    }

    override fun i18n(
        defaultLabel: CharSequence,
        args: List<Any?>
    ): I18nLabelValue {
        return botBus.i18n(defaultLabel, args)
    }

    override suspend fun send(i18nText: CharSequence, delay: Long) {
        withContext(executor.asCoroutineDispatcher()) {
            synchronized(botBus) {
                botBus.send(i18nText, delay = delay)
            }
        }
    }

    override suspend fun send(i18nText: String, vararg i18nArgs: Any?) {
        withContext(executor.asCoroutineDispatcher()) {
            synchronized(botBus) {
                botBus.send(i18nText, *i18nArgs)
            }
        }
    }

    override suspend fun end(i18nText: CharSequence, delay: Long) {
        withContext(executor.asCoroutineDispatcher()) {
            synchronized(botBus) {
                botBus.end(i18nText, delay = delay)
            }
        }
    }

    override suspend fun end(i18nText: String, vararg i18nArgs: Any?) {
        withContext(executor.asCoroutineDispatcher()) {
            synchronized(botBus) {
                botBus.end(i18nText, *i18nArgs)
            }
        }
    }

    override suspend fun send(delay: Long, messageProvider: Bus<*>.() -> Any?) {
        val messages = toMessageList(messageProvider)
        synchronized(botBus) {
            if (messages.messages.isEmpty()) {
                botBus.send(delay)
            } else {
                botBus.send(messages, delay)
            }
        }
    }

    override suspend fun end(delay: Long, messageProvider: Bus<*>.() -> Any?) {
        val messages = toMessageList(messageProvider)
        synchronized(botBus) {
            if (messages.messages.isEmpty()) {
                botBus.end(delay)
            } else {
                botBus.end(messages, delay)
            }
        }
    }

    protected open suspend fun toMessageList(messageProvider: Bus<*>.() -> Any?): MessagesList =
        // calls to `translate` are blocking (database and possibly translator API),
        // so we ensure they are done in a worker thread
        withContext(executor.asCoroutineDispatcher()) {
            toMessageList(null, botBus, messageProvider)
        }

    data class Ref(val bus: AsyncBotBus): CoroutineContext.Element {
        companion object Key : CoroutineContext.Key<Ref>

        override val key = Key
    }
}
