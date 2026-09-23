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

package ai.tock.bot.admin.model.knowledgebase

import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntryStatus
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobFailure
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobProgress
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobState
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobType
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseProjectionState
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseEntryPayload(
    val title: String,
    val searchHints: List<String> = emptyList(),
    val content: String,
    val sourceUrl: String? = null,
    val tags: List<String> = emptyList(),
    val status: KnowledgeBaseEntryStatus = KnowledgeBaseEntryStatus.DRAFT,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseEntryDTO(
    val id: String,
    val namespace: String,
    val botIds: List<String>,
    val title: String,
    val searchHints: List<String>,
    val content: String,
    val sourceUrl: String?,
    val tags: List<String>,
    val status: KnowledgeBaseEntryStatus,
    val contentHash: String,
    val projectionState: KnowledgeBaseProjectionState,
    val projectedAt: Instant?,
    val projectedIndexSessionId: String?,
    val createdAt: Instant,
    val createdBy: String,
    val updatedAt: Instant?,
    val updatedBy: String?,
    val projectionError: String? = null,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBasePage(
    val rows: List<KnowledgeBaseEntryDTO>,
    val total: Int,
    val start: Int,
    val end: Int,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseCounts(
    val total: Int,
    val draft: Int,
    val published: Int,
    val indexed: Int,
    val pending: Int,
    val orphan: Int,
    val failed: Int = 0,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseSyncStatus(
    val indexMode: String,
    val indexSessionId: String?,
    val indexName: String?,
    val embeddingModelKnown: Boolean,
    val embeddingModel: String?,
    val lastProjectionAt: Instant?,
    val counts: KnowledgeBaseCounts,
    val canCreateIndex: Boolean,
    val embeddingMismatch: Boolean = false,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseJobDTO(
    val id: String,
    val type: KnowledgeBaseJobType,
    val state: KnowledgeBaseJobState,
    val startedAt: Instant,
    val endedAt: Instant?,
    val progress: KnowledgeBaseJobProgress,
    val failures: List<KnowledgeBaseJobFailure>,
    val projected: Int,
    val removed: Int,
    val syncStatus: KnowledgeBaseSyncStatus?,
    val error: String?,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseSaveResult(
    val entry: KnowledgeBaseEntryDTO,
    val job: KnowledgeBaseJobDTO,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseBulkStatus(
    val entryIds: List<String>,
    val status: KnowledgeBaseEntryStatus,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseImportRow(
    val payload: KnowledgeBaseEntryPayload,
    val sourceId: String? = null,
    val issues: List<String> = emptyList(),
    val rejected: Boolean = false,
    val faqEnabled: Boolean? = null,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseImportPreview(
    val rows: List<KnowledgeBaseImportRow>,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseImportCandidate(
    val payload: KnowledgeBaseEntryPayload,
    val sourceId: String?,
    val state: String,
    val existingEntryId: String?,
    val issues: List<String>,
    val faqEnabled: Boolean?,
)

enum class KnowledgeBaseDuplicatePolicy { SKIP, UPDATE, CREATE }

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseImportRequest(
    val candidates: List<KnowledgeBaseImportCandidate>,
    val duplicatePolicy: KnowledgeBaseDuplicatePolicy,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseImportResult(
    val created: Int,
    val updated: Int,
    val skipped: Int,
    val failed: Int,
    val entryIds: List<String>,
    val job: KnowledgeBaseJobDTO?,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseExportedEntry(
    val title: String,
    val searchHints: List<String>,
    val content: String,
    val sourceUrl: String?,
    val tags: List<String>,
    val status: KnowledgeBaseEntryStatus,
    val sourceId: String,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseExportEnvelope(
    val namespace: String,
    val botId: String,
    val entries: List<KnowledgeBaseExportedEntry>,
    val format: String = "tock-knowledge-base",
    val version: Int = 1,
    val exportedAt: Instant = Instant.now(),
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseRetrievalRequest(
    val question: String,
    val entryId: String? = null,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseRetrievalHit(
    val rank: Int,
    val score: Double?,
    val title: String,
    val source: String?,
    val sourceType: String,
    val kbEntryId: String?,
    val content: String,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class KnowledgeBaseRetrievalTest(
    val question: String,
    val indexSessionId: String,
    val k: Int,
    val entryRank: Int?,
    val hits: List<KnowledgeBaseRetrievalHit>,
)
