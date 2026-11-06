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

package ai.tock.genai.orchestratorclient.services.impl

import ai.tock.genai.orchestratorclient.api.CompletionApi
import ai.tock.genai.orchestratorclient.requests.CompletionRequest
import ai.tock.genai.orchestratorclient.responses.CompletionResponse
import ai.tock.genai.orchestratorclient.responses.SentenceCompletionResponse
import ai.tock.genai.orchestratorclient.responses.TokenEvent
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorClient
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorParsingError
import ai.tock.genai.orchestratorclient.services.CompletionService
import ai.tock.genai.orchestratorclient.services.CompletionStreamListener
import com.fasterxml.jackson.module.kotlin.readValue
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.ErrorStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler
import com.launchdarkly.eventsource.background.BackgroundEventSource
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

class CompletionServiceImpl : CompletionService {
    private val retrofit = GenAIOrchestratorClient.getClient()
    private val completionApi = retrofit.create(CompletionApi::class.java)

    // Reuse the shared client configuration (base URL, timeout, snake_case mapper) so the
    // streaming client stays consistent with the Retrofit client and nothing is duplicated.
    private val baseUrl: String = GenAIOrchestratorClient.serverBaseUrl
    private val timeout: Long = GenAIOrchestratorClient.requestTimeoutMs
    private val jsonObjectMapper = GenAIOrchestratorClient.objectMapper

    override fun generate(query: CompletionRequest): CompletionResponse? = completionApi.generate(query).execute().body()

    override fun generateSentences(query: CompletionRequest): SentenceCompletionResponse? = completionApi.generateSentences(query).execute().body()

    override fun generateStream(
        query: CompletionRequest,
        listener: CompletionStreamListener,
    ) {
        val latch = CountDownLatch(1)
        // Set once the terminal 'answer' event has been received. After that, the server
        // closing the stream is expected (StreamClosedByServerException), so we must not
        // surface it as an error.
        val completed = AtomicBoolean(false)
        var eventSource: BackgroundEventSource? = null
        try {
            val url = URL("$baseUrl/completion/stream")
            val body =
                jsonObjectMapper
                    .writeValueAsString(query)
                    .toRequestBody("application/json".toMediaType())

            eventSource =
                BackgroundEventSource
                    .Builder(
                        object : BackgroundEventHandler {
                            override fun onOpen() {
                                logger.debug("Completion stream connection opened")
                            }

                            override fun onClosed() {
                                logger.debug("Completion stream connection closed")
                                // Terminal: release the caller. Do not let the source reconnect.
                                latch.countDown()
                            }

                            override fun onMessage(
                                event: String,
                                messageEvent: MessageEvent,
                            ) {
                                try {
                                    when (event) {
                                        "token" -> {
                                            val tokenEvent: TokenEvent =
                                                jsonObjectMapper.readValue(messageEvent.data)
                                            listener.onToken(tokenEvent)
                                        }

                                        "answer" -> {
                                            val response: CompletionResponse =
                                                jsonObjectMapper.readValue(messageEvent.data)
                                            listener.onAnswer(response)
                                            // 'answer' is the terminal event: stop here so the
                                            // EventSource does not auto-reconnect and re-run it.
                                            completed.set(true)
                                            latch.countDown()
                                        }

                                        "error" -> {
                                            val node = jsonObjectMapper.readTree(messageEvent.data)
                                            val message = node.get("message")?.asText() ?: "Completion streaming error"
                                            val detail = node.get("detail")?.asText() ?: messageEvent.data
                                            logger.error { "Completion stream error event: $message - $detail" }
                                            completed.set(true)
                                            listener.onError(
                                                GenAIOrchestratorParsingError(message = message, detail = detail),
                                            )
                                            latch.countDown()
                                        }

                                        else -> {
                                            logger.warn { "Unknown SSE event type: $event" }
                                        }
                                    }
                                } catch (e: Exception) {
                                    logger.error { "Error parsing SSE message for event: $event - ${e.message}" }
                                    listener.onError(
                                        GenAIOrchestratorParsingError(
                                            detail = e.message ?: "Unknown parsing error",
                                        ),
                                    )
                                    latch.countDown()
                                }
                            }

                            override fun onComment(comment: String) {
                                logger.debug { "Completion stream comment: $comment" }
                            }

                            override fun onError(t: Throwable) {
                                // After the terminal 'answer' event, the server closing the
                                // stream is expected. Ignore it instead of surfacing an error.
                                if (completed.get()) {
                                    logger.debug { "Completion stream closed after answer (expected): ${t.message}" }
                                    latch.countDown()
                                    return
                                }
                                logger.error { "Completion stream error: ${t.message}" }
                                listener.onError(t)
                                latch.countDown()
                            }
                        },
                        EventSource
                            .Builder(
                                ConnectStrategy
                                    .http(url)
                                    .methodAndBody("POST", body)
                                    .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                                    .readTimeout(timeout, TimeUnit.MILLISECONDS),
                            )
                            // Fail fast instead of retrying forever on HTTP errors (e.g. 422)
                            .errorStrategy(ErrorStrategy.alwaysThrow()),
                    ).threadPriority(Thread.MAX_PRIORITY)
                    .build()

            eventSource.start()
            latch.await()
            listener.onComplete()
        } catch (e: Exception) {
            logger.error { "Completion stream error: ${e.message}" }
            listener.onError(e)
        } finally {
            // Critical: stop the background thread so it cannot reconnect and re-trigger it.
            eventSource?.close()
        }
    }
}
