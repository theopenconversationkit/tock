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

package fr.vsct.tock.bot.api.client

import fr.vsct.tock.bot.api.model.UserRequest
import fr.vsct.tock.bot.api.model.context.Entity
import fr.vsct.tock.bot.api.model.message.bot.BotMessage
import fr.vsct.tock.bot.api.model.message.bot.Card
import fr.vsct.tock.bot.api.model.message.bot.CustomMessage
import fr.vsct.tock.bot.api.model.message.bot.I18nText
import fr.vsct.tock.bot.api.model.message.bot.Sentence
import fr.vsct.tock.bot.api.model.message.user.UserMessage
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.StoryHandlerDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.shared.jackson.AnyValueWrapper
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.I18nLabelValue
import fr.vsct.tock.translator.RawString
import fr.vsct.tock.translator.TranslatedString
import fr.vsct.tock.translator.UserInterfaceType
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

class TockClientBus(
    override val botDefinition: ClientBotDefinition,
    val request: UserRequest,
    val sendAnswer: (List<BotMessage>) -> Unit
) : ClientBus {

    override val applicationId: String = request.context.applicationId
    override val userId: PlayerId = request.context.userId
    override val botId: PlayerId = request.context.botId
    override val intent: IntentAware? = request.intent?.let { Intent(it) }
    override val test: Boolean = request.context.user.test
    override val userLocale: Locale = request.context.language
    override val userInterfaceType: UserInterfaceType = request.context.userInterface
    override val targetConnectorType: ConnectorType = request.context.connectorType
    override val contextId: String? = request.context.userId.id
    private var _currentAnswerIndex: Int = 0
    override val currentAnswerIndex: Int get() = _currentAnswerIndex
    override val entities: List<Entity> = request.entities
    override val message: UserMessage = request.message

    private val context = ClientBusContext()
    private val messages: MutableList<BotMessage> = CopyOnWriteArrayList()

    //TODO
    override var step: StoryStep<out StoryHandlerDefinition>?
        get() = null
        set(value) {}

    override fun defaultDelay(answerIndex: Int): Long = 0

    private fun addMessage(plainText: CharSequence?, delay: Long) {
        context.connectorMessages.remove(targetConnectorType)?.also {
            messages.add(CustomMessage(AnyValueWrapper(it), delay))
        }
        if (plainText != null) {
            messages.add(
                when (plainText) {
                    is String -> Sentence(I18nText(plainText), delay = delay)
                    is TranslatedString, is RawString -> Sentence(I18nText(plainText.toString(), toBeTranslated = false), delay = delay)
                    is I18nText -> Sentence(plainText, delay = delay)
                    else -> Sentence(I18nText(plainText.toString()), delay = delay)
                }
            )
        }
    }

    override fun endRawText(plainText: CharSequence?, delay: Long): ClientBus {
        addMessage(plainText, delay)
        sendAnswer(messages)
        return this
    }

    override fun sendRawText(plainText: CharSequence?, delay: Long): ClientBus {
        addMessage(plainText, delay)
        return this
    }

    override fun end(card: Card): ClientBus {
        send(card)
        sendAnswer(messages)
        return this
    }

    override fun send(card: Card): ClientBus {
        messages.add(card)
        return this
    }

    override fun withMessage(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): ClientBus {
        if (targetConnectorType == connectorType) {
            context.connectorMessages[connectorType] = messageProvider()
        }
        return this
    }

    override fun i18n(defaultLabel: CharSequence, args: List<Any?>): I18nLabelValue {
        val namespace = request.context.namespace
        val category = intent?.wrappedIntent()?.name ?: namespace
        return I18nLabelValue(
            I18nKeyProvider.generateKey(namespace, category, defaultLabel),
            namespace,
            category,
            defaultLabel,
            args)
    }
}