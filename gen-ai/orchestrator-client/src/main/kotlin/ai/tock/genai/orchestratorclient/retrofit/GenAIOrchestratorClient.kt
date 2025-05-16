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


import ai.tock.shared.*
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.text.DateFormat
import java.util.concurrent.TimeUnit


/**
 *  Wraps calls to Generative AI Orchestrator Server.
 */
object GenAIOrchestratorClient {
    private val baseUrl: String = property("tock_gen_ai_orchestrator_server_url", "http://localhost:8000")
    private val timeout: Long = longProperty("tock_gen_ai_orchestrator_client_request_timeout_ms", 55000)

    private val jsonObjectMapper: ObjectMapper =
        jacksonObjectMapper().findAndRegisterModules()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    private var client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(GenAIOrchestratorInterceptor(jsonObjectMapper))
        .addInterceptor(
            // adapt log level to use specific log interceptors
            HttpLoggingInterceptor().setLevel(
                HttpLoggingInterceptor.Level.valueOf(
                    retrofitLogLevel(
                        retrofitDefaultLogLevel
                    ).toString()
                )
            )
        )
        .readTimeout(timeout, TimeUnit.MILLISECONDS)
        .connectTimeout(timeout, TimeUnit.MILLISECONDS)
        .writeTimeout(timeout, TimeUnit.MILLISECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addJacksonConverter(jsonObjectMapper)
        .build()

    fun getClient() = retrofit
}
