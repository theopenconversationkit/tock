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

package ai.tock.genai.orchestratorclient.requests

import ai.tock.genai.orchestratorcore.models.compressor.DocumentCompressorSetting
import ai.tock.genai.orchestratorcore.models.em.EMSetting
import ai.tock.genai.orchestratorcore.models.llm.LLMSetting
import ai.tock.genai.orchestratorcore.models.vectorstore.DocumentSearchType
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreSetting

data class VectorStoreInspectionCapabilitiesRequest(
    val vectorStoreSetting: VectorStoreSetting?,
)

data class VectorStoreInspectionIndexesRequest(
    val vectorStoreSetting: VectorStoreSetting?,
    val indexNamePrefix: String,
)

data class VectorStoreInspectionDocumentsFilter(
    val text: String? = null,
    val documentId: String? = null,
    val anomaly: String? = null,
)

data class VectorStoreInspectionDocumentsRequest(
    val vectorStoreSetting: VectorStoreSetting?,
    val indexNamePrefix: String,
    val indexName: String,
    val filter: VectorStoreInspectionDocumentsFilter? = null,
    val start: Int,
    val size: Int,
    val includeStats: Boolean = true,
    val includeChunks: Boolean = true,
)

data class VectorStoreInspectionCondenseRequest(
    val questionCondensingLlmSetting: LLMSetting,
    val questionCondensingPrompt: PromptTemplate,
    val question: String,
    val chatHistory: List<ChatMessage> = emptyList(),
)

data class VectorStoreInspectionCompressionOverride(
    val minScore: Double,
    val maxDocuments: Int,
    val fillToMaxDocuments: Boolean,
)

data class VectorStoreInspectionSearchRequest(
    val vectorStoreSetting: VectorStoreSetting?,
    val embeddingQuestionEmSetting: EMSetting,
    val compressorSetting: DocumentCompressorSetting?,
    val indexNamePrefix: String,
    val indexName: String,
    val searchType: DocumentSearchType,
    val query: String,
    val keyWords: List<String> = emptyList(),
    val fetchK: Int,
    val k: Int,
    val compressionEnabled: Boolean,
    /** Internal orchestrator value: before_cut or after_cut. */
    val compressionStage: String,
    val compressionOverride: VectorStoreInspectionCompressionOverride? = null,
    val pinnedChunkIds: List<String> = emptyList(),
    val pinnedRankStrategy: String = "score_only",
)
