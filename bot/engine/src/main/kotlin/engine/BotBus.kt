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

package ai.tock.bot.engine

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.ParameterKey
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandlerBase
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.action.ActionPriority
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.feature.FeatureType
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.MessagesList
import ai.tock.bot.engine.nlp.NlpCallStats
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.entity.Value
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabelValue

/**
 * A new bus instance is created for each user request.
 *
 * The bus is used by bot implementations to reply to the user request.
 */
interface BotBus : Bus<BotBus> {

    companion object {
        /**
         * Helper method to return the current bus,
         * linked to the thread currently used by the handler.
         * (warning: advanced usage only).
         */
        fun retrieveCurrentBus(): BotBus? = Bot.retrieveCurrentBus()
    }

    /**
     * The bot definition of the current bot.
     */
    val botDefinition: BotDefinition
    /**
     * The user timeline. Gets history and data about the user.
     */
    val userTimeline: UserTimeline
    /**
     * The current dialog history for this user.
     */
    val dialog: Dialog
    /**
     * The current story.
     */
    var story: Story
    /**
     * The user action.
     */
    val action: Action

    /**
     * The data specific to the connector (if any).
     */
    val connectorData: ConnectorData

    //shortcuts

    /**
     * User preferences of the current user.
     */
    val userPreferences: UserPreferences

    /**
     * The underlying [Connector] used.
     * Please do not use this method as it is exposed for third party libraries only.
     */
    val underlyingConnector: Connector

    /**
     * The entities in the dialog state.
     */
    val entities: Map<String, EntityStateValue>

    /**
     * To manage i18n.
     */
    var i18nProvider: I18nKeyProvider

    /**
     * Qualify the next user action.
     */
    var nextUserActionState: NextUserActionState?

    var step: StoryStep<out StoryHandlerDefinition>?
        get() = story.currentStep
        set(step) {
            story.step = step?.name
        }

    override val stepName: String? get() = step?.name

    /**
     * The text sent by the user if any.
     */
    val userText: String? get() = (action as? SendSentence)?.stringText?.trim()

    /**
     * To know if the current intent is owned by the [IntentAware].
     */
    fun isIntent(intentOwner: IntentAware): Boolean = intentOwner.wrap(intent?.wrappedIntent())

    /**
     * Returns the NLP call stats if an NLP call has occurred, null either.
     */
    fun nlpStats(): NlpCallStats? = if (action is SendSentence) (action as SendSentence).nlpStats else null

    /**
     * Is this current action is a [SendChoice]?
     */
    fun isChoiceAction(): Boolean = action is SendChoice

    /**
     * Returns the value of the specified choice parameter, null if the user action is not a [SendChoice]
     * or if this parameter is not set.
     */
    fun choice(key: ParameterKey): String? = action.choice(key)

    /**
     * Returns true if the specified choice parameter has the "true" value, false either.
     */
    fun booleanChoice(key: ParameterKey): Boolean = action.booleanChoice(key)

    /**
     * Checks that the specified choice parameter has the specified value.
     */
    fun hasChoiceValue(param: ParameterKey, value: ParameterKey): Boolean = choice(param) == value.key

    /**
     * Returns true if the current action has the specified entity role.
     */
    fun hasActionEntity(role: String): Boolean {
        return action.hasEntity(role)
    }

    /**
     * Returns true if the current action has the specified entity role.
     */
    fun hasActionEntity(entity: Entity): Boolean = hasActionEntity(entity.role)

    /**
     * Returns the current value for the specified entity role.
     */
    fun <T : Value> entityValue(
        role: String,
        valueTransformer: (EntityValue) -> T? = @Suppress("UNCHECKED_CAST") { it.value as? T? }
    ): T? {
        return entities[role]?.value?.let { valueTransformer.invoke(it) }
    }

    /**
     * Returns the current value for the specified entity.
     */
    fun <T : Value> entityValue(
        entity: Entity,
        valueTransformer: (EntityValue) -> T? = @Suppress("UNCHECKED_CAST") { it.value as? T? }
    ): T? = entityValue(entity.role, valueTransformer)

    /**
     * Returns the current text content for the specified entity.
     */
    fun entityText(entity: Entity): String? = entityValueDetails(entity)?.content

    /**
     * Returns the current text content for the specified entity.
     */
    fun entityText(role: String): String? = entityValueDetails(role)?.content

    /**
     * Returns the current [EntityValue] for the specified entity.
     */
    fun entityValueDetails(entity: Entity): EntityValue? = entityValueDetails(entity.role)

