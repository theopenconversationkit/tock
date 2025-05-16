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
package ai.tock.nlp.rasa

import ai.tock.shared.addJacksonConverter
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Streaming

internal class RasaClient(conf: RasaConfiguration) {

    data class TrainModelRequest(
        val domain: String,
        val config: String,
        val nlu: String,
        val save_to_default_model_directory: Boolean = true
    )

    data class PutModelRequest(
        val model_file: String,
        val remote_storage: String? = null
    )

    data class ParseRequest(
        val text: String
    )

    data class ParsedResponse(
        val entities: List<ParsedEntity> = emptyList(),
        val intent: ParsedIntent? = null,
        val intent_ranking: List<ParsedIntent> = emptyList()
    )

    data class ParsedIntent(
        val name: String,
        val confidence: Double
    )

    data class ParsedEntity(
        val start: Int,
        val end: Int,
        val value: String,
        val entity: String,
        val confidence: Double,
        val role: String? = null,
        val extractor: String? = null
    )

    private interface RasaApi {

        @Streaming
        @Headers("Content-Type:application/json")
        @POST("model/train")
        fun train(@Body request: TrainModelRequest): Call<ResponseBody>

        @Headers("Content-Type:application/json")
        @POST("model/parse")
        fun parse(@Body request: ParseRequest): Call<ParsedResponse>

        @Headers("Content-Type:application/json")
        @PUT("model")
        fun setModel(@Body request: PutModelRequest): Call<ResponseBody>

        @GET("")
        fun healthcheck(): Call<ResponseBody>
    }

    private val api: RasaApi

    init {
        api = retrofitBuilderWithTimeoutAndLogger(timeoutInSeconds)
            .addJacksonConverter()
            .baseUrl(conf.rasaUrl)
            .build()
            .create()
    }

    fun train(request: TrainModelRequest): String =
        api.train(request).execute().run {
            if (isSuccessful) {
                headers().get("filename") ?: error("no file name")
            } else {
                error(errorBody()?.string() ?: "unknown error")
            }
        }

    fun setModel(request: PutModelRequest) {
        api.setModel(request).execute().run {
            if (!isSuccessful) {
                error(errorBody()?.string() ?: "unknown error")
            }
        }
    }

    fun parse(request: ParseRequest): ParsedResponse =
        api.parse(request).execute().run { body() ?: error(errorBody()?.string() ?: "unknown error") }

    fun healthcheck(): Boolean =
        try {
            api.healthcheck().execute().isSuccessful
        } catch (e: Exception) {
            false
        }

    companion object {
        private val timeoutInSeconds = longProperty("tock_bot_rest_timeout_in_ms", 100000000L)

        private val logger = KotlinLogging.logger {}
    }
}
