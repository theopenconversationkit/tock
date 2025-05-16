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

package ai.tock.bot.orchestration.orchestrator

import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.orchestration.shared.AskEligibilityToOrchestratedBotRequest
import ai.tock.bot.orchestration.shared.NoOrchestrationStatus
import ai.tock.bot.orchestration.shared.OrchestrationMetaData
import ai.tock.bot.orchestration.shared.OrchestrationTargetedBot
import ai.tock.bot.orchestration.shared.ResumeOrchestrationRequest
import ai.tock.bot.orchestration.shared.SecondaryBotNoResponse
import ai.tock.bot.orchestration.shared.SecondaryBotResponse
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

class RestJacksonOrchestratedRuntimeBot(
    target: OrchestrationTargetedBot,
    urlBot: String,
    timeoutMs: Long,
    serialisationModules: List<Module>
) : OrchestratedRuntimeBot(target) {

    private val targetBotClient = BotRestClient.create(urlBot, timeoutMs, serialisationModules)

    override fun askOrchestration(request: AskEligibilityToOrchestratedBotRequest): SecondaryBotResponse {
        return targetBotClient.askOrchestration(request).execute().body() ?: SecondaryBotNoResponse(
            status = NoOrchestrationStatus.NOT_AVAILABLE,
            metaData = request.metadata ?: OrchestrationMetaData(PlayerId("unknown"), target.botId, PlayerId("orchestrator"))
        )
    }

    override fun resumeOrchestration(request: ResumeOrchestrationRequest): SecondaryBotResponse {
        return targetBotClient.resumeOrchestration(request).execute().body() ?: SecondaryBotNoResponse(
            status = NoOrchestrationStatus.END,
            metaData = request.metadata
        )
    }
}

interface BotRestClient {

    @POST("orchestration/eligibility")
    fun askOrchestration(@Body request: AskEligibilityToOrchestratedBotRequest): Call<SecondaryBotResponse>

    @POST("orchestration/proxy")
    fun resumeOrchestration(@Body request: ResumeOrchestrationRequest): Call<SecondaryBotResponse>

    companion object {

        fun create(url: String, timeout: Long = 30000L, serialisationModules: List<Module> = emptyList()): BotRestClient =
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(Level.BODY))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .callTimeout(timeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build()
                .let {
                    Retrofit.Builder().client(it)
                }
                .baseUrl(url)
                .addConverterFactory(
                    JacksonConverterFactory.create(
                        jacksonObjectMapper()
                            .findAndRegisterModules()
                            .registerModules(serialisationModules + JavaTimeModule())
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
                            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
                            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    )
                )
                .build()
                .create(BotRestClient::class.java)
    }
}
