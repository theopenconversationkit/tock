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

package ai.tock.genai.orchestratorclient.responses

import ai.tock.genai.orchestratorcore.models.vectorstore.DocumentSearchType
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreProvider
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude

data class VectorStoreCapabilitiesResponse(
    val provider: VectorStoreProvider,
    val searchTypes: List<DocumentSearchType>,
    val supportsScores: Boolean,
    val supportsIndexListing: Boolean,
    val supportsMetadataFilter: Boolean,
    val notes: List<String> = emptyList(),
)

data class VectorStoreIndexDescription(
    val indexName: String,
    val indexSessionId: String,
    val indexDatetime: String,
    val documentCount: Int,
    val chunkCount: Int,
)

data class VectorStoreIndexListResponse(
    val indexes: List<VectorStoreIndexDescription> = emptyList(),
)

data class VectorStoreIndexAnomaly(
    val code: String,
    val count: Int,
    val severity: String,
)

data class VectorStoreIndexStats(
    val documentCount: Int,
    val chunkCount: Int,
    val chunksPerDocumentAvg: Double,
    val chunkLengthMedian: Double,
    val indexDatetime: String,
)

data class InspectedChunk(
    val chunkId: String,
    val chunk: String,
    val content: String,
    val contentLength: Int,
    val metadata: Map<String, Any?>,
)

data class InspectedDocument(
    val documentId: String,
    val title: String,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val source: String?,
    val chunkCount: Int,
    val indexSessionId: String,
    val chunks: List<InspectedChunk>? = null,
)

data class VectorStoreInspectionDocumentsResponse(
    val stats: VectorStoreIndexStats? = null,
    val anomalies: List<VectorStoreIndexAnomaly> = emptyList(),
    val rows: List<InspectedDocument> = emptyList(),
    val total: Int,
    val start: Int,
    val end: Int,
)

data class VectorStoreInspectionCondenseResponse(
    val condensedQuestion: String,
    val keyWords: List<String> = emptyList(),
    val effectivePrompt: String,
    val duration: Double,
)

data class VectorStoreInspectionFunnelStage(
    val status: String,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val count: Int?,
    val reason: String? = null,
    val discarded: Int? = null,
)

data class VectorStoreInspectionSearchFunnel(
    val vector: VectorStoreInspectionFunnelStage,
    val fts: VectorStoreInspectionFunnelStage,
    val rrf: VectorStoreInspectionFunnelStage,
    @param:JsonAlias("top_k_cut")
    val topKCut: VectorStoreInspectionFunnelStage,
    val compression: VectorStoreInspectionFunnelStage,
)

data class VectorStoreInspectionChannelRanks(
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val vector: Int? = null,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val fts: Int? = null,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val rrf: Int? = null,
)

data class VectorStoreInspectionChannelScores(
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val vector: Double? = null,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val fts: Double? = null,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val rrf: Double? = null,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val compressor: Double? = null,
)

data class VectorStoreInspectionSearchResultChunk(
    val chunkId: String,
    val documentId: String,
    val title: String,
    val chunk: String,
    val content: String,
    val ranks: VectorStoreInspectionChannelRanks,
    val scores: VectorStoreInspectionChannelScores,
    val outcome: String,
    val pinned: Boolean,
    val metadata: Map<String, Any?>? = null,
)

data class VectorStoreInspectionSearchResponse(
    val funnel: VectorStoreInspectionSearchFunnel,
    /** Internal orchestrator value: before_cut or after_cut. */
    val compressionStage: String,
    val results: List<VectorStoreInspectionSearchResultChunk>,
    val duration: Double,
)
