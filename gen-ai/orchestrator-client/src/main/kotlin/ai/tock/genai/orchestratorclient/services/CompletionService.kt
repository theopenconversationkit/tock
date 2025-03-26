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

package ai.tock.genai.orchestratorclient.services

import ai.tock.genai.orchestratorclient.requests.CompletionRequest
import ai.tock.genai.orchestratorclient.responses.CompletionResponse
import ai.tock.genai.orchestratorclient.responses.SentenceCompletionResponse

interface CompletionService {
    fun generate(query: CompletionRequest): CompletionResponse?

    fun generateSentences(query: CompletionRequest):  SentenceCompletionResponse?
}