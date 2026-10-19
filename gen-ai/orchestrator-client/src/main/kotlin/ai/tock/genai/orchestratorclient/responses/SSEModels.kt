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

/**
 * Represents a token event from the RAG streaming API.
 * Emitted when a new fragment of the answer text is generated.
 */
data class TokenEvent(
    val type: String = "token",
    val content: String,
)

/**
 * Represents a complete SSE event received from the server.
 * The data is stored as raw JSON for flexible handling.
 */
sealed class SSEEvent {
    data class Token(
        val event: TokenEvent,
    ) : SSEEvent()

    data class Answer(
        val response: RAGResponse,
    ) : SSEEvent()

    data class Error(
        val errorMessage: String,
    ) : SSEEvent()
}
