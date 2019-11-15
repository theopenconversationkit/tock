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

package ai.tock.bot.api.client

import ai.tock.bot.api.model.BotResponse
import ai.tock.bot.api.model.ResponseContext
import ai.tock.bot.api.model.UserRequest
import ai.tock.bot.api.model.context.Entity
import ai.tock.bot.api.model.message.bot.BotMessage
import ai.tock.bot.api.model.message.bot.Card
import ai.tock.bot.api.model.message.bot.Carousel
import ai.tock.bot.api.model.message.bot.CustomMessage
import ai.tock.bot.api.model.message.bot.I18nText
import ai.tock.bot.api.model.message.bot.Sentence
import ai.tock.bot.api.model.message.user.UserMessage
import ai.tock.bot.api.model.websocket.RequestData
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.jackson.ConstrainedValueWrapper
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.RawString
import ai.tock.translator.TranslatedString
import ai.tock.translator.UserInterfaceType
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

class TockClientBus(
    override val botDefinition: ClientBotDefinition,
    val requestId: String,
    val request: UserRequest,
    val sendAnswer: (BotResponse) -> Unit
) : ClientBus {

    constructor(botDefinition: ClientBotDefinition, data: RequestData, sendAnswer: (BotResponse) -> Unit) :
        this(botDefinition, data.requestId, data.botRequest!!, sendAnswer)

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
    override val entities: MutableList<Entity> = request.entities.toMutableList()
    override val message: UserMessage = request.message

    private val context = ClientBusContext()
    private val messages: MutableList<BotMessage> = CopyOnWriteArrayList()

    override lateinit var story: ClientStoryDefinition
    override var step: ClientStep? = null

    override val stepName: String? = null

    override fun handle() {
        story =
            botDefinition.stories.find { it.storyId == request.storyId }
                ?: botDefinition.stories.find { intent != null && it.isStarterIntent(intent.wrappedIntent()) }
                    ?: botDefinition.unknownStory
        step = story.steps.find { it.name == request.step }
        story.handler.handle(this)
    }

    override fun defaultDelay(answerIndex: Int): Long = 0

    private fun addMessage(plainText: CharSequence?, delay: Long) {
        context.connectorMessages.remove(targetConnectorType)?.also {
            messages.add(CustomMessage(ConstrainedValueWrapper(it), delay))
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
        answer(messages)
        return this
    }

    override fun sendRawText(plainText: CharSequence?, delay: Long): ClientBus {
        addMessage(plainText, delay)
        return this
    }

    override fun end(card: Card): ClientBus {
        send(card)
        answer(messages)
        return this
    }

    override fun end(carousel: Carousel): ClientBus {
        send(carousel)
        answer(messages)
        return this
    }

    private fun answer(messages: List<BotMessage>) {
        sendAnswer(
            BotResponse(
                messages,
                story.storyId,
                step?.name,
                entities,
                ResponseContext(requestId)
            )
        )
    }

    override fun send(carousel: Carousel): ClientBus {
        messages.add(carousel)
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