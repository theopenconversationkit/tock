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
import com.github.salomonbrys.kodein.instance
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.chat.v1.HangoutsChat
import com.google.api.services.chat.v1.model.DeprecatedEvent
import com.google.api.services.chat.v1.model.Thread
import mu.KotlinLogging
import java.time.Duration

class GoogleChatConnector(
    private val connectorId: String,
    private val path: String,
    private val chatService: HangoutsChat,
    private val authorisationHandler: GoogleChatAuthorisationHandler,
    private val useCondensedFootnotes: Boolean
) : ConnectorBase(GoogleChatConnectorProvider.connectorType) {

    private val logger = KotlinLogging.logger {}
    private val executor: Executor by injector.instance()

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            router.post(path)
                .handler(authorisationHandler)
                .handler { context ->
                    try {
                        val body = context.body().asString()
                        logger.info { "message received from Google chat: $body" }

                        // answer immediately
                        context.response().end()

                        val messageEvent = JacksonFactory().fromString(body, DeprecatedEvent::class.java)
                        val spaceName = messageEvent.space?.name
                        val threadName = messageEvent.message?.thread?.name
                        val event = GoogleChatRequestConverter.toEvent(messageEvent, connectorId)
                        if (event != null && spaceName != null && threadName != null) {
                            executor.executeBlocking {
                                controller.handle(
                                    event,
                                    ConnectorData(GoogleChatConnectorCallback(connectorId, spaceName, threadName))
                                )
                            }
                        } else {
                            logger.debug { "skip message: $messageEvent" }
                        }
                    } catch (e: Throwable) {
                        logger.error { e }
                    }
                }
        }
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        logger.debug { "event: $event" }
        if (event is Action) {
            val message = GoogleChatMessageConverter.toMessageOut(event, useCondensedFootnotes)
            if (message != null) {
                callback as GoogleChatConnectorCallback
                executor.executeBlocking(Duration.ofMillis(delayInMs)) {
                    try {
                        logger.info {
                            "Sending to Google Chat: space=${callback.spaceName}, thread=${callback.threadName}"
                        }
                        logger.debug {
                            "Message content: ${message.toGoogleMessage()}"
                        }

                        val response = chatService
                            .spaces()
                            .messages()
                            .create(
                                callback.spaceName,
                                message.toGoogleMessage().setThread(Thread().setName(callback.threadName))
                            )
                            .setMessageReplyOption("REPLY_MESSAGE_FALLBACK_TO_NEW_THREAD")
                            .execute()

                        logger.info { "Google Chat API response: ${response?.name}" }
                    } catch (e: Exception) {
                        logger.error(e) {
                            "Failed to send message to Google Chat (space=${callback.spaceName}, thread=${callback.threadName})"
                        }
                    }
                }
            }
        }
    }

    override fun addSuggestions(text: CharSequence, suggestions: List<CharSequence>): BotBus.() -> ConnectorMessage? = {
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
        suggestions: List<CharSequence>
    ): BotBus.() -> ConnectorMessage? = {
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
            else -> message.also { logger.warn { "Add suggestion to message $message not handled" } }
        }
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = {
        when (message) {
            is MediaAction -> listOf(
                card {
                    section {
                        buttons {
                            buttonFromMediaAction(message)
                        }
                    }
                }
            )
            is MediaCard -> listOf(
                card {
                    sectionFromMediaCard(message)
                }
            )
            is MediaCarousel -> listOf(
                card {
                    message.cards.forEach { sectionFromMediaCard(it) }
                }
            )
            else -> emptyList()
        }
    }

    private fun ChatCard.sectionFromMediaCard(message: MediaCard) {
        section {
            if (message.title != null) {
                keyValue(content = message.title!!, bottomLabel = message.subTitle)
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
            if (message.url != null) {
                link(message.url!!)
            } else {
                nlpAction(message.title)
            }
        }
    }
}