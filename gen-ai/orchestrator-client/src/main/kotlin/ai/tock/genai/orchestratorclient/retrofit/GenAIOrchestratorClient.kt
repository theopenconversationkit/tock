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

package ai.tock.genai.orchestratorclient.retrofit

import ai.tock.shared.addJacksonConverter
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.retrofitDefaultLogLevel
import ai.tock.shared.retrofitLogLevel
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 *  Wraps calls to Generative AI Orchestrator Server.
 */
object GenAIOrchestratorClient {
    /** Base URL of the Gen AI Orchestrator server (shared with the streaming client). */
    val serverBaseUrl: String = property("tock_gen_ai_orchestrator_server_url", "http://localhost:8000")

    /** Request timeout in ms (shared with the streaming client). */
    val requestTimeoutMs: Long = longProperty("tock_gen_ai_orchestrator_client_request_timeout_ms", 55000)

    /** Jackson mapper (snake_case) shared with the streaming client so (de)serialization matches. */
    val objectMapper: ObjectMapper =
        jacksonObjectMapper()
            .findAndRegisterModules()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    private var client: OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(GenAIOrchestratorInterceptor(objectMapper))
            .addInterceptor(
                // adapt log level to use specific log interceptors
                HttpLoggingInterceptor().setLevel(
                    HttpLoggingInterceptor.Level.valueOf(
                        retrofitLogLevel(
                            retrofitDefaultLogLevel,
                        ).toString(),
                    ),
                ),
            ).readTimeout(requestTimeoutMs, TimeUnit.MILLISECONDS)
            .connectTimeout(requestTimeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(requestTimeoutMs, TimeUnit.MILLISECONDS)
            .build()

    private val retrofit: Retrofit =
        Retrofit
            .Builder()
            .baseUrl(serverBaseUrl)
            .client(client)
            .addJacksonConverter(objectMapper)
            .build()

    fun getClient() = retrofit
}
