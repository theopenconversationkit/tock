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

package ai.tock.genai.orchestratorclient.api

import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseDeleteRequest
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseIndexRequest
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseTargetRequest
import ai.tock.genai.orchestratorclient.responses.KnowledgeBaseRowsResponse
import ai.tock.genai.orchestratorclient.responses.KnowledgeBaseWriteResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface KnowledgeBaseApi {
    @POST("knowledge-base/index")
    fun index(
        @Body request: KnowledgeBaseIndexRequest,
    ): Call<KnowledgeBaseWriteResponse>

    @POST("knowledge-base/delete")
    fun delete(
        @Body request: KnowledgeBaseDeleteRequest,
    ): Call<KnowledgeBaseWriteResponse>

    @POST("knowledge-base/rows")
    fun rows(
        @Body request: KnowledgeBaseTargetRequest,
    ): Call<KnowledgeBaseRowsResponse>
}