    /**
     * Returns the current [EntityValue] for the specified role.
     */
    fun entityValueDetails(role: String): EntityValue? = entities[role]?.value

    /**
     * Updates the current entity value in the dialog.
     * @param role entity role
     * @param newValue the new entity value
     */
    fun changeEntityValue(role: String, newValue: EntityValue?) {
        dialog.state.changeValue(role, newValue)
    }

    /**
     * Updates the current entity value in the dialog.
     * @param entity the entity definition
     * @param newValue the new entity value
     */
    fun changeEntityValue(entity: Entity, newValue: Value?) {
        dialog.state.changeValue(entity, newValue)
    }

    /**
     * Updates the current entity value in the dialog.
     * @param entity the entity definition
     * @param newValue the new entity value
     */
    fun changeEntityValue(entity: Entity, newValue: EntityValue) = changeEntityValue(entity.role, newValue)

    /**
     * Updates the current entity text value in the dialog.
     * @param entity the entity definition
     * @param textContent the new entity text content
     */
    @Deprecated("use changeEntityText instead")
    fun changeEntityValue(entity: Entity, textContent: String) =
        changeEntityValue(
            entity.role,
            EntityValue(entity, null, textContent)
        )

    /**
     * Updates the current entity text value in the dialog.
     * @param entity the entity definition
     * @param textContent the new entity text content
     */
    fun changeEntityText(entity: Entity, textContent: String?) =
        changeEntityValue(
            entity.role,
            EntityValue(entity, null, textContent)
        )

    /**
     * Removes entity value for the specified role.
     */
    fun removeEntityValue(role: String) {
        dialog.state.resetValue(role)
    }

    /**
     * Removes entity value for the specified role.
     */
    fun removeEntityValue(entity: Entity) = removeEntityValue(entity.role)

    /**
     * Removes all current entity values.
     */
    fun removeAllEntityValues() {
        dialog.state.resetAllEntityValues()
    }

    /**
     * Resets all entity values, context values, [ai.tock.bot.engine.dialog.DialogState.userLocation]
     * and [ai.tock.bot.engine.dialog.DialogState.nextActionState]
     * but keep entity values history.
     * @see [ai.tock.bot.engine.dialog.DialogState.resetState]
     */
    fun resetDialogState() {
        dialog.state.resetState()
    }

    /**
     * Returns the persistent current context value.
     */
    fun <T : Any> contextValue(name: String): T? {
        @Suppress("UNCHECKED_CAST")
        return dialog.state.context[name] as? T?
    }

    /**
     * Returns the persistent current context value.
     */
    fun <T : Any> contextValue(key: ParameterKey): T? = contextValue(key.key)

    /**
     * Updates persistent context value.
     * Do not store Collection or Map in the context, only plain objects or typed arrays.
     */
    fun changeContextValue(name: String, value: Any?) {
        dialog.state.setContextValue(name, value)
    }

    /**
     * Updates persistent context value.
     */
    fun changeContextValue(key: ParameterKey, value: Any?) = changeContextValue(key.key, value)

    /**
     * Returns the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun <T> getBusContextValue(name: String): T?

    /**
     * Returns the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun <T> getBusContextValue(key: ParameterKey): T? = getBusContextValue(key.key)

    /**
     * Updates the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun setBusContextValue(key: String, value: Any?)

    /**
     * Updates the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun setBusContextValue(key: ParameterKey, value: Any?) = setBusContextValue(key.key, value)

    /**
     * Sends text that should not be translated as last bot answer.
     */
    override fun endRawText(plainText: CharSequence?, delay: Long): BotBus {
        return end(SendSentence(botId, applicationId, userId, plainText), delay)
    }

    /**
     * Sends [Message] as last bot answer.
     */
    fun end(message: Message, delay: Long = defaultDelay(currentAnswerIndex)): BotBus {
        return end(message.toAction(this), delay)
    }

    /**
     * Sends [Action] as last bot answer.
     */
    fun end(action: Action, delay: Long = defaultDelay(currentAnswerIndex)): BotBus

    /**
     * Sends a [MessagesList] and end the dialog.
     */
    fun end(messages: MessagesList, initialDelay: Long = 0): BotBus {
        messages.messages.forEachIndexed { i, m ->
            val wait = initialDelay + m.delay
            if (messages.messages.size - 1 == i) {
                end(m.toAction(this), wait)
            } else {
                send(m.toAction(this), wait)
            }
        }
        return this
    }

