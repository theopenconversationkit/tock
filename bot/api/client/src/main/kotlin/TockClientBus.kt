/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import ai.tock.bot.api.model.message.bot.Debug
import ai.tock.bot.api.model.message.bot.I18nText
import ai.tock.bot.api.model.message.bot.Sentence
import ai.tock.bot.api.model.message.bot.Suggestion
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
import ai.tock.translator.TranslatedSequence
import ai.tock.translator.UserInterfaceType
import ai.tock.translator.raw
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

    // Source connector : is the connector which initialize a conversation
    override val sourceConnectorType: ConnectorType = request.context.sourceConnectorType

    // Target connector : is the connector for which the message is produced
    override val targetConnectorType: ConnectorType = request.context.targetConnectorType

    override val contextId: String? = request.context.userId.id
    private var _currentAnswerIndex: Int = 0
    override val currentAnswerIndex: Int get() = _currentAnswerIndex
    override val entities: MutableList<Entity> = request.entities.toMutableList()
    override val message: UserMessage = request.message

    private val context = ClientBusContext()

    override lateinit var story: ClientStoryDefinition
    override var step: ClientStep? = null

    override val stepName: String? = null

    override fun handle() {
        story =
            if (request.storyId == botDefinition.unknownStory.storyId) {
                botDefinition.unknownStory
            } else {
                botDefinition.stories.find { it.storyId == request.storyId }
                    ?: botDefinition.stories.find { intent != null && it.isStarterIntent(intent.wrappedIntent()) }
                    ?: botDefinition.unknownStory
            }
        step = story.steps.find { it.name == request.step }
        story.handler.handle(this)
    }

    override fun defaultDelay(answerIndex: Int): Long = 0

    private fun addMessage(message: BotMessage?, lastResponse: Boolean = false) {
        if (message != null) {
            answer(message, lastResponse)
        }
    }

    private fun addMessage(
        plainText: CharSequence?,
        delay: Long = 0,
        suggestions: List<Suggestion> = emptyList(),
        lastResponse: Boolean = false
    ) {
        context.connectorMessages.remove(targetConnectorType)?.also {
            answer(CustomMessage(ConstrainedValueWrapper(it), delay), lastResponse && plainText == null)
        }
        if (plainText != null) {
            answer(
                when (plainText) {
                    is String -> Sentence(I18nText(plainText), delay = delay, suggestions = suggestions)
                    is TranslatedSequence -> Sentence(
                        I18nText(plainText.toString(), toBeTranslated = false),
                        delay = delay,
                        suggestions = suggestions
                    )

                    is I18nText -> Sentence(plainText, delay = delay, suggestions = suggestions)
                    else -> Sentence(I18nText(plainText.toString()), delay = delay, suggestions = suggestions)
                },
                lastResponse
            )
        }
    }

    override fun endRawText(plainText: CharSequence?, delay: Long): ClientBus {
        addMessage(plainText, delay, lastResponse = true)
        return this
    }

    override fun sendRawText(plainText: CharSequence?, delay: Long): ClientBus {
        addMessage(plainText?.raw, delay)
        return this
    }

    /**
     * Add a debug data message for the test connector only
     * @param title title of debug message
     * @param data object corresponding to the debugging data
     */
    override fun sendDebugData(title: String, data: Any?): ClientBus {
        // The test connector is a rest connector (source),
        // but it invokes the engine with a target connector,
        // to receive the corresponding messages
        if (ConnectorType.rest == sourceConnectorType) {
            addMessage(Debug(title, data))
        }
        return this
    }

    override fun send(
        i18nText: CharSequence,
        suggestions: List<Suggestion>,
        delay: Long,
        vararg i18nArgs: Any?
    ): ClientBus {
        addMessage(translate(i18nText, i18nArgs), delay, suggestions)
        return this
    }

    override fun end(
        i18nText: CharSequence,
        suggestions: List<Suggestion>,
        delay: Long,
        vararg i18nArgs: Any?
    ): ClientBus {
        addMessage(translate(i18nText, i18nArgs), delay, suggestions, lastResponse = true)
        return this
    }

    override fun end(card: Card): ClientBus {
        addMessage(card, lastResponse = true)
        return this
    }

    override fun end(carousel: Carousel): ClientBus {
        addMessage(carousel, lastResponse = true)
        return this
    }

    private fun answer(message: BotMessage, lastResponse: Boolean = false) =
        answer(listOf(message), lastResponse)

    private fun answer(messages: List<BotMessage>, lastResponse: Boolean = false) {
        sendAnswer(
            BotResponse(
                messages,
                story.storyId,
                step?.name,
                entities,
                ResponseContext(
                    requestId = requestId,
                    lastResponse = lastResponse
                )
            )
        )
    }

    override fun send(carousel: Carousel): ClientBus {
        addMessage(carousel)
        return this
    }

    override fun send(card: Card): ClientBus {
        addMessage(card)
        return this
    }

    override fun withMessage(connectorType: ConnectorType, messageProvider: () -> ConnectorMessage): ClientBus {
        if (targetConnectorType == connectorType) {
            context.connectorMessages[connectorType] = messageProvider()
        }
        return this
    }

    override fun withMessage(
        connectorType: ConnectorType,
        connectorId: String,
        messageProvider: () -> ConnectorMessage
    ): ClientBus {
        if (applicationId == connectorId && targetConnectorType == connectorType) {
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
            args
        )
    }
}
