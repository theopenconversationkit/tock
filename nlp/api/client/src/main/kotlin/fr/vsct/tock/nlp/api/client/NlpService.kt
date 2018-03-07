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

package fr.vsct.tock.nlp.api.client

import fr.vsct.tock.nlp.api.client.model.NlpIntentEntitiesQuery
import fr.vsct.tock.nlp.api.client.model.NlpQuery
import fr.vsct.tock.nlp.api.client.model.NlpResult
import fr.vsct.tock.nlp.api.client.model.dump.ApplicationDump
import fr.vsct.tock.nlp.api.client.model.dump.IntentDefinition
import fr.vsct.tock.nlp.api.client.model.dump.SentencesDump
import fr.vsct.tock.nlp.api.client.model.evaluation.EntityEvaluationQuery
import fr.vsct.tock.nlp.api.client.model.evaluation.EntityEvaluationResult
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeResult
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

/**
 *
 */
internal interface NlpService {

    @POST("parse")
    fun parse(@Body query: NlpQuery): Call<NlpResult>

    @POST("parse/intent/entities")
    fun parseIntentEntities(@Body query: NlpIntentEntitiesQuery): Call<NlpResult>

    @POST("evaluate")
    fun evaluateEntities(@Body query: EntityEvaluationQuery): Call<EntityEvaluationResult>

    @POST("merge")
    fun mergeValues(@Body query: ValuesMergeQuery): Call<ValuesMergeResult>

    @GET("intents")
    fun getIntentsByNamespaceAndName(@Query("namespace") namespace: String, @Query("name") name: String): Call<List<IntentDefinition>>

    @Multipart
    @POST("dump/import")
    fun importNlpDump(@Part dump: MultipartBody.Part): Call<Boolean>

    @POST("dump/import/plain")
    fun importNlpPlainDump(@Body dump: ApplicationDump): Call<Boolean>

    @Multipart
    @POST("dump/import/sentences")
    fun importNlpSentencesDump(@Part dump: MultipartBody.Part): Call<Boolean>

    @POST("dump/import/sentences/plain")
    fun importNlpPlainSentencesDump(@Body dump: SentencesDump): Call<Boolean>

    @GET("healthcheck")
    fun healthcheck(): Call<Void>

}