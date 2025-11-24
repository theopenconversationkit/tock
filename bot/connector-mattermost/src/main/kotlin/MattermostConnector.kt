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

package ai.tock.bot.connector.mattermost

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.mattermost.model.MattermostConnectorMessage
import ai.tock.bot.connector.mattermost.model.MattermostMessageIn
import ai.tock.bot.connector.mattermost.model.MattermostMessageOut
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.monitoring.logError
import ai.tock.shared.Executor
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import mu.KotlinLogging
import java.time.Duration

class MattermostConnector(
    private val applicationId: String,
    private val path: String,
    private val url: String,
    private val token: String,
    private val channelId: String? = null,
    private val outgoingToken: String,
    private val tockUsername: String? = null,
) : ConnectorBase(mattermostConnectorType) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val client: MattermostClient = MattermostClient(url, token)
    private val executor: Executor by injector.instance()

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            logger.info { "Register mattermost service $path" }
            router.post(path).handler { context ->
                logger.debug { "Handle mattermost request ${context.body().asString()}" }
                // see https://developers.mattermost.com/integrate/webhooks/outgoing/
                val requestTimerData = BotRepository.requestTimer.start("mattermost_webhook")
                try {
                    val body =
                        when (context.request().getHeader("Content-Type")) {
                            "application/x-www-form-urlencoded" -> {
                                val metadata: JsonObject = JsonObject()
                                for ((key, value) in context.request().formAttributes().entries()) {
                                    metadata.put(key, value)
                                }
                                Json.encode(metadata)
                            }
                            // else consider application/json
                            else -> context.body().asString()
                        }

                    val message: MattermostMessageIn = mapper.readValue(body)

                    if (message.token != outgoingToken) {
                        logger.error { "Failed to validate token : ${message.token}" }
                        context.fail(400)
                    }

                    try {
                        val event = MattermostRequestConverter.toEvent(message, applicationId)
                        controller.handle(event)
                    } catch (e: Throwable) {
                        logger.logError(e, requestTimerData)
                    }
                } catch (e: Exception) {
                    logger.logError(e, requestTimerData)
                } finally {
                    try {
                        BotRepository.requestTimer.end(requestTimerData)
                        context.response().end()
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                }
            }
        }
    }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        logger.debug { "event: $event" }
        if (event is Action) {
            val message = MattermostMessageConverter.toMessageOut(event, channelId, tockUsername)
            if (message != null) {
                sendMessage(message, delayInMs)
            }
        }
    }

    private fun sendMessage(
        message: MattermostConnectorMessage,
        delayInMs: Long,
    ) {
        executor.executeBlocking(Duration.ofMillis(delayInMs)) {
            client.sendMessage(message as MattermostMessageOut)
        }
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        {
            val messages = mutableListOf<ConnectorMessage>()
            if (message is MediaCard) {
                val title = message.title
                val subTitle = message.subTitle
                if (message.actions.isEmpty()) {
                    if (title != null && subTitle != null) {
                        messages.add(textMessage(title, channelId, tockUsername))
                    }
                } else {
                    messages.add(
                        textMessageLinks(
                            subTitle ?: title ?: "",
                            channelId,
                            tockUsername,
                            message.actions.filterNot { it.url == null }.map { mattermostLink(it.title, it.url ?: "") },
                        ),
                    )
                }
            }
            messages
        }
}
