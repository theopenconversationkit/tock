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

package ai.tock.bot.admin.knowledgebase

import java.time.Instant
import java.util.UUID

enum class KnowledgeBaseEntryStatus { DRAFT, PUBLISHED }

enum class KnowledgeBaseProjectionState { INDEXED, PENDING, ORPHAN, NONE }

data class KnowledgeBaseEntry(
    val namespace: String,
    val botIds: List<String>,
    val title: String,
    val searchHints: List<String>,
    val content: String,
    val sourceUrl: String?,
    val tags: List<String>,
    val status: KnowledgeBaseEntryStatus,
    val contentHash: String,
    val createdBy: String,
    val createdAt: Instant = Instant.now(),
    val updatedBy: String? = null,
    val updatedAt: Instant? = null,
    val _id: String = UUID.randomUUID().toString(),
    val revision: String = UUID.randomUUID().toString(),
    // A durable outbox in the same atomic document write; cleared with a revision guard.
    val pendingJobId: String? = null,
    // Tombstones preserve failed deletions and allow cleanup after returning to an old session.
    val deleted: Boolean = false,
    val sourceId: String? = null,
    // Never-published drafts do not require a vector-store connection.
    val everPublished: Boolean = status == KnowledgeBaseEntryStatus.PUBLISHED,
)
