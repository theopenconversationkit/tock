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

package ai.tock.bot.admin.model.genai

import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionSearchFunnel
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionSearchResultChunk
import ai.tock.genai.orchestratorcore.models.vectorstore.DocumentSearchType

data class VectorStoreIndexDTO(
    val indexName: String,
    val indexSessionId: String,
    val indexDatetime: String,
    val documentCount: Int,
    val chunkCount: Int,
    val isCurrent: Boolean,
)

data class VectorStoreIndexListDTO(
    val indexes: List<VectorStoreIndexDTO>,
)

data class VectorStoreInspectionDocumentsFilterDTO(
    val text: String? = null,
    val documentId: String? = null,
    val anomaly: String? = null,
)

data class VectorStoreInspectionDocumentsRequestDTO(
    val indexName: String,
    val filter: VectorStoreInspectionDocumentsFilterDTO? = null,
    val start: Int,
    val size: Int,
    val includeStats: Boolean = true,
    val includeChunks: Boolean = true,
)

data class VectorStoreInspectionCondenseRequestDTO(
    val question: String,
)

data class VectorStoreInspectionCompressionOverrideDTO(
    val minScore: Double,
    val maxDocuments: Int,
    val fillToMaxDocuments: Boolean,
)

data class VectorStoreInspectionSearchRequestDTO(
    val indexName: String,
    val searchType: DocumentSearchType,
    val query: String,
    val keyWords: List<String> = emptyList(),
    val fetchK: Int,
    val k: Int,
    val compressionEnabled: Boolean,
    /** Public Studio value: beforeCut or afterCut. */
    val compressionStage: String,
    val compressionOverride: VectorStoreInspectionCompressionOverrideDTO? = null,
    val pinnedChunkIds: List<String> = emptyList(),
    val pinnedRankStrategy: String = "score_only",
)

data class VectorStoreInspectionSearchResponseDTO(
    val funnel: VectorStoreInspectionSearchFunnel,
    /** Public Studio value: beforeCut or afterCut. */
    val compressionStage: String,
    val results: List<VectorStoreInspectionSearchResultChunk>,
    val duration: Double,
)
