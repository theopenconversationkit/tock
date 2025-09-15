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

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorMessageProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.AsyncStoryStep
import ai.tock.bot.definition.DialogContextKey
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.ParameterKey
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryDefinitionWithSteps
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.feature.FeatureType
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.engine.user.UserState
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType
import java.util.Locale

@ExperimentalTockCoroutines
interface AsyncBus : DialogEntityManager, I18nKeyProvider {
    /**
     * The connector ID.
     */
    val connectorId: String

    val targetConnectorType: ConnectorType

    /**
     * The current bot id.
     */
    val botId: PlayerId

    /**
     * The locale in which the bot should answer
     *
     * This locale generally corresponds to the user's selected locale.
     * When the user's specific locale is not supported by the chatbot (as defined in the admin interface),
     * the selected locale will be the closest available match, defaulting to the [ai.tock.shared.defaultLocale].
     */
    val userLocale: Locale

    /**
     * The current user interface type.
     */
    val userInterfaceType: UserInterfaceType

    /**
     * The current user id.
     */
    val userId: PlayerId

    val userInfo: UserPreferences

    val userState: UserState

    /**
     * The current intent of the dialog at Bus (ie request) initialization.
     */
    val intent: IntentAware?

    /**
     * The current intent for this user (may be different from the initial [intent]).
     */
    val currentIntent: IntentAware?

    val currentStoryDefinition: StoryDefinition

    var step: AsyncStoryStep<*>?

    suspend fun constrainNlp(nextActionState: NextUserActionState)

    /**
     * Returns true if the current action has the specified entity role.
     */
    fun hasActionEntity(role: String): Boolean

    /**
     * Returns the value of the specified choice parameter, null if the user action is not a [SendChoice]
     * or if this parameter is not set.
     *
     * @see booleanChoice
     */
    fun choice(key: ParameterKey): String?

    /**
     * Returns true if the specified choice parameter has the "true" value, false either.
     *
     * @see choice
     */
    fun booleanChoice(key: ParameterKey): Boolean

    /**
     * Returns the persistent current context value for the given [key], or `null` if
     * no such value is present in the dialog state.
     */
    fun <T : Any> getContextValue(key: DialogContextKey<T>): T?

    /**
     * Updates persistent context value.
     * Do not store Collection or Map in the context, only plain objects or typed arrays.
     */
    fun <T : Any> setContextValue(key: DialogContextKey<T>, value: T?)

    /**
     * Updates the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun <T : Any> setBusContextValue(key: DialogContextKey<T>, value: T?)

    /**
     * Returns the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun <T : Any> getBusContextValue(key: DialogContextKey<T>): T?

    /**
     * Is the feature enabled?
     *
     * @param feature the feature to check
     * @param default the default value if the feature state is unknown
     */
    suspend fun isFeatureEnabled(feature: FeatureType, default: Boolean = false): Boolean

    suspend fun handleAndSwitchStory(
        storyDefinition: StoryDefinition,
        starterIntent: Intent = storyDefinition.mainIntent(),
        step: StoryStepDef? = null,
    )

    suspend fun <S : StoryStepDef> handleAndSwitchStory(
        storyDefinition: StoryDefinitionWithSteps<S>,
        starterIntent: Intent = storyDefinition.mainIntent(),
        step: S? = null,
    ) {
        handleAndSwitchStory(storyDefinition as StoryDefinition, starterIntent, step)
    }

    fun i18nWithKey(key: String, defaultLabel: String, vararg args: Any?): I18nLabelValue
    fun i18nWithKey(key: String, defaultLabel: String, defaultI18n: Set<I18nLocalizedLabel>, vararg args: Any?): I18nLabelValue

    suspend fun send(i18nText: CharSequence, delay: Long = defaultAnswerDelay())
    suspend fun send(i18nText: String, vararg i18nArgs: Any?)
    suspend fun end(i18nText: CharSequence, delay: Long = defaultAnswerDelay())
    suspend fun end(i18nText: String, vararg i18nArgs: Any?)

    /**
     * [messageProvider] must return an instance or a collection of the following possible types:
     * - [CharSequence] ([String], [ai.tock.translator.I18nLabelValue], or [ai.tock.translator.RawString])
     * - [ConnectorMessageProvider] (typically a [ConnectorMessage] for the current [Bus.targetConnectorType])
     * - [Message] (typically a [ai.tock.bot.engine.message.Sentence])
     *
     * @param delay the delay between the previous message and this one
     */
    suspend fun send(
        delay: Long = defaultAnswerDelay(),
        messageProvider: Bus<*>.() -> Any?
    )

    /**
     * [messageProvider] must return an instance or a collection of the following possible types:
     * - [CharSequence] ([String], [ai.tock.translator.I18nLabelValue], or [ai.tock.translator.RawString])
     * - [ConnectorMessageProvider] (typically a [ConnectorMessage] for the current [Bus.targetConnectorType])
     * - [Message] (typically a [ai.tock.bot.engine.message.Sentence])
     *
     * @param delay the delay between the previous message and this one
     */
    suspend fun end(
        delay: Long = defaultAnswerDelay(),
        messageProvider: Bus<*>.() -> Any?
    )

    /**
     * @see ai.tock.bot.definition.BotDefinition.defaultDelay
     */
    fun defaultAnswerDelay(): Long
}
