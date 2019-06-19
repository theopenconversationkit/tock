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

    private fun addMessage(plainText: CharSequence?) {
        context.connectorMessages.remove(targetConnectorType)?.also {
            messages.add(CustomMessage(it))
        }
        if (plainText != null) {
            when (plainText) {
                is String -> messages.add(Sentence(I18nText(plainText)))
                is TranslatedString, is RawString -> messages.add(Sentence(I18nText(plainText.toString(), toBeTranslated = false)))
                else -> messages.add(Sentence(I18nText(plainText.toString())))
            }
        }
    }

    override fun endRawText(plainText: CharSequence?, delay: Long): ClientBus {
        addMessage(plainText)
        sendAnswer(messages)
        return this
    }

    override fun sendRawText(plainText: CharSequence?, delay: Long): ClientBus {
        addMessage(plainText)
        return this
    }

    override fun withMessage(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): ClientBus {
        if (targetConnectorType == connectorType) {
            context.connectorMessages[connectorType] = messageProvider()
        }
        return this
    }

    override fun translate(text: CharSequence?, vararg args: Any?): CharSequence {
        return if (text.isNullOrBlank()) {
            ""
        } else if (text is I18nLabelValue) {
            translate(text)
        } else if (text is TranslatedString || text is RawString) {
            text
        } else {
            return I18nText(text.toString(), args.map { it.toString() })
        }
    }

    override fun i18n(defaultLabel: CharSequence, args: List<Any?>): I18nLabelValue {
        error("not yet supported")
    }
}