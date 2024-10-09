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

package ai.tock.genai.orchestratorclient.requests
import ai.tock.genai.orchestratorcore.models.em.EMSetting
import ai.tock.genai.orchestratorcore.models.llm.LLMSetting
import ai.tock.genai.orchestratorcore.models.observability.ObservabilitySetting
import ai.tock.genai.orchestratorcore.models.vectorstore.DocumentSearchParamsBase
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreSetting

data class RAGQuery(
    // val condenseQuestionLlmSetting: LLMSetting,
    // val condenseQuestionPromptInputs: Map<String, String>,
    val history: List<ChatMessage> = emptyList(),
    val questionAnsweringLlmSetting: LLMSetting,
    val questionAnsweringPromptInputs: Map<String, String>,
    val embeddingQuestionEmSetting: EMSetting,
    val documentIndexName: String,
    val documentSearchParams: DocumentSearchParamsBase,
    val vectorStoreSetting: VectorStoreSetting?,
    val observabilitySetting: ObservabilitySetting?
)

data class ChatMessage(
    val text: String,
    val type: ChatMessageType,
)

enum class ChatMessageType{
    HUMAN,
    AI
}
