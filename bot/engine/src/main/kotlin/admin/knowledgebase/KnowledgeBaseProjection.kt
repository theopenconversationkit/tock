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

data class KnowledgeBaseProjection(
    val _id: String,
    val namespace: String,
    val botId: String,
    val targetId: String,
    val indexSessionId: String,
    val entryId: String,
    val contentHash: String,
    val rowIds: List<String>,
    val title: String,
    val projectedAt: Instant = Instant.now(),
)

/** Provenance and embedding identity; survives empty indexes and finished job cleanup. */
data class KnowledgeBaseIndex(
    val _id: String,
    val namespace: String,
    val botId: String,
    val indexSessionId: String,
    val embeddingFingerprint: String,
    val embeddingModel: String,
    val managed: Boolean = false,
)
