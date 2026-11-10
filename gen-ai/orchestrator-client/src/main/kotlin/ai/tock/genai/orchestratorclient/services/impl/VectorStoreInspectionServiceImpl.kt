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

package ai.tock.genai.orchestratorclient.services.impl

import ai.tock.genai.orchestratorclient.api.VectorStoreInspectionApi
import ai.tock.genai.orchestratorclient.requests.VectorStoreChunkTargetRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreDeleteChunkRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreDeleteIndexRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreIndexTargetRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionCapabilitiesRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionCondenseRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionDocumentsRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionIndexesRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionSearchRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreSetEmbeddingModelRequest
import ai.tock.genai.orchestratorclient.responses.VectorStoreCapabilitiesResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreChunkDescriptionResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreIndexListResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreIndexMetadataResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionCondenseResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionDocumentsResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionSearchResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreWriteResponse
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorClient
import ai.tock.genai.orchestratorclient.services.VectorStoreInspectionService

class VectorStoreInspectionServiceImpl : VectorStoreInspectionService {
    private val api =
        GenAIOrchestratorClient.getClient().create(VectorStoreInspectionApi::class.java)

    override fun getCapabilities(request: VectorStoreInspectionCapabilitiesRequest): VectorStoreCapabilitiesResponse? = api.getCapabilities(request).execute().body()

    override fun getIndexes(request: VectorStoreInspectionIndexesRequest): VectorStoreIndexListResponse? = api.getIndexes(request).execute().body()

    override fun getDocuments(request: VectorStoreInspectionDocumentsRequest): VectorStoreInspectionDocumentsResponse? = api.getDocuments(request).execute().body()

    override fun condense(request: VectorStoreInspectionCondenseRequest): VectorStoreInspectionCondenseResponse? = api.condense(request).execute().body()

    override fun search(request: VectorStoreInspectionSearchRequest): VectorStoreInspectionSearchResponse? = api.search(request).execute().body()

    override fun getIndexMetadata(request: VectorStoreIndexTargetRequest): VectorStoreIndexMetadataResponse = api.getIndexMetadata(request).checkedBody()

    override fun setEmbeddingModel(request: VectorStoreSetEmbeddingModelRequest): VectorStoreIndexMetadataResponse = api.setEmbeddingModel(request).checkedBody()

    override fun getChunk(request: VectorStoreChunkTargetRequest): VectorStoreChunkDescriptionResponse = api.getChunk(request).checkedBody()

    override fun deleteChunk(request: VectorStoreDeleteChunkRequest): VectorStoreWriteResponse = api.deleteChunk(request).checkedBody()

    override fun deleteIndex(request: VectorStoreDeleteIndexRequest): VectorStoreWriteResponse = api.deleteIndex(request).checkedBody()

    private fun <T> retrofit2.Call<T>.checkedBody(): T {
        val response = execute()
        check(response.isSuccessful) { "Vector store management request failed" }
        return checkNotNull(response.body()) { "Empty vector store management response" }
    }
}
