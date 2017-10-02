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

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.ParameterKey
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionSignificance
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
import fr.vsct.tock.translator.I18nLabelKey
import fr.vsct.tock.translator.RawString
import fr.vsct.tock.translator.TranslatedString
import fr.vsct.tock.translator.Translator
import fr.vsct.tock.translator.UserInterfaceType
import java.util.Locale

/**
 * The main interface to build a response to a user query.
 */
interface BotBus {

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
     * The last user action.
     */
    val action: Action

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
    val userLocale: Locale
    /**
     * The current user interface type.
     */
    val userInterfaceType: UserInterfaceType
    /**
     * The [ConnectorType] used by the response.
     */
    val targetConnectorType: ConnectorType

    /**
     * The entities in the dialog state.
     */
    val entities: Map<String, EntityStateValue>
    /**
     * The current intent.
     */
    val intent: Intent?

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
    var step: StoryStep?
        get() = story.findCurrentStep()
        set(step) {
            story.currentStep = step?.name
        }

    /**
     * To test if the current intent is owned by the [IntentAware].
     */
    fun isIntent(intentOwner: IntentAware): Boolean = intentOwner.wrap(intent)

    /**
     * Get the NLP call stats if an NLP call has occurred, null either.
     */
    fun nlpStats(): NlpCallStats? = if (action is SendSentence) (action as SendSentence).nlpStats else null

    /**
     * Returns the value of the specified choice parameter, null if the user action is not a [SendChoice]
     * or if this parameter is not set.
     */
    fun paramChoice(paramName: String): String? {
        return if (action is SendChoice) {
            (action as SendChoice).parameters[paramName]
        } else {
            null
        }
    }

    /**
     * Returns the value of the specified choice parameter, null if the user action is not a [SendChoice]
     * or if this parameter is not set.
     */
    fun paramChoice(key: ParameterKey): String?
            = paramChoice(key.keyName)

    /**
     * Returns the value of the specified choice parameter, null if the user action is not a [SendChoice]
     * or if this parameter is not set.
     */
    fun choice(key: ParameterKey): String?
            = paramChoice(key.keyName)

    /**
     * Returns true if the specified choice as the "true" value, false either.
     */
    fun booleanChoice(key: ParameterKey): Boolean
            = choice(key).equals("true", true)

    /**
     * Returns true if the current action has the specified entity role.
     */
    fun hasActionEntity(role: String): Boolean {
        return action.hasEntity(role)
    }

    /**
     * Returns true if the current action has the specified entity role.
     */
    fun hasActionEntity(entity: Entity): Boolean
            = hasActionEntity(entity.role)

    /**
     * Returns the current value for the specified entity role.
     */
    fun <T : Value> entityValue(role: String): T? {
        @Suppress("UNCHECKED_CAST")
        return entities[role]?.value?.value as T?
    }

    /**
     * Returns the current value for the specified entity.
     */
    fun <T : Value> entityValue(entity: Entity): T?
            = entityValue(entity.role)

    /**
     * Returns the current text content for the specified entity.
     */
    fun entityText(entity: Entity): String?
            = entityContextValue(entity)?.content

    /**
     * Returns the current entity ContextValue.
     */
    fun entityContextValue(entity: Entity): ContextValue?
            = entities[entity.role]?.value

    /**
     * Update the current entity value in the dialog.
     * @param role entity role
     * @param newValue the new entity value
     */
    fun changeEntityValue(role: String, newValue: ContextValue?) {
        dialog.state.changeValue(role, newValue)
    }

    /**
     * Update the current entity value in the dialog.
     * @param entity the entity definition
     * @param newValue the new entity value
     */
    fun changeEntityValue(entity: Entity, newValue: Value?) {
        dialog.state.changeValue(entity, newValue)
    }

    /**
     * Update the current entity value in the dialog.
     * @param entity the entity definition
     * @param newValue the new entity context value
     */
    fun changeEntityValue(entity: Entity, newValue: ContextValue)
            = changeEntityValue(entity.role, newValue)

    /**
     * Update the current entity text value in the dialog.
     * @param entity the entity definition
     * @param textContent the new entity text content
     */
    fun changeEntityValue(entity: Entity, textContent: String) =
            changeEntityValue(
                    entity.role,
                    ContextValue(entity, null, textContent))

    /**
     * Remove entity value for the specified role.
     */
    fun removeEntityValue(role: String) {
        dialog.state.removeValue(role)
    }

    /**
     * Remove entity value for the specified role.
     */
    fun removeEntityValue(entity: Entity)
            = removeEntityValue(entity.role)

    /**
     * Remove all current entity values.
     */
    fun removeAllEntityValues() {
        dialog.state.removeAllEntityValues()
    }

