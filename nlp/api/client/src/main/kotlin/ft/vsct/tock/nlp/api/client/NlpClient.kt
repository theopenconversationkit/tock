/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ft.vsct.tock.nlp.api.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.vsct.tock.nlp.entity.ValueResolverRepository
import ft.vsct.tock.nlp.api.client.model.NlpQuery
import ft.vsct.tock.nlp.api.client.model.NlpResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

/**
 *
 */
class NlpClient(baseUrl: String = System.getenv("tock_nlp_service_url") ?: "http://localhost:8888") {

    private val nlpService: NlpService

    init {
        val mapper = jacksonObjectMapper()
        mapper.findAndRegisterModules()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)

        val retrofit = Retrofit.Builder()
                .baseUrl("$baseUrl/rest/nlp/")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .client(
                        OkHttpClient.Builder()
                                .readTimeout(5, TimeUnit.SECONDS)
                                .connectTimeout(5, TimeUnit.SECONDS)
                                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                                .build()
                )
                .build()
        nlpService = retrofit.create<NlpService>(NlpService::class.java)
        //init default value mapping
        ValueResolverRepository.initDefault(mapper)
    }

    fun parse(request: NlpQuery): Response<NlpResult> {
        return nlpService.parse(request).execute()
    }

    fun healthcheck(): Boolean {
        return nlpService.healthcheck().execute().isSuccessful
    }
}