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

package ai.tock.bot.llm.rag.core.client


import ai.tock.bot.llm.rag.core.client.models.RagQuery
import ai.tock.bot.llm.rag.core.client.models.RagResult
import ai.tock.shared.*
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 *  Wraps calls to the Rag (retrieval augmented generation) stack.
 */
class RagServiceApiClient(baseUrl: String = property("tock_rag_client_service_url", "http://localhost:8000")) :
        RagClient {

    private val logger = KotlinLogging.logger {}
    private val ragService: RagService

    init {
        val timeout = longProperty("tock_rag_client_request_timeout_ms", 25000)
        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addJacksonConverter(initMapper())
                .client(
                        OkHttpClient.Builder()
                                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
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
                                .build()
                )
                .build()

        ragService = retrofit.create(RagService::class.java)
    }

    private fun initMapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        mapper.findAndRegisterModules()
        // force java time module
        mapper.registerModule(JavaTimeModule())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
        return mapper
    }

    private fun <T> Response<T>.parseAndReturns(): T? =
            body()
                    ?: run {
                        logger.error { "rag client ERROR !!! : ${errorBody()?.string()}" }
                        null as Nothing?
                    }

    override fun ask(query: RagQuery): RagResult? {
        return ragService.ask(query).execute().parseAndReturns()
    }

    override fun healthcheck(): Call<Void> {
        TODO("Not yet implemented")
    }

}
