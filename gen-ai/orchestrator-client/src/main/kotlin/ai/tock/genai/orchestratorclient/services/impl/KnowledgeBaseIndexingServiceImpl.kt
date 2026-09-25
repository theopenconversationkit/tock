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

package ai.tock.genai.orchestratorclient.services.impl

import ai.tock.genai.orchestratorclient.api.KnowledgeBaseApi
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseDeleteRequest
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseIndexRequest
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseTargetRequest
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorClient
import ai.tock.genai.orchestratorclient.services.KnowledgeBaseIndexingService
import retrofit2.Call

class KnowledgeBaseIndexingServiceImpl : KnowledgeBaseIndexingService {
    private val api = GenAIOrchestratorClient.getClient().create(KnowledgeBaseApi::class.java)

    override fun index(request: KnowledgeBaseIndexRequest) = api.index(request).checkedBody()

    override fun delete(request: KnowledgeBaseDeleteRequest) = api.delete(request).checkedBody()

    override fun rows(request: KnowledgeBaseTargetRequest) = api.rows(request).checkedBody()

    private fun <T> Call<T>.checkedBody(): T {
        val response = execute()
        check(response.isSuccessful) { "Knowledge base orchestrator request failed (HTTP ${response.code()})" }
        return checkNotNull(response.body()) { "Empty knowledge base orchestrator response" }
    }
}