    /**
     * Sends a [Message].
     */
    fun send(message: Message, delay: Long = defaultDelay(currentAnswerIndex)): BotBus {
        return send(message.toAction(this), delay)
    }

    /**
     * Sends a [MessagesList].
     */
    fun send(messages: MessagesList, initialDelay: Long = 0): BotBus {
        messages.messages.forEachIndexed { i, m ->
            val wait = initialDelay + m.delay
            send(m.toAction(this), wait)
        }
        return this
    }

    /**
     * Sends an [Action].
     */
    fun send(action: Action, delay: Long = defaultDelay(currentAnswerIndex)): BotBus

    /**
     * Adds the specified [ActionPriority] to the bus context.
     */
    fun withPriority(priority: ActionPriority): BotBus

    /**
     * Adds the specified [ActionNotificationType] to the bus context.
     */
    fun withNotificationType(notificationType: ActionNotificationType): BotBus

    /**
     * Adds the specified [ActionVisibility] to the bus context.
     */
    fun withVisibility(visibility: ActionVisibility): BotBus

    /**
     * Reloads the user profile.
     */
    fun reloadProfile()

    /**
     * Switches the context to the specified story definition (start a new [Story]).
     */
    fun switchStory(storyDefinition: StoryDefinition) {
        val starterIntent = storyDefinition.mainIntent()
        story = Story(storyDefinition, starterIntent, story.step)
        dialog.stories.add(story)
        dialog.state.currentIntent = starterIntent
    }

    /**
     * Handles the action and switches the context to the specified story definition.
     */
    fun handleAndSwitchStory(storyDefinition: StoryDefinition) {
        switchStory(storyDefinition)
        @Suppress("UNCHECKED_CAST")
        storyDefinition.storyHandler.handle(this)
    }

    /**
     * Does not send an answer. Synchronous [Connector]s (like Google Assistant or Alexa)
     * usually do not support skipping answer.
     */
    fun skipAnswer() {
        connectorData.skipAnswer = true
    }

    /**
     * Is the feature enabled?
     *
     * @param feature the feature to check
     * @param default the default value if the feature state is unknown
     */
    fun isFeatureEnabled(feature: FeatureType, default: Boolean = false) =
        injector.provide<FeatureDAO>().isEnabled(botDefinition.botId, botDefinition.namespace, feature, default)

    /**
     * Marks the current as not understood in the nlp model.
     */
    fun markAsUnknown()

    //i18n provider implementation
    override fun i18n(defaultLabel: CharSequence, args: List<Any?>): I18nLabelValue =
        i18nProvider.i18n(defaultLabel, args)

    /**
     * Gets an i18n label with the specified key.
     */
    fun i18nKey(key: String, defaultLabel: CharSequence, vararg args: Any?): I18nLabelValue =
        story.definition.storyHandler.let {
            (it as? StoryHandlerBase<*>)?.i18nKey(key, defaultLabel, *args)
                ?: I18nLabelValue(
                    key,
                    botDefinition.namespace,
                    botDefinition.botId,
                    defaultLabel,
                    args.toList()
                )
        }

    //I18nTranslator implementation
    override val contextId: String? get() = dialog.id.toString()

    override fun defaultDelay(answerIndex: Int): Long = botDefinition.defaultDelay(answerIndex)

    override val test: Boolean get() = userPreferences.test

    //this is mainly to allow mockk to work -->

    override fun withMessage(message: ConnectorMessage): BotBus = super.withMessage(message)

    override fun send(delay: Long): BotBus = super.send(delay)

    override fun send(i18nText: CharSequence, delay: Long, vararg i18nArgs: Any?): BotBus = super.send(i18nText, delay, *i18nArgs)

    override fun send(i18nText: CharSequence, vararg i18nArgs: Any?): BotBus = super.send(i18nText, *i18nArgs)

    override fun send(delay: Long, messageProvider: BotBus.() -> Any?): BotBus = super.send(delay, messageProvider)

    override fun end(i18nText: CharSequence, delay: Long, vararg i18nArgs: Any?): BotBus = super.end(i18nText, delay, *i18nArgs)

    override fun end(i18nText: CharSequence, vararg i18nArgs: Any?): BotBus = super.end(i18nText, *i18nArgs)

    override fun end(delay: Long): BotBus = super.end(delay)

    override fun end(delay: Long, messageProvider: BotBus.() -> Any?): BotBus = super.end(delay, messageProvider)
}