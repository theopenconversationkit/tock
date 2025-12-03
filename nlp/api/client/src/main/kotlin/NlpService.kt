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

package ai.tock.nlp.api.client

import ai.tock.nlp.api.client.model.NlpLogCount
import ai.tock.nlp.api.client.model.NlpLogCountQuery
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.nlp.api.client.model.dump.ApplicationDefinition
import ai.tock.nlp.api.client.model.dump.ApplicationDump
import ai.tock.nlp.api.client.model.dump.CreateApplicationQuery
import ai.tock.nlp.api.client.model.dump.IntentDefinition
import ai.tock.nlp.api.client.model.dump.SentencesDump
import ai.tock.nlp.api.client.model.evaluation.EntityEvaluationQuery
import ai.tock.nlp.api.client.model.evaluation.EntityEvaluationResult
import ai.tock.nlp.api.client.model.merge.ValuesMergeQuery
import ai.tock.nlp.api.client.model.merge.ValuesMergeResult
import ai.tock.nlp.api.client.model.monitoring.MarkAsUnknownQuery
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

internal data class BooleanResponse(val success: Boolean = true)

/**
 *
 */
internal interface NlpService {
    @POST("parse")
    fun parse(
        @Body query: NlpQuery,
    ): Call<NlpResult>

    @POST("evaluate")
    fun evaluateEntities(
        @Body query: EntityEvaluationQuery,
    ): Call<EntityEvaluationResult>

    @POST("merge")
    fun mergeValues(
        @Body query: ValuesMergeQuery,
    ): Call<ValuesMergeResult>

    @POST("unknown")
    fun markAsUnknown(
        @Body query: MarkAsUnknownQuery,
    ): Call<ResponseBody>

    @GET("intents")
    fun getIntentsByNamespaceAndName(
        @Query("namespace") namespace: String,
        @Query("name") name: String,
    ): Call<List<IntentDefinition>>

    @GET("application")
    fun getApplicationByNamespaceAndName(
        @Query("namespace") namespace: String,
        @Query("name") name: String,
    ): Call<ApplicationDefinition>

    @POST("application/create")
    fun createApplication(
        @Body query: CreateApplicationQuery,
    ): Call<ApplicationDefinition?>

    @Multipart
    @POST("dump/import")
    fun importNlpDump(
        @Part dump: MultipartBody.Part,
    ): Call<BooleanResponse>

    @POST("dump/import/plain")
    fun importNlpPlainDump(
        @Body dump: ApplicationDump,
    ): Call<BooleanResponse>

    @Multipart
    @POST("dump/import/sentences")
    fun importNlpSentencesDump(
        @Part dump: MultipartBody.Part,
    ): Call<BooleanResponse>

    @POST("dump/import/sentences/plain")
    fun importNlpPlainSentencesDump(
        @Body dump: SentencesDump,
    ): Call<BooleanResponse>

    @POST("logs/count")
    fun logsCount(
        @Body query: NlpLogCountQuery,
    ): Call<List<NlpLogCount>>

    @GET("healthcheck")
    fun healthcheck(): Call<ResponseBody>
}
