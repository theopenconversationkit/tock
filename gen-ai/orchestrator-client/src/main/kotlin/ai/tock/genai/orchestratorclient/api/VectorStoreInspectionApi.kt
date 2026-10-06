/*
 * Copyright (C) 2017/2026 SNCF Connect & Tech
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

package ai.tock.genai.orchestratorclient.api

import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionCapabilitiesRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionCondenseRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionDocumentsRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionIndexesRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionSearchRequest
import ai.tock.genai.orchestratorclient.responses.VectorStoreCapabilitiesResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreIndexListResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionCondenseResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionDocumentsResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionSearchResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface VectorStoreInspectionApi {
    @POST("/vector-store-inspection/capabilities")
    fun getCapabilities(
        @Body request: VectorStoreInspectionCapabilitiesRequest,
    ): Call<VectorStoreCapabilitiesResponse>

    @POST("/vector-store-inspection/indexes")
    fun getIndexes(
        @Body request: VectorStoreInspectionIndexesRequest,
    ): Call<VectorStoreIndexListResponse>

    @POST("/vector-store-inspection/documents")
    fun getDocuments(
        @Body request: VectorStoreInspectionDocumentsRequest,
    ): Call<VectorStoreInspectionDocumentsResponse>

    @POST("/vector-store-inspection/condense")
    fun condense(
        @Body request: VectorStoreInspectionCondenseRequest,
    ): Call<VectorStoreInspectionCondenseResponse>

    @POST("/vector-store-inspection/search")
    fun search(
        @Body request: VectorStoreInspectionSearchRequest,
    ): Call<VectorStoreInspectionSearchResponse>
}
