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
import retrofit2.Response
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.Duration
import java.util.Locale

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
                )
                    .baseUrl("$baseUrl/$p/")
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
    ): Response<ClientMessageResponse> {
        return getService(path).talk(locale.toLanguageTag(), query).execute()
    }
}