    /**
     * Remove all entities and context values.
     */
    fun resetDialogState() {
        dialog.state.resetState()
    }

    /**
     * Returns the persistent current context value.
     */
    fun <T : Any> contextValue(name: String): T? {
        @Suppress("UNCHECKED_CAST")
        return dialog.state.context[name] as T?
    }

    /**
     * Returns the persistent current context value.
     */
    fun <T : Any> contextValue(key: ParameterKey): T?
            = contextValue(key.keyName)

    /**
     * Update persistent context value.
     */
    fun changeContextValue(name: String, value: Any?) {
        if (value == null) dialog.state.context.remove(name) else dialog.state.context[name] = value
    }

    /**
     * Update persistent context value.
     */
    fun changeContextValue(key: ParameterKey, value: Any?)
            = changeContextValue(key.keyName, value)

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
     * Update the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun setBusContextValue(key: String, value: Any?)

    /**
     * Update the non persistent current context value.
     * Bus context values are useful to store a temporary (ie request scoped) state.
     */
    fun setBusContextValue(key: ParameterKey, value: Any?) = setBusContextValue(key.keyName, value)

    fun end(delay: Long = 0): BotBus {
        return endRawText(null, delay)
    }

    fun end(i18nText: CharSequence, delay: Long = 0, vararg i18nArgs: Any?): BotBus {
        return endRawText(translate(i18nText, *i18nArgs), delay)
    }

    fun end(i18nText: CharSequence, vararg i18nArgs: Any?): BotBus {
        return endRawText(translate(i18nText, *i18nArgs))
    }

    /**
     * Send text that should not be translated, and terminate bot actions.
     */
    fun endRawText(plainText: CharSequence?, delay: Long = 0): BotBus {
        return end(SendSentence(botId, applicationId, userId, plainText), delay)
    }

    fun end(message: Message, delay: Long = 0): BotBus {
        return end(message.toAction(this), delay)
    }

    fun end(action: Action, delay: Long = 0): BotBus

    fun end(messages: MessagesList, initialDelay: Long = 0): BotBus {
        messages.messages.forEachIndexed { i, m ->
            val wait = initialDelay + m.delay
            if (messages.messages.size - 1 == i) {
                end(m.toAction(this), wait)
            } else {
                send(m.toAction(this), wait)
            }
        }
        return this;
    }


    fun send(i18nText: CharSequence, delay: Long = 0, vararg i18nArgs: Any?): BotBus {
        return sendRawText(translate(i18nText, *i18nArgs), delay)
    }

    fun send(i18nText: CharSequence, vararg i18nArgs: Any?): BotBus {
        return sendRawText(translate(i18nText, *i18nArgs))
    }

    fun send(delay: Long = 0): BotBus {
        return sendRawText(null, delay)
    }

    /**
     * Send text that should not be translated.
     */
    fun sendRawText(plainText: CharSequence?, delay: Long = 0): BotBus

    fun send(message: Message, delay: Long = 0): BotBus {
        return send(message.toAction(this), delay)
    }

    fun send(action: Action, delay: Long = 0): BotBus

    /**
     * Add the specified [ActionSignificance] to the bus context.
     */
    fun with(significance: ActionSignificance): BotBus

    /**
     * Add the specified [ConnectorMessage] to the bus context if the [targetConnectorType] is compatible.
     */
    @Deprecated("use with(connectorType, messageProvider) version")
    fun with(message: ConnectorMessage): BotBus
            = with(message.connectorType, { message })

    /**
     * Add the specified [ConnectorMessage] to the bus context if the [targetConnectorType] is compatible.
     */
    fun with(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): BotBus

    /**
     * Translate and format if needed the text with the optionals args.
     */
    fun translate(text: CharSequence?, vararg args: Any?): CharSequence {
        return if (text.isNullOrBlank()) {
            ""
        } else if (text is I18nLabelKey) {
            translate(text)
        } else if (text is TranslatedString || text is RawString) {
            text
        } else {
            return translate(i18nProvider.i18nKeyFromLabel(text!!, args.toList()))
        }
    }

    /**
     * Translate the specified key.
     */
    fun translate(key: I18nLabelKey?): CharSequence =
            if (key == null) ""
            else Translator.translate(
                    key,
                    userTimeline.userPreferences.locale,
                    userInterfaceType
            )

    /**
     * Reload the user profile.
     */
    fun reloadProfile()

    /**
     * Switch the context to the underlying story definition (start a new [Story]).
     */
    fun switchStory(storyDefinition: StoryDefinition) {
        val starterIntent = storyDefinition.mainIntent()
        story = Story(storyDefinition, starterIntent)
        dialog.state.currentIntent = starterIntent
    }

}