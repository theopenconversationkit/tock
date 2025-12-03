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

package ai.tock.nlp.entity.rest

import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.service.entity.EntityTypeRecognition
import ai.tock.nlp.core.service.entity.EntityTypeValue
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.Locale

/**
 * Rest client used to find entities from a rest API.
 */
class RestEntityTypeClient(targetUrl: String = property("tock_nlp_entity_type_url", "http://localhost:5000/app/v1/")) {
    internal data class EntityTypeRequest(
        val text: String,
        val language: Locale = defaultLocale,
    )

    internal data class EntityTypeResponse(
        val entities: List<EntityResponse>,
    )

    internal data class EntityResponse(val entity: EntityDescription, val probability: Double = 1.0)

    internal data class EntityDescription(
        val start: Int,
        val end: Int,
        val type: EntityTypeDescription,
    )

    internal data class EntityTypeDescription(val name: String)

    private interface EntityTypeApi {
        @Headers("Content-Type:application/json")
        @GET("entities")
        fun supportedEntityTypes(): Call<Set<String>>

        @Headers("Content-Type:application/json")
        @POST("parse")
        fun parse(
            @Body testPlan: EntityTypeRequest,
        ): Call<EntityTypeResponse>

        @GET("healthcheck")
        fun healthcheck(): Call<ResponseBody>
    }

    private val api: EntityTypeApi

    init {
        api =
            retrofitBuilderWithTimeoutAndLogger(timeoutInSeconds)
                .addJacksonConverter()
                .baseUrl(targetUrl)
                .build()
                .create()
    }

    fun retrieveSupportedEntityTypes(): Set<String> =
        try {
            api.supportedEntityTypes().execute().body() ?: emptySet()
        } catch (e: Exception) {
            logger.error(e)
            emptySet()
        }

    fun parse(
        text: String,
        language: Locale,
    ): List<EntityTypeRecognition> =
        try {
            api.parse(EntityTypeRequest(text, language)).execute().body()
                ?.entities?.map {
                    EntityTypeRecognition(
                        EntityTypeValue(
                            it.entity.start,
                            it.entity.end,
                            EntityType(it.entity.type.name),
                        ),
                        it.probability,
                    )
                } ?: emptyList()
        } catch (e: Exception) {
            logger.error(e)
            emptyList()
        }

    fun healthcheck(): Boolean =
        try {
            api.healthcheck().execute().isSuccessful
        } catch (e: Exception) {
            false
        }

    companion object {
        private val timeoutInSeconds = longProperty("tock_bot_rest_timeout_in_ms", 10000L)

        private val logger = KotlinLogging.logger {}
    }
}
