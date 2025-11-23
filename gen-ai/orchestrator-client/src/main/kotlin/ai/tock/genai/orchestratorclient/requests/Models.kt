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

package ai.tock.genai.orchestratorclient.requests

enum class Formatter(val id: String) {
    F_STRING(id = "f-string"),
    JINJA2(id = "jinja2"),
}

data class PromptTemplate(
    val formatter: String = Formatter.JINJA2.id,
    val template: String,
    val inputs: Map<String, Any> = emptyMap(),
)

data class DialogDetails(
    val dialogId: String? = null,
    val userId: String? = null,
    val history: List<ChatMessage> = emptyList(),
    val tags: List<String> = emptyList(),
)

data class ChatMessage(
    val text: String,
    val type: ChatMessageType,
)

enum class ChatMessageType {
    HUMAN,
    AI,
}
