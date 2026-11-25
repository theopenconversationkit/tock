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

package ai.tock.bot.connector.slack

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.slack.model.EventApiMessage
import ai.tock.bot.connector.slack.model.SlackConnectorMessage
import ai.tock.bot.connector.slack.model.SlackMessageOut
import ai.tock.bot.connector.slack.model.UrlVerificationEvent
import ai.tock.bot.connector.slack.model.old.SlackMessageIn
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository.requestTimer
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.monitoring.logError
import ai.tock.shared.Executor
import ai.tock.shared.booleanProperty
import ai.tock.shared.error
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import java.net.URLDecoder
import java.time.Duration

class SlackConnector(
    val applicationId: String,
    val path: String,
    val outToken1: String,
    val outToken2: String,
    val outToken3: String,
    val client: SlackClient,
) : ConnectorBase(SlackConnectorProvider.connectorType) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val OLD_SLACK_API = booleanProperty("tock_slack_old_api_style", false)
    }

    private val executor: Executor by injector.instance()

    override fun register(controller: ConnectorController) {
        controller.registerRoutes(path, routes(controller))
    }

    /**
     * The webhook route, of the Events API or of the former API (`tock_slack_old_api_style`). It answers as the
     * former Vert.x handler did: at once with an empty body, the challenge as text for an URL verification, and
     * the events are processed by the [Executor].
     */
    internal fun routes(controller: ConnectorController): HttpRoutes =
        HttpRoutes {
            post(path) { request ->
                if (OLD_SLACK_API) oldApi(controller, request) else eventApi(controller, request)
            }
        }

    private fun eventApi(
        controller: ConnectorController,
        request: HttpRequest,
    ): HttpResponse {
        val requestTimerData = requestTimer.start("slack_webhook")
        return try {
            val body =
                request.bodyAsString.let {
                    if (it.startsWith("payload=")) {
                        URLDecoder.decode(it.substring("payload=".length), "UTF-8")
                    } else {
                        it
                    }
                }
            logger.info { "message received from slack: $body" }

            val message: EventApiMessage = mapper.readValue(body)
            if (message is UrlVerificationEvent) {
                HttpResponse(headers = mapOf("Content-type" to listOf("text/plain")), body = message.challenge.toByteArray())
            } else {
                // answer to slack immediately
                val event = SlackRequestConverter.toEvent(message, applicationId)
                if (event != null) {
                    executor.executeBlocking {
                        controller.handle(event)
                    }
                } else {
                    logger.debug { "skip message: $body" }
                }
                HttpResponse.status()
            }
        } catch (e: Throwable) {
            logger.logError(e, requestTimerData)
            HttpResponse.status()
        } finally {
            try {
                requestTimer.end(requestTimerData)
            } catch (e: Throwable) {
                logger.error(e)
            }
        }
    }

    private fun oldApi(
        controller: ConnectorController,
        request: HttpRequest,
    ): HttpResponse {
        val requestTimerData = requestTimer.start("slack_webhook")
        try {
            val body = parseJson(request.bodyAsString)
            logger.info { "message received from slack: $body" }

            val message = mapper.readValue<SlackMessageIn>(body, SlackMessageIn::class.java)
            if (message.user_id != "USLACKBOT") {
                executor.executeBlocking {
                    try {
                        val event = SlackRequestConverter.toEvent(message, applicationId)
                        if (event != null) {
                            controller.handle(event)
                        } else {
                            logger.logError("unable to convert $message to event", requestTimerData)
                        }
                    } catch (e: Throwable) {
                        logger.logError(e, requestTimerData)
                    }
                }
            }
        } catch (e: Throwable) {
            logger.logError(e, requestTimerData)
        } finally {
            try {
                requestTimer.end(requestTimerData)
            } catch (e: Throwable) {
                logger.error(e)
            }
        }
        return HttpResponse.status()
    }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        logger.debug { "event: $event" }
        if (event is Action) {
            val message = SlackMessageConverter.toMessageOut(event)
            if (message != null) {
                sendMessage(message, delayInMs)
            }
        }
    }

    private fun sendMessage(
        message: SlackConnectorMessage,
        delayInMs: Long,
    ) {
        executor.executeBlocking(Duration.ofMillis(delayInMs)) {
            client.sendMessage(outToken1, outToken2, outToken3, message)
        }
    }

    override fun addSuggestions(
        text: CharSequence,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            slackMessage(
                text,
                slackAttachment(null, suggestions.map { slackButton(it) }),
            )
        }

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            if (message is SlackMessageOut) {
                val attachment = message.attachments.lastOrNull()
                if (attachment == null) {
                    message.copy(attachments = listOf(slackAttachment(null, suggestions.map { slackButton(it) })))
                } else if (attachment.actions.isEmpty()) {
                    message.copy(
                        attachments =
                            message.attachments.take(message.attachments.size - 1) +
                                slackAttachment(
                                    null,
                                    suggestions.map { slackButton(it) },
                                ),
                    )
                } else {
                    null
                }
            } else {
                null
            }
        }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        {
            if (message is MediaCard) {
                val title = message.title
                val subTitle = message.subTitle
                listOfNotNull(
                    if (title != null && subTitle != null) {
                        textMessage(title)
                    } else {
                        null
                    },
                    slackMessage(
                        subTitle ?: title ?: "",
                        slackAttachment(null, message.actions.filter { it.url == null }.map { slackButton(it.title) }),
                    ),
                )
            } else {
                emptyList()
            }
        }
}
