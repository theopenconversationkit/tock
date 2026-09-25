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

package ai.tock.bot.mongo

import ai.tock.bot.admin.knowledgebase.KnowledgeBaseDAO
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntry
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseIndex
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJob
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobState
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseProjection
import ai.tock.shared.ensureIndex
import org.litote.kmongo.and
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.deleteMany
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.`in`
import org.litote.kmongo.ne
import org.litote.kmongo.save
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOne

internal object KnowledgeBaseMongoDAO : KnowledgeBaseDAO {
    private val entries = MongoBotConfiguration.database.getCollection<KnowledgeBaseEntry>("knowledge_base_entry")
    private val projections = MongoBotConfiguration.database.getCollection<KnowledgeBaseProjection>("knowledge_base_projection")
    private val jobs = MongoBotConfiguration.database.getCollection<KnowledgeBaseJob>("knowledge_base_job")
    private val indexes = MongoBotConfiguration.database.getCollection<KnowledgeBaseIndex>("knowledge_base_index")

    init {
        entries.ensureIndex(KnowledgeBaseEntry::namespace, KnowledgeBaseEntry::botIds)
        entries.ensureIndex(KnowledgeBaseEntry::pendingJobId)
        projections.ensureIndex(KnowledgeBaseProjection::namespace, KnowledgeBaseProjection::botId, KnowledgeBaseProjection::targetId)
        jobs.ensureIndex(KnowledgeBaseJob::state, KnowledgeBaseJob::startedAt)
        jobs.ensureIndex(KnowledgeBaseJob::namespace, KnowledgeBaseJob::botId, KnowledgeBaseJob::state)
    }

    override fun deleteBot(
        namespace: String,
        botId: String,
    ) {
        entries.deleteMany(KnowledgeBaseEntry::namespace eq namespace, KnowledgeBaseEntry::botIds `in` listOf(botId))
        projections.deleteMany(KnowledgeBaseProjection::namespace eq namespace, KnowledgeBaseProjection::botId eq botId)
        jobs.deleteMany(KnowledgeBaseJob::namespace eq namespace, KnowledgeBaseJob::botId eq botId)
        indexes.deleteMany(KnowledgeBaseIndex::namespace eq namespace, KnowledgeBaseIndex::botId eq botId)
    }

    override fun entries(
        namespace: String,
        botId: String,
    ): List<KnowledgeBaseEntry> = entries.find(KnowledgeBaseEntry::namespace eq namespace, KnowledgeBaseEntry::botIds `in` listOf(botId)).toList()

    override fun saveEntry(entry: KnowledgeBaseEntry) {
        entries.save(entry)
    }

    override fun pendingEntries(): List<KnowledgeBaseEntry> = entries.find(KnowledgeBaseEntry::pendingJobId ne null).toList()

    override fun acknowledge(entry: KnowledgeBaseEntry) {
        entries.updateOne(and(KnowledgeBaseEntry::_id eq entry._id, KnowledgeBaseEntry::revision eq entry.revision), setValue(KnowledgeBaseEntry::pendingJobId, null))
    }

    override fun markProjectionPending(
        entry: KnowledgeBaseEntry,
        jobId: String,
    ) {
        entries.updateOne(and(KnowledgeBaseEntry::_id eq entry._id, KnowledgeBaseEntry::revision eq entry.revision), setValue(KnowledgeBaseEntry::pendingJobId, jobId))
    }

    override fun projections(
        namespace: String,
        botId: String,
        targetId: String,
    ): List<KnowledgeBaseProjection> =
        projections.find(KnowledgeBaseProjection::namespace eq namespace, KnowledgeBaseProjection::botId eq botId, KnowledgeBaseProjection::targetId eq targetId).toList()

    override fun saveProjection(projection: KnowledgeBaseProjection) {
        projections.save(projection)
    }

    override fun deleteProjection(id: String) {
        projections.deleteOneById(id)
    }

    override fun index(id: String): KnowledgeBaseIndex? = indexes.findOneById(id)

    override fun saveIndex(index: KnowledgeBaseIndex) {
        indexes.save(index)
    }

    override fun saveJob(job: KnowledgeBaseJob) {
        jobs.save(job)
    }

    override fun job(id: String): KnowledgeBaseJob? = jobs.findOneById(id)

    override fun activeJobs(
        namespace: String,
        botId: String,
    ): List<KnowledgeBaseJob> =
        jobs
            .find(
                KnowledgeBaseJob::namespace eq namespace,
                KnowledgeBaseJob::botId eq botId,
                KnowledgeBaseJob::state `in` listOf(KnowledgeBaseJobState.QUEUED, KnowledgeBaseJobState.RUNNING),
            ).ascendingSort(KnowledgeBaseJob::startedAt)
            .toList()

    override fun nextJob(): KnowledgeBaseJob? =
        jobs.find(KnowledgeBaseJob::state eq KnowledgeBaseJobState.RUNNING).ascendingSort(KnowledgeBaseJob::startedAt).firstOrNull()
            ?: jobs.find(KnowledgeBaseJob::state eq KnowledgeBaseJobState.QUEUED).ascendingSort(KnowledgeBaseJob::startedAt).firstOrNull()
}
