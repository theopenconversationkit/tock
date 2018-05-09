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
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.ParameterKey
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.definition.StoryHandlerDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionNotificationType
import fr.vsct.tock.bot.engine.action.ActionPriority
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.ContextValue
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.NextUserActionState
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.message.MessagesList
import fr.vsct.tock.bot.engine.nlp.NlpCallStats
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.entity.Value
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.I18nLabelValue
import fr.vsct.tock.translator.UserInterfaceType
import java.util.Locale

/**
 * A new bus instance is created for each user request.
 *
 * The bus is used by bot implementations to reply to the user request.
 */
interface BotBus : I18nTranslator {

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
     * The user timeline. Get history and data abgout the user.
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
     * The current application id.
     */
    val applicationId: String
    /**
     * The current bot id.
     */
    val botId: PlayerId
    /**
     * The current user id.
     */
    val userId: PlayerId
    /**
     * User preferences of the current user.
     */
    val userPreferences: UserPreferences
    /**
     * The current user [Locale].
     */
    override val userLocale: Locale
    /**
     * The current user interface type.
     */
    override val userInterfaceType: UserInterfaceType
    /**
     * The [ConnectorType] used for the response.
     */
    override val targetConnectorType: ConnectorType

    /**
     * The entities in the dialog state.
     */
    val entities: Map<String, EntityStateValue>
    /**
     * The current intent.
     */
    val intent: IntentAware?

    /**
     * To manage i18n.
     */
    var i18nProvider: I18nKeyProvider

    /**
     * Qualify the next user action.
     */
    var nextUserActionState: NextUserActionState?

    /**
     * The current [StoryStep] of the [Story].
     */
    var step: StoryStep<out StoryHandlerDefinition>?
        get() = story.findCurrentStep()
        set(step) {
            story.currentStep = step?.name
        }

    /**
     * To know if the current intent is owned by the [IntentAware].
     */
    fun isIntent(intentOwner: IntentAware): Boolean = intentOwner.wrap(intent?.wrappedIntent())

    /**
     * Returns the NLP call stats if an NLP call has occurred, null either.
     */
    fun nlpStats(): NlpCallStats? = if (action is SendSentence) (action as SendSentence).nlpStats else null

    /**
     * Returns the value of the specified choice parameter, null if the user action is not a [SendChoice]
     * or if this parameter is not set.
     */
    fun choice(key: ParameterKey): String? = action.choice(key)

