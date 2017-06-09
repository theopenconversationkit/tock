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

package fr.vsct.tock.duckling.client

import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import io.vertx.core.json.JsonArray
import mu.KotlinLogging
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.lang.reflect.Type
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *
 */
internal object DucklingClient {

    data class ParseRequest(val language: String,
                            val dimensions: List<String>,
                            val referenceDate: ZonedDateTime,
                            val referenceTimezone: ZoneId,
                            val textToParse: String)

    interface DucklingService {

        @POST("parse")
        fun parse(@Body request: ParseRequest): Call<JSONValue>

        @GET("healthcheck")
        fun healthcheck(): Call<Void>
    }

    private val logger = KotlinLogging.logger {}
    private val service: DucklingService
    private val jacksonConverterFactory: JacksonConverterFactory

    init {
        jacksonConverterFactory = JacksonConverterFactory.create(mapper)
        val retrofit = retrofitBuilderWithTimeoutAndLogger(longProperty("tock_duckling_request_timeout_ms", 5000), logger)
                .baseUrl("${property("nlp_duckling_url", "http://localhost:8889")}/")
                .addConverterFactory(RawJsonBodyConverterFactory)
                .build()

        service = retrofit.create()
    }

    object JacksonJsonArrayConverter : Converter<ResponseBody, JSONValue> {

        override fun convert(value: ResponseBody): JSONValue {
            return JSONValue(JsonArray(value.string()))
        }
    }

    object RawJsonBodyConverterFactory : Converter.Factory() {

        override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<ResponseBody, *> {
            return JacksonJsonArrayConverter
        }

        override fun requestBodyConverter(type: Type, parameterAnnotations: Array<out Annotation>, methodAnnotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, RequestBody> {
            return jacksonConverterFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
        }

        override fun stringConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, String> {
            return jacksonConverterFactory.stringConverter(type, annotations, retrofit)
        }
    }

    fun parse(
            language: String,
            dimensions: List<String>,
            referenceDate: ZonedDateTime,
            referenceTimezone: ZoneId,
            textToParse: String): JSONValue {
        return service.parse(ParseRequest(language, dimensions, referenceDate, referenceTimezone, textToParse)).execute().body()
    }

    fun healthcheck(): Boolean {
        return service.healthcheck().execute().isSuccessful
    }
}