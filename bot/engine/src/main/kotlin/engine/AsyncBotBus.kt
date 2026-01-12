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
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.safeCast

@ExperimentalTockCoroutines
open class AsyncBotBus(val syncBus: BotBus) : AsyncBus {
    companion object {
        /**
         * Helper method to retrieve the current bus,
         * linked to the coroutine currently used by the handler.
         */
        suspend fun retrieveCurrentBus(): AsyncBotBus? = currentCoroutineContext()[Ref]?.bus
    }

    @Deprecated("Use syncBus instead", ReplaceWith("syncBus"))
    val botBus get() = syncBus

    private val executor: Executor get() = injector.provide()
    private val featureDao: FeatureDAO get() = injector.provide()

    override val connectorId: String
        get() = syncBus.connectorId
    override val targetConnectorType: ConnectorType
        get() = syncBus.targetConnectorType
    override val botId: PlayerId
        get() = syncBus.botId
    override val userId: PlayerId
        get() = syncBus.userId
    override val userLocale: Locale
        get() = syncBus.userLocale
    override val userInterfaceType: UserInterfaceType
        get() = syncBus.userInterfaceType
    override val intent: IntentAware?
        get() = syncBus.intent
    override val currentIntent: IntentAware?
        get() = syncBus.currentIntent
    override val currentStoryDefinition: StoryDefinition
        get() = story.definition
    override var step: AsyncStoryStep<*>?
        get() = story.currentStep as? AsyncStoryStep<*>
        set(step) {
            story.step = step?.name
        }
    override val userInfo: UserPreferences
        get() = syncBus.userPreferences
    override val userState: UserState
        get() = syncBus.userTimeline.userState
    val story: Story get() = syncBus.story

    override fun defaultAnswerDelay() = syncBus.defaultDelay(syncBus.currentAnswerIndex)

    override suspend fun constrainNlp(nextActionState: NextUserActionState) {
        syncBus.nextUserActionState = nextActionState
    }

    override fun choice(key: ParameterKey): String? {
        return syncBus.choice(key)
    }

    override fun booleanChoice(key: ParameterKey): Boolean {
        return syncBus.booleanChoice(key)
    }

    override fun hasActionEntity(role: String): Boolean {
        return syncBus.hasActionEntity(role)
    }

    override fun <T : Value> entityValue(
        role: String,
        valueTransformer: (EntityValue) -> T?,
    ): T? {
        return synchronized(syncBus) { syncBus.entityValue(role, valueTransformer) }
    }

    override fun entityValueDetails(role: String): EntityValue? {
        return synchronized(syncBus) { syncBus.entityValueDetails(role) }
    }

    override fun changeEntityValue(
        role: String,
        newValue: EntityValue?,
    ) {
        synchronized(syncBus) { syncBus.changeEntityValue(role, newValue) }
    }

    override fun changeEntityValue(
        entity: Entity,
        newValue: Value?,
    ) {
        return synchronized(syncBus) { syncBus.changeEntityValue(entity, newValue) }
    }

    override fun removeAllEntityValues() {
        // Synchronized to avoid ConcurrentModificationException with other entity setters
        synchronized(syncBus) {
            syncBus.removeAllEntityValues()
        }
    }

    override fun <T : Any> getContextValue(key: DialogContextKey<T>): T? {
        return syncBus.dialog.state.context[key.name]?.let(key.type::safeCast)
    }

    override fun <T : Any> setContextValue(
        key: DialogContextKey<T>,
        value: T?,
    ) {
        syncBus.dialog.state.setContextValue(key, value)
    }

    override fun <T : Any> setBusContextValue(
        key: DialogContextKey<T>,
        value: T?,
    ) {
        syncBus.setBusContextValue(key.name, value)
    }

    override fun <T : Any> getBusContextValue(key: DialogContextKey<T>): T? {
        return syncBus.getBusContextValue<Any?>(key.name)?.let(key.type::safeCast)
    }

    override suspend fun isFeatureEnabled(
        feature: FeatureType,
        default: Boolean,
    ): Boolean =
        featureDao.isEnabled(
            syncBus.botDefinition.botId,
            syncBus.botDefinition.namespace,
            feature,
            connectorId,
            default,
            userId.id,
        )

    override suspend fun handleAndSwitchStory(
        storyDefinition: StoryDefinition,
        starterIntent: Intent,
        step: StoryStepDef?,
    ) {
        synchronized(syncBus) {
            syncBus.stepDef = step
            syncBus.switchStory(storyDefinition, starterIntent)
            syncBus.hasCurrentSwitchStoryProcess = false
        }
        (storyDefinition.storyHandler as? AsyncStoryHandler)?.handle(this)
            ?: storyDefinition.storyHandler.handle(syncBus)
    }

    override fun i18nWithKey(
        key: String,
        defaultLabel: String,
        vararg args: Any?,
    ): I18nLabelValue {
        return syncBus.i18nKey(key, defaultLabel, *args)
    }

    override fun i18nWithKey(
        key: String,
        defaultLabel: String,
        defaultI18n: Set<I18nLocalizedLabel>,
        vararg args: Any?,
    ): I18nLabelValue {
        return syncBus.i18nKey(key, defaultLabel, defaultI18n, *args)
    }

    override fun i18n(
        defaultLabel: CharSequence,
        args: List<Any?>,
    ): I18nLabelValue {
        return syncBus.i18n(defaultLabel, args)
    }

    override suspend fun send(
        i18nText: CharSequence,
        delay: Long,
    ) {
        withContext(executor.asCoroutineDispatcher()) {
            synchronized(syncBus) {
                syncBus.send(i18nText, delay = delay)
            }
        }
    }

    override suspend fun send(
        i18nText: String,
        vararg i18nArgs: Any?,
    ) {
        withContext(executor.asCoroutineDispatcher()) {
            synchronized(syncBus) {
                syncBus.send(i18nText, *i18nArgs)
            }
        }
    }

    override suspend fun end(
        i18nText: CharSequence,
        delay: Long,
    ) {
        withContext(executor.asCoroutineDispatcher()) {
            synchronized(syncBus) {
                syncBus.end(i18nText, delay = delay)
            }
        }
    }

    override suspend fun end(
        i18nText: String,
        vararg i18nArgs: Any?,
    ) {
        withContext(executor.asCoroutineDispatcher()) {
            synchronized(syncBus) {
                syncBus.end(i18nText, *i18nArgs)
            }
        }
    }

    override suspend fun send(
        delay: Long,
        messageProvider: Bus<*>.() -> Any?,
    ) {
        val messages = toMessageList(messageProvider)
        synchronized(syncBus) {
            if (messages.messages.isEmpty()) {
                syncBus.send(delay)
            } else {
                syncBus.send(messages, delay)
            }
        }
    }

    override suspend fun end(
        delay: Long,
        messageProvider: Bus<*>.() -> Any?,
    ) {
        val messages = toMessageList(messageProvider)
        synchronized(syncBus) {
            if (messages.messages.isEmpty()) {
                syncBus.end(delay)
            } else {
                syncBus.end(messages, delay)
            }
        }
    }

    protected open suspend fun toMessageList(messageProvider: Bus<*>.() -> Any?): MessagesList =
        // calls to `translate` are blocking (database and possibly translator API),
        // so we ensure they are done in a worker thread
        withContext(executor.asCoroutineDispatcher()) {
            toMessageList(null, syncBus, messageProvider)
        }

    data class Ref(val bus: AsyncBotBus) : CoroutineContext.Element {
        companion object Key : CoroutineContext.Key<Ref>

        override val key = Key
    }
}
