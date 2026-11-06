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

package ai.tock.bot.connector.rest.client

import ai.tock.bot.connector.rest.client.model.ClientMessageRequest
import ai.tock.bot.connector.rest.client.model.ClientMessageResponse
import ai.tock.shared.Level
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.BufferedSource
import retrofit2.Response
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.Duration
import java.util.Locale

/**
 * Simple representation of a Server-Sent Event
 */
data class ServerSentEvent(
    val event: String? = null,
    val data: String? = null,
    val id: String? = null,
    val retry: Long? = null,
)

/**
 *
 */
class ConnectorRestClient(
    private val baseUrl: String = System.getenv("tock_bot_rest_url") ?: "http://localhost:8888",
) {
    private val restCache: Cache<String, ConnectorRestService> =
        CacheBuilder.newBuilder().expireAfterAccess(Duration.ofHours(1)).build()

    private fun getService(path: String): ConnectorRestService {
        val p = if (path.startsWith("/")) path.substring(1) else path
        return restCache.get(p) {
            val mapper = jacksonObjectMapper()
            mapper.findAndRegisterModules()
            // force java time module
            mapper.registerModule(JavaTimeModule())
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)

            val timeout = longProperty("tock_bot_rest_client_request_timeout_ms", 100000)
            val retrofit =
                retrofitBuilderWithTimeoutAndLogger(
                    ms = timeout,
                    level = Level.BODY,
                ).baseUrl("$baseUrl/$p/")
                    .addConverterFactory(JacksonConverterFactory.create(mapper))
                    .build()
            retrofit.create(ConnectorRestService::class.java)
        }
    }

    /**
     * Analyse a sentence and returns the result.
     */
    fun talk(
        path: String,
        locale: Locale,
        query: ClientMessageRequest,
    ): Response<ClientMessageResponse> = getService(path).talk(locale.toLanguageTag(), query).execute()

    /**
     * Stream the bot response as Server-Sent Events.
     */
    fun talkStream(
        path: String,
        locale: Locale,
        query: ClientMessageRequest,
    ): Flow<ServerSentEvent> =
        flow {
            val response = getService(path).talkStream(locale.toLanguageTag(), query).execute()
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to stream response: ${response.code()} ${response.message()}")
            }
            val body = response.body() ?: throw IllegalStateException("Empty response body")
            body.source().use { source ->
                // Spec-compliant SSE parsing (https://html.spec.whatwg.org/multipage/server-sent-events.html):
                // accumulate fields until a blank line dispatches the event. Tolerates comments
                // (':' lines, e.g. keep-alive pings), multi-line 'data:', and unknown fields.
                var eventType: String? = null
                var id: String? = null
                val data = StringBuilder()

                suspend fun dispatch() {
                    // A blank line with no field set is just a keep-alive separator: skip it.
                    if (eventType == null && id == null && data.isEmpty()) return
                    emit(
                        ServerSentEvent(
                            event = eventType ?: "message",
                            data = data.toString().removeSuffix("\n"),
                            id = id,
                        ),
                    )
                    eventType = null
                    id = null
                    data.setLength(0)
                }

                while (!source.exhausted()) {
                    val line = source.readUtf8LineStrict()
                    when {
                        // Blank line: dispatch the buffered event.
                        line.isEmpty() -> {
                            dispatch()
                        }

                        // Comment line: ignore.
                        line.startsWith(":") -> {}

                        else -> {
                            val colon = line.indexOf(':')
                            val field = if (colon >= 0) line.substring(0, colon) else line
                            // A single leading space after the colon is part of the delimiter.
                            var value = if (colon >= 0) line.substring(colon + 1) else ""
                            if (value.startsWith(" ")) value = value.substring(1)
                            when (field) {
                                "event" -> {
                                    eventType = value
                                }

                                "data" -> {
                                    data.append(value).append('\n')
                                }

                                "id" -> {
                                    id = value
                                }

                                // "retry" and unknown fields are ignored.
                                else -> {}
                            }
                        }
                    }
                }
                // Flush a trailing event not terminated by a blank line.
                dispatch()
            }
        }
}
