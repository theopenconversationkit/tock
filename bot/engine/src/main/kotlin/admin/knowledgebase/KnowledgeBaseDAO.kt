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

interface KnowledgeBaseDAO {
    fun deleteBot(
        namespace: String,
        botId: String,
    )

    fun entries(
        namespace: String,
        botId: String,
    ): List<KnowledgeBaseEntry>

    fun saveEntry(entry: KnowledgeBaseEntry)

    fun pendingEntries(): List<KnowledgeBaseEntry>

    fun acknowledge(entry: KnowledgeBaseEntry)

    fun markProjectionPending(
        entry: KnowledgeBaseEntry,
        jobId: String,
    )

    fun projections(
        namespace: String,
        botId: String,
        targetId: String,
    ): List<KnowledgeBaseProjection>

    fun saveProjection(projection: KnowledgeBaseProjection)

    fun deleteProjection(id: String)

    fun index(id: String): KnowledgeBaseIndex?

    fun saveIndex(index: KnowledgeBaseIndex)

    fun saveJob(job: KnowledgeBaseJob)

    fun job(id: String): KnowledgeBaseJob?

    fun activeJobs(
        namespace: String,
        botId: String,
    ): List<KnowledgeBaseJob>

    /** Read-only peek, also used when idle. Processing requires the shared worker lock and a fresh read. */
    fun nextJob(): KnowledgeBaseJob?
}
