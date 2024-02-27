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

package ai.tock.genai.orchestratorclient.services.impl

import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorClient
import ai.tock.genai.orchestratorclient.api.CompletionApi
import ai.tock.genai.orchestratorclient.requests.SentenceGenerationQuery
import ai.tock.genai.orchestratorclient.responses.SentenceGenerationResponse
import ai.tock.genai.orchestratorclient.services.CompletionService

class CompletionServiceImpl: CompletionService {
    private val retrofit = GenAIOrchestratorClient.getClient()
    private val completionApi = retrofit.create(CompletionApi::class.java)

    override fun generateSentences(query: SentenceGenerationQuery): SentenceGenerationResponse?
        = completionApi.generateSentences(query).execute().body()
}