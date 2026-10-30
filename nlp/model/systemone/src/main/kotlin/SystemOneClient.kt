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
package ai.tock.nlp.systemone

import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import mu.KotlinLogging
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * REST client for a System One API-compatible decision-model endpoint.
 * Supports Cloudflare's wrapped response and direct System One API responses.
 */
internal class SystemOneClient(
    private val conf: SystemOneConfiguration,
) {
    /**
     * A single "choice" question of the Clef `questions` schema: given [criteria]
     * (option id -> human readable description), Clef returns a probability per option.
     */
    @Serializable
    data class ChoiceQuestion(
        @EncodeDefault(EncodeDefault.Mode.ALWAYS)
        val type: String = "choice",
        val instructions: String? = null,
        val criteria: Map<String, String?>,
    )

    @Serializable
    data class RunRequest(
        val model: String,
        val state: String,
        val questions: Map<String, ChoiceQuestion>,
    )

    @Serializable
    data class ChoiceAnswer(
        val type: String = "choice",
        val choice: String? = null,
        val probabilities: Map<String, Double> = emptyMap(),
        val confidence: Double = 0.0,
    )

    @Serializable
    data class CloudflareApiError(
        val code: Int? = null,
        val message: String? = null,
    )

    @Serializable(with = RunResultSerializer::class)
    sealed class RunResult {
        abstract val model: String?
        abstract val answers: Map<String, ChoiceAnswer>

        @Serializable
        data class Cloudflare(
            val result: ModelResult? = null,
            val success: Boolean = false,
            val errors: List<CloudflareApiError> = emptyList(),
        ) : RunResult() {
            override val model: String? get() = result?.model
            override val answers: Map<String, ChoiceAnswer> get() = result?.answers.orEmpty()
        }

        @Serializable
        data class SystemOneApi(
            override val model: String? = null,
            override val answers: Map<String, ChoiceAnswer> = emptyMap(),
            val usage: JsonElement? = null,
            @SerialName("latency_ms")
            val latencyMs: Double? = null,
        ) : RunResult()
    }

    @Serializable
    data class ModelResult(
        val model: String? = null,
        val answers: Map<String, ChoiceAnswer> = emptyMap(),
    )

    internal object RunResultSerializer : JsonContentPolymorphicSerializer<RunResult>(RunResult::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RunResult> =
            when {
                "result" in element.jsonObject -> RunResult.Cloudflare.serializer()
                "answers" in element.jsonObject -> RunResult.SystemOneApi.serializer()
                else -> throw SerializationException("Unsupported System One API response format")
            }
    }

    private interface SystemOneApi {
        @Headers("Content-Type: application/json")
        @POST
        fun run(
            @Url endpointUrl: HttpUrl,
            @Header("Authorization") authorization: String,
            @Body request: RunRequest,
        ): Call<RunResult>
    }

    private val endpointUrl: HttpUrl by lazy {
        conf.endpointUrl.toHttpUrlOrNull()
            ?: error("System One endpointUrl must be a valid absolute HTTP(S) URL")
    }

    private val api: SystemOneApi by lazy {
        val baseUrl =
            endpointUrl
                .newBuilder()
                .encodedPath("/")
                .query(null)
                .fragment(null)
                .build()
        retrofitBuilderWithTimeoutAndLogger(conf.timeoutInMs, logger)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(baseUrl)
            .build()
            .create()
    }

    fun run(request: RunRequest): RunResult =
        api
            .run(
                endpointUrl,
                "Bearer ${conf.apiToken}",
                request,
            ).execute()
            .run {
                if (!isSuccessful) {
                    error(errorBody()?.string() ?: "unknown error")
                }
                when (val response = body() ?: error("Empty System One API response")) {
                    is RunResult.Cloudflare -> {
                        if (response.success && response.result != null) {
                            response
                        } else {
                            error(
                                response.errors
                                    .takeIf { it.isNotEmpty() }
                                    ?.joinToString { it.message ?: "unknown error" }
                                    ?: "Cloudflare response was unsuccessful or missing its result",
                            )
                        }
                    }

                    is RunResult.SystemOneApi -> {
                        response
                    }
                }
            }

    /**
     * Lightweight call used to check that the configured endpoint/token/model is reachable.
     * A Clef "choice" question requires between 2 and 255 criteria, so at least two dummy
     * options are sent here even though only the call's success/failure is relevant.
     */
    fun healthcheck(): Boolean =
        try {
            run(
                RunRequest(
                    model = conf.model,
                    state = "healthcheck",
                    questions =
                        mapOf(
                            "healthcheck" to
                                ChoiceQuestion(
                                    instructions = "healthcheck",
                                    criteria = mapOf("ok" to "ok", "not_ok" to "not ok"),
                                ),
                        ),
                ),
            )
            true
        } catch (e: Exception) {
            logger.warn(e) { "System One healthcheck failed" }
            false
        }

    companion object {
        private val logger = KotlinLogging.logger {}
        private val json = Json { ignoreUnknownKeys = true }
    }
}
