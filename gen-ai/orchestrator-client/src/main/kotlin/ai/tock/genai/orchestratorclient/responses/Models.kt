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

package ai.tock.genai.orchestratorclient.responses

data class ChunkSentences(
    val chunk: String? = null,
    val sentences: List<String>? = emptyList(),
    val usedInResponse: Boolean? = false,
    val reason: String? = null,
)

data class LLMAnswer(
    val status: String?,
    val answer: String?,
    val confidenceScore: Double? = 0.0,
    val displayAnswer: Boolean? = true,
    val topic: String? = null,
    val suggestedTopics: List<String>? = null,
    val understanding: String? = null,
    val redirectionIntent: String? = null,
    val contextUsage: List<ChunkSentences>? = null,
)

data class Footnote(
    val identifier: String,
    val title: String,
    val url: String? = null,
    val content: String? = null,
    val score: Float? = null,
)

data class ObservabilityInfo(
    val traceId: String,
    val traceName: String,
    val traceUrl: String,
)

class ErrorResponse(
    val code: String,
    val message: String,
    val detail: String?,
    val info: ErrorInfo,
)

class ErrorInfo(
    val provider: String,
    val error: String,
    val cause: String,
    val request: String,
)
