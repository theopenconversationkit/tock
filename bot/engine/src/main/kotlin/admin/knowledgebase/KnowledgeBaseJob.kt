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

enum class KnowledgeBaseJobType { SAVE_ENTRY, DELETE_ENTRY, PUBLISH, UNPUBLISH, REPAIR_INDEX, CREATE_INDEX, VERIFY_INDEX }

enum class KnowledgeBaseJobState { QUEUED, RUNNING, COMPLETED, FAILED }

data class KnowledgeBaseJobProgress(
    val total: Int = 0,
    val done: Int = 0,
    val failed: Int = 0,
)

data class KnowledgeBaseJobFailure(
    val entryId: String,
    val title: String,
    val error: String,
)

data class KnowledgeBaseJob(
    val namespace: String,
    val botId: String,
    val type: KnowledgeBaseJobType,
    val entryIds: List<String> = emptyList(),
    val _id: String = UUID.randomUUID().toString(),
    val state: KnowledgeBaseJobState = KnowledgeBaseJobState.QUEUED,
    val startedAt: Instant = Instant.now(),
    val endedAt: Instant? = null,
    val progress: KnowledgeBaseJobProgress = KnowledgeBaseJobProgress(),
    val failures: List<KnowledgeBaseJobFailure> = emptyList(),
    val projected: Int = 0,
    val removed: Int = 0,
    val error: String? = null,
    // Allocated once, so CREATE_INDEX retries reuse the same collection.
    val indexSessionId: String? = null,
)
