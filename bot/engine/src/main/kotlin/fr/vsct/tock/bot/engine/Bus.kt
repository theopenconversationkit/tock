/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.StoryHandlerDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.PlayerId

/**
 * Base methods for the bus.
 */
interface Bus<T : Bus<T>> : I18nTranslator {

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
     * The current intent.
     */
    val intent: IntentAware?

    /**
     * The current answer index of the bot for this action.
     */
    val currentAnswerIndex: Int

    /**
     * Is it a test mode ?
     */
    val test: Boolean

    /**
     * The current [StoryStep] of the [Story].
     */
    var step: StoryStep<out StoryHandlerDefinition>?

    /**
     * Get the default delay between two answers.
     */
    fun defaultDelay(answerIndex: Int): Long

    /**
     * Adds the specified [ConnectorMessage] to the bus context if the [targetConnectorType] is compatible.
     */
    fun withMessage(message: ConnectorMessage): T = withMessage(message.connectorType) { message }

    /**
     * Adds the specified [ConnectorMessage] to the bus context if the [targetConnectorType] is compatible.
     */
    fun withMessage(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): T

    /**
     * Sends previously registered [ConnectorMessage].
     */
    fun send(delay: Long = defaultDelay(currentAnswerIndex)): T {
        return sendRawText(null, delay)
    }

    /**
     * Sends i18nText.
     */
    fun send(
        i18nText: CharSequence,
        delay: Long = defaultDelay(currentAnswerIndex),
        vararg i18nArgs: Any?
    ): T {
        return sendRawText(translate(i18nText, *i18nArgs), delay)
    }

    /**
     * Sends i18nText.
     */
    fun send(i18nText: CharSequence, vararg i18nArgs: Any?): T {
        return sendRawText(translate(i18nText, *i18nArgs))
    }

    /**
     * Sends messages provided by [messageProvider].
     * if [messageProvider] returns a [CharSequence] send it as text. Else call simply send().
     */
    fun send(
        delay: Long = defaultDelay(currentAnswerIndex),
        messageProvider: T.() -> Any?
    ): T {
        @Suppress("UNCHECKED_CAST")
        this as T
        val r = messageProvider(this)
        if (r is CharSequence) {
            send(r, delay)
        } else {
            send(delay)
        }
        return this
    }

    /**
     * Send text that should not be translated.
     */
    fun sendRawText(plainText: CharSequence?, delay: Long = defaultDelay(currentAnswerIndex)): T

    /**
     * Sends i18nText as last bot answer.
     */
    fun end(
        i18nText: CharSequence,
        delay: Long = defaultDelay(currentAnswerIndex),
        vararg i18nArgs: Any?
    ): T {
        return endRawText(translate(i18nText, *i18nArgs), delay)
    }

    /**
     * Sends i18nText as last bot answer.
     */
    fun end(i18nText: CharSequence, vararg i18nArgs: Any?): T {
        return endRawText(translate(i18nText, *i18nArgs))
    }

    /**
     * Send previously registered [ConnectorMessage] as last bot answer.
     */
    fun end(delay: Long = defaultDelay(currentAnswerIndex)): T {
        return endRawText(null, delay)
    }

    /**
     * Sends messages provided by [messageProvider] as last bot answer.
     * if [messageProvider] returns a [CharSequence] send it as text. Else call simply end().
     */
    fun end(
        delay: Long = defaultDelay(currentAnswerIndex),
        messageProvider: T.() -> Any?
    ): T {
        @Suppress("UNCHECKED_CAST")
        this as T
        val r = messageProvider(this)
        if (r is CharSequence) {
            end(r, delay)
        } else {
            end(delay)
        }
        return this
    }

    /**
     * Sends text that should not be translated as last bot answer.
     */
    fun endRawText(plainText: CharSequence?, delay: Long = defaultDelay(currentAnswerIndex)): T
}