    /**
     * Returns true if the specified choice as the "true" value, false either.
     */
    fun booleanChoice(key: ParameterKey): Boolean = action.booleanChoice(key)

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
        valueTransformer: (ContextValue) -> T? = @Suppress("UNCHECKED_CAST") { it.value as? T? }
    ): T? {
        return entities[role]?.value?.let { valueTransformer.invoke(it) }
    }

    /**
     * Returns the current value for the specified entity.
     */
    fun <T : Value> entityValue(
        entity: Entity,
        valueTransformer: (ContextValue) -> T? = @Suppress("UNCHECKED_CAST") { it.value as? T? }
    ): T? = entityValue(entity.role, valueTransformer)

    /**
     * Returns the current text content for the specified entity.
     */
    fun entityText(entity: Entity): String? = entityContextValue(entity)?.content

    /**
     * Returns the current text content for the specified entity.
     */
    fun entityText(role: String): String? = entityContextValue(role)?.content

    /**
     * Returns the current entity ContextValue.
     */
    fun entityContextValue(entity: Entity): ContextValue? = entityContextValue(entity.role)

    /**
     * Returns the current entity ContextValue.
     */
    fun entityContextValue(role: String): ContextValue? = entities[role]?.value

    /**
     * Updates the current entity value in the dialog.
     * @param role entity role
     * @param newValue the new entity value
     */
    fun changeEntityValue(role: String, newValue: ContextValue?) {
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
     * @param newValue the new entity context value
     */
    fun changeEntityValue(entity: Entity, newValue: ContextValue) = changeEntityValue(entity.role, newValue)

    /**
     * Updates the current entity text value in the dialog.
     * @param entity the entity definition
     * @param textContent the new entity text content
     */
    fun changeEntityValue(entity: Entity, textContent: String) =
        changeEntityValue(
            entity.role,
            ContextValue(entity, null, textContent)
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
     * Resets all entity values, context values, [fr.vsct.tock.bot.engine.dialog.DialogState.userLocation]
     * and [fr.vsct.tock.bot.engine.dialog.DialogState.nextActionState]
     * but keep entity values history.
     * @see [fr.vsct.tock.bot.engine.dialog.DialogState.resetState]
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
    fun <T : Any> contextValue(key: ParameterKey): T? = contextValue(key.keyName)

    /**
     * Updates persistent context value.
     */
    fun changeContextValue(name: String, value: Any?) {
        if (value == null) dialog.state.context.remove(name) else dialog.state.context[name] = value
    }

    /**
     * Updates persistent context value.
     */
    fun changeContextValue(key: ParameterKey, value: Any?) = changeContextValue(key.keyName, value)

    /**
     * Returns the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun getBusContextValue(name: String): Any?

    /**
     * Returns the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun getBusContextValue(key: ParameterKey): Any? = getBusContextValue(key.keyName)

    /**
     * Updates the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun setBusContextValue(key: String, value: Any?)

    /**
     * Updates the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun setBusContextValue(key: ParameterKey, value: Any?) = setBusContextValue(key.keyName, value)

    /**
     * Send previously registered [ConnectorMessage] as last bot answer.
     */
    fun end(delay: Long = 0): BotBus {
        return endRawText(null, delay)
    }

    /**
     * Sends i18nText as last bot answer.
     */
    fun end(i18nText: CharSequence, delay: Long = 0, vararg i18nArgs: Any?): BotBus {
        return endRawText(translate(i18nText, *i18nArgs), delay)
    }

    /**
     * Sends i18nText as last bot answer.
     */
    fun end(i18nText: CharSequence, vararg i18nArgs: Any?): BotBus {
        return endRawText(translate(i18nText, *i18nArgs))
    }

    /**
     * Sends text that should not be translated as last bot answer.
     */
    fun endRawText(plainText: CharSequence?, delay: Long = 0): BotBus {
        return end(SendSentence(botId, applicationId, userId, plainText), delay)
    }

    /**
     * Sends [Message] as last bot answer.
     */
    fun end(message: Message, delay: Long = 0): BotBus {
        return end(message.toAction(this), delay)
    }

    /**
     * Sends [Action] as last bot answer.
     */
    fun end(action: Action, delay: Long = 0): BotBus

    /**
     * Sends [MessagesList] as last bot answer.
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
     * Sends i18nText.
     */
    fun send(i18nText: CharSequence, delay: Long = 0, vararg i18nArgs: Any?): BotBus {
        return sendRawText(translate(i18nText, *i18nArgs), delay)
    }

    /**
     * Sends i18nText.
     */
    fun send(i18nText: CharSequence, vararg i18nArgs: Any?): BotBus {
        return sendRawText(translate(i18nText, *i18nArgs))
    }

    /**
     * Sends previously registered [ConnectorMessage].
     */
    fun send(delay: Long = 0): BotBus {
        return sendRawText(null, delay)
    }

    /**
     * Send text that should not be translated.
     */
    fun sendRawText(plainText: CharSequence?, delay: Long = 0): BotBus

    /**
     * Sends [Message].
     */
    fun send(message: Message, delay: Long = 0): BotBus {
        return send(message.toAction(this), delay)
    }

    /**
     * Sends [Action].
     */
    fun send(action: Action, delay: Long = 0): BotBus

    /**
     * Adds the specified [ActionPriority] to the bus context.
     */
    fun withPriority(priority: ActionPriority): BotBus

    /**
     * Adds the specified [ActionNotificationType] to the bus context.
     */
    fun withNotificationType(notificationType: ActionNotificationType): BotBus

    /**
     * Adds the specified [ConnectorMessage] to the bus context if the [targetConnectorType] is compatible.
     */
    fun withMessage(message: ConnectorMessage): BotBus = withMessage(message.connectorType, { message })

    /**
     * Adds the specified [ConnectorMessage] to the bus context if the [targetConnectorType] is compatible.
     */
    fun withMessage(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): BotBus

    /**
     * Reloads the user profile.
     */
    fun reloadProfile()

    /**
     * Switches the context to the underlying story definition (start a new [Story]).
     */
    fun switchStory(storyDefinition: StoryDefinition) {
        val starterIntent = storyDefinition.mainIntent()
        story = Story(storyDefinition, starterIntent)
        dialog.state.currentIntent = starterIntent
    }

    //i18n provider implementation
    override fun i18n(defaultLabel: CharSequence, args: List<Any?>): I18nLabelValue =
        i18nProvider.i18n(defaultLabel, args)

    //I18nTranslator implementation
    override val contextId: String? get() = dialog.id.toString()
}