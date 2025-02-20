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

package ai.tock.bot.api.service

import ai.tock.bot.api.model.websocket.RequestData
import ai.tock.bot.api.model.websocket.ResponseData
import ai.tock.bot.connector.web.WebConnectorResponseContent
import ai.tock.bot.engine.event.MetadataEvent
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.create
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import com.fasterxml.jackson.module.kotlin.readValue
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler
import com.launchdarkly.eventsource.background.BackgroundEventSource
import mu.KotlinLogging
import java.net.URI
import java.util.concurrent.TimeUnit

internal class BotApiClient(baseUrl: String) {

    private val connectionTimeoutInMs = longProperty("tock_bot_api_connection_timeout_in_ms", 3000L)
    private val timeoutInMs = longProperty("tock_bot_api_timeout_in_ms", 60000L)
    private val logger = KotlinLogging.logger {}
    private val formattedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

    private val service: BotApiService

    init {
        service = retrofitBuilderWithTimeoutAndLogger(timeoutInMs, logger)
            .addJacksonConverter()
            .baseUrl(formattedBaseUrl)
            .build()
            .create()
    }

    fun send(request: RequestData): ResponseData? =
        try {
            service.send(request).execute().body()
        } catch (e: Exception) {
            logger.error(e)
            null
        }

    fun sendWithSse(request: RequestData, sendResponse: (ResponseData?) -> Unit): Unit =
        try {

            val closeListener = CloseListener()
            val eventSource = BackgroundEventSource
                .Builder(
                    object : BackgroundEventHandler {

                        override fun onOpen() {
                            logger.debug("open sse connection")
                        }

                        override fun onClosed() {
                            logger.debug("close sse connection")
                        }

                        override fun onMessage(event: String, messageEvent: MessageEvent) {
                            logger.debug { "Event: $event" }
                            logger.debug { "Message: ${messageEvent.data}" }
                            if (event == "message") {
                                val message: ResponseData = mapper.readValue(messageEvent.data)
                                sendResponse(message)
                                if (message.botResponse?.context?.lastResponse == true) {
                                    logger.debug { "Last sse answer" }
                                    closeListener.close()
                                }
                            }
                        }

                        override fun onComment(comment: String) {
                            logger.debug { "sse comment: $comment" }
                        }

                        override fun onError(t: Throwable) {
                            logger.error(t)
                        }
                    },
                    EventSource.Builder(
                        ConnectStrategy
                            .http(URI.create("${formattedBaseUrl}webhook/sse").toURL())
                            // Specifying custom request headers
                            .header(
                                "message",
                                mapper.writeValueAsString(request)
                            )
                            .connectTimeout(connectionTimeoutInMs, TimeUnit.MILLISECONDS)
                            .readTimeout(timeoutInMs, TimeUnit.MILLISECONDS)
                    )
                )
                .threadPriority(Thread.MAX_PRIORITY)
                .build()
                .apply {
                    closeListener.source = this
                    start()
                }
        } catch (e: Exception) {
            logger.error(e)
        }
}

private class CloseListener(var source: BackgroundEventSource? = null) {
    fun close() {
        source?.close()
    }
}
