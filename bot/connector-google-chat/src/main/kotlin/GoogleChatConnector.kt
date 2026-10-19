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
package ai.tock.bot.connector.googlechat

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.googlechat.builder.ChatButtons
import ai.tock.bot.connector.googlechat.builder.ChatCard
import ai.tock.bot.connector.googlechat.builder.card
import ai.tock.bot.connector.media.MediaAction
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.event.Event
import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.databind.JsonNode
import com.github.salomonbrys.kodein.instance
import com.google.api.services.chat.v1.HangoutsChat
import com.google.api.services.chat.v1.model.Message
import mu.KotlinLogging
import java.time.Duration

class GoogleChatConnector(
    private val connectorId: String,
    private val path: String,
    private val chatService: HangoutsChat,
    private val authorisationHandler: GoogleChatAuthorisationHandler,
    private val useCondensedFootnotes: Boolean,
    private val displaySourcesWithoutUrl: Boolean,
    private val introMessage: String? = null,
    private val useThread: Boolean = false,
    private val sourcesLabel: String,
    private val waitingMessage: String,
    private val feedback: GoogleChatFeedback? = null,
    private val enableStreaming: Boolean = false,
) : ConnectorBase(GoogleChatConnectorProvider.connectorType) {
    private val logger = KotlinLogging.logger {}
    private val executor: Executor by injector.instance()

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            router
                .post(path)
                .handler(authorisationHandler)
                .handler { context ->
                    try {
                        val body = context.body().asString()
                        logger.debug { "message received from Google chat: $body" }

                        // answer immediately
                        context
                            .response()
                            .putHeader("Content-Type", "application/json; charset=UTF-8")
                            .setStatusCode(200)
                            .end("{}")
                        val messageEvent: JsonNode = mapper.readTree(body)
                        val chatEvent: JsonNode = messageEvent.required("chat")

                        // https://developers.google.com/workspace/add-ons/concepts/event-objects#chat-payload
                        if (chatEvent.has("messagePayload")) {
                            handleMessage(chatEvent, controller)
                        } else if (chatEvent.has("buttonClickedPayload") && feedback != null) {
                            handleFeedback(messageEvent, chatEvent, controller)
                        } else {
                            logger.debug {
                                "Unsupported Google Chat event skipped. " +
                                    "Feedback button events are handled only when feedback is enabled."
                            }
                        }
                    } catch (e: Throwable) {
                        logger.error(e) { "Error while handling Google Chat event" }
                    }
                }
        }
    }

    private fun handleMessage(
        chatEvent: JsonNode,
        controller: ConnectorController,
    ) {
        val message = chatEvent.required("messagePayload").required("message")
        val spaceName = message.required("space").required("name").asText()
        val threadName = message.required("thread").required("name").asText()
        val event = GoogleChatRequestConverter.toEvent(chatEvent, connectorId)
        if (event is Action && enableStreaming) {
            event.metadata.streamedResponse = true
        }
        executor.executeBlocking {
            val callback =
                GoogleChatConnectorCallback(
                    connectorId,
                    spaceName,
                    threadName,
                    chatService,
                    introMessage,
                    useThread,
                    waitingMessage,
                    feedback,
                )

            callback.initializeProcessingMessage()
            controller.handle(event, ConnectorData(callback))
        }
    }

    private fun handleFeedback(
        messageEvent: JsonNode,
        chatEvent: JsonNode,
        controller: ConnectorController,
    ) {
        val request = GoogleChatRequestConverter.toFeedbackRequest(messageEvent, chatEvent, connectorId)
        if (request == null) {
            logger.debug { "Ignoring unsupported Google Chat button action" }
            return
        }

        executor.executeBlocking {
            controller.handle(request.event)
            patchFeedbackAcknowledgement(request)
        }
    }

    private fun patchFeedbackAcknowledgement(request: GoogleChatFeedbackRequest) {
        try {
            chatService
                .spaces()
                .messages()
                .patch(
                    request.messageName,
                    Message().setAccessoryWidgets(feedback?.acknowledgement(request.vote, request.event.actionId)),
                ).setUpdateMask("accessoryWidgets")
                .execute()
        } catch (e: Exception) {
            logger.error(e) { "Failed to acknowledge feedback on Google Chat message: ${request.messageName}" }
        }
    }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        logger.debug { "event: $event" }
        if (event !is Action) return

        val message =
            GoogleChatMessageConverter.toMessageOut(event, useCondensedFootnotes, displaySourcesWithoutUrl, sourcesLabel)
                ?: return

        callback as GoogleChatConnectorCallback

        executor.executeBlocking(Duration.ofMillis(delayInMs)) {
            val processingMessageName = callback.processingMessageName

            if (processingMessageName != null) {
                callback.patchGoogleMessage(processingMessageName, message, event.id.toString())
                callback.processingMessageName = null
            } else {
                callback.sendGoogleMessageAndGetName(message, event.id.toString())
            }
        }
    }

    override fun addSuggestions(
        text: CharSequence,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            card {
                section {
                    textParagraph(text)
                    buttons {
                        suggestions.map { nlpTextButton(it) }
                    }
                }
            }
        }

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            when (message) {
                is GoogleChatConnectorCardMessageOut -> {
                    if (message.card.hasChatButtons()) {
                        message
                    } else {
                        message.apply {
                            card.section {
                                buttons { suggestions.map { nlpTextButton(it) } }
                            }
                        }
                    }
                }

                is GoogleChatConnectorTextMessageOut -> {
                    card {
                        section {
                            textParagraph(message.text)
                            buttons {
                                suggestions.map { nlpTextButton(it) }
                            }
                        }
                    }
                }

                else -> {
                    message.also { logger.warn { "Add suggestion to message $message not handled" } }
                }
            }
        }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        {
            when (message) {
                is MediaAction -> {
                    listOf(
                        card {
                            section {
                                buttons {
                                    buttonFromMediaAction(message)
                                }
                            }
                        },
                    )
                }

                is MediaCard -> {
                    listOf(
                        card {
                            sectionFromMediaCard(message)
                        },
                    )
                }

                is MediaCarousel -> {
                    listOf(
                        card {
                            message.cards.forEach { sectionFromMediaCard(it) }
                        },
                    )
                }

                else -> {
                    emptyList()
                }
            }
        }

    private fun ChatCard.sectionFromMediaCard(message: MediaCard) {
        section {
            message.title?.let { title ->
                keyValue(content = title, bottomLabel = message.subTitle)
            }
            if (message.actions.isNotEmpty()) {
                buttons {
                    message.actions.forEach {
                        buttonFromMediaAction(it)
                    }
                }
            }
            // TODO : handle MediaFile
        }
    }

    private fun ChatButtons.buttonFromMediaAction(message: MediaAction) {
        textButton(message.title) {
            val url = message.url
            if (url != null) {
                link(url)
            } else {
                nlpAction(message.title)
            }
        }
    }
}
