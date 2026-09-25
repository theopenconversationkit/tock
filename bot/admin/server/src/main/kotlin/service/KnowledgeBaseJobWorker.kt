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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseDAO
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntryStatus
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseIndex
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJob
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobFailure
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobProgress
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobState
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobType
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseProjection
import ai.tock.bot.engine.user.UserLock
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseDeleteRequest
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseDeletion
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseDocument
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseIndexRequest
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseTargetRequest
import ai.tock.genai.orchestratorclient.services.KnowledgeBaseIndexingService
import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.longProperty
import ai.tock.shared.provide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

class KnowledgeBaseJobProcessor(
    private val service: KnowledgeBaseService = KnowledgeBaseService.default,
    private val indexing: KnowledgeBaseIndexingService = injector.provide(),
    private val ragDAO: BotRAGConfigurationDAO = injector.provide(),
) {
    private val dao: KnowledgeBaseDAO get() = service.dao

    /** The unlocked read is only a wake-up hint; work is read again under the shared lease. */
    internal suspend fun processPending(lock: UserLock) {
        val pending =
            withContext(Dispatchers.IO) {
                dao.nextJob() != null ||
                    dao.pendingEntries().any { entry -> entry.pendingJobId?.let { dao.job(it) == null } == true }
            }
        if (!pending) return

        // Renew the Mongo lease separately from blocking IO, including during recovery.
        lock.withLock("knowledge-base-projection-worker") {
            withContext(Dispatchers.IO) {
                while (processNext()) {
                    kotlinx.coroutines.currentCoroutineContext().ensureActive()
                }
            }
        }
    }

    /** Caller owns the shared worker lock, including while resuming RUNNING jobs. */
    suspend fun processNext(): Boolean {
        service.recoverOutbox()
        val queued = dao.nextJob() ?: return false
        process(queued)
        return true
    }

    internal suspend fun process(queued: KnowledgeBaseJob) {
        var job = queued.copy(state = KnowledgeBaseJobState.RUNNING, endedAt = null, error = null, failures = emptyList(), projected = 0, removed = 0)
        dao.saveJob(job)
        try {
            val create = job.type == KnowledgeBaseJobType.CREATE_INDEX
            val target = service.target(job.namespace, job.botId, if (create) job.indexSessionId else null)
            if (create) {
                check(target != null) { "knowledge-base.validation.index_configuration" }
                check(target.rag.indexSessionId.isNullOrBlank() || target.rag.indexSessionId == target.session) { "knowledge-base.job.configuration_changed" }
            }
            if (target != null && job.type in listOf(KnowledgeBaseJobType.REPAIR_INDEX, KnowledgeBaseJobType.VERIFY_INDEX)) verify(target)
            if (job.type == KnowledgeBaseJobType.VERIFY_INDEX) {
                dao.saveJob(job.copy(state = KnowledgeBaseJobState.COMPLETED, endedAt = Instant.now()))
                return
            }
            val all = dao.entries(job.namespace, job.botId)
            val projections = target?.let { dao.projections(job.namespace, job.botId, it.id) }.orEmpty().associateBy { it.entryId }
            val ids =
                if (create || job.type == KnowledgeBaseJobType.REPAIR_INDEX) {
                    (all.filter { (!it.deleted && it.status == KnowledgeBaseEntryStatus.PUBLISHED) || it.pendingJobId != null }.map { it._id } + projections.keys).distinct()
                } else {
                    job.entryIds.distinct()
                }
            job = job.copy(progress = KnowledgeBaseJobProgress(total = ids.size))
            dao.saveJob(job)
            ids.forEach { id ->
                kotlinx.coroutines.currentCoroutineContext().ensureActive()
                val entry = dao.entries(job.namespace, job.botId).firstOrNull { it._id == id }
                val projection = projections[id]
                try {
                    if (target != null) {
                        val current = service.target(job.namespace, job.botId, if (create) target.session else null)
                        check(current?.id == target.id && current.embeddingFingerprint == target.embeddingFingerprint) { "knowledge-base.job.configuration_changed" }
                        if (create) check(current.rag.indexSessionId.isNullOrBlank() || current.rag.indexSessionId == target.session) { "knowledge-base.job.configuration_changed" }
                        val published = entry != null && !entry.deleted && entry.status == KnowledgeBaseEntryStatus.PUBLISHED
                        if (published) {
                            val index = dao.index(target.id)
                            check(index == null || index.embeddingFingerprint == target.embeddingFingerprint) { "knowledge-base.job.embedding_changed" }
                            if (index == null) dao.saveIndex(KnowledgeBaseIndex(target.id, job.namespace, job.botId, target.session, target.embeddingFingerprint, target.embeddingModel, create))
                            val expectedRowId = "kb-" + KnowledgeBaseService.hash("${target.indexName}/$id")
                            if (projection?.contentHash != entry!!.contentHash || projection?.rowIds != listOf(expectedRowId)) {
                                val response =
                                    indexing.index(
                                        KnowledgeBaseIndexRequest(
                                            target.setting,
                                            target.indexName,
                                            target.indexPrefix,
                                            target.rag.emSetting,
                                            target.session,
                                            listOf(KnowledgeBaseDocument(id, entry.title, entry.searchHints, entry.content, entry.sourceUrl)),
                                        ),
                                    )
                                kotlinx.coroutines.currentCoroutineContext().ensureActive()
                                val result = response.results.single()
                                check(result.error == null && result.count == 1 && result.rowIds.size == 1) { result.error ?: "knowledge-base.job.index_failed" }
                                val obsolete = projection?.rowIds.orEmpty().filter { it !in result.rowIds }
                                if (obsolete.isNotEmpty()) job = job.copy(removed = job.removed + remove(target, id, obsolete))
                                dao.saveProjection(KnowledgeBaseProjection("${target.id}/$id", job.namespace, job.botId, target.id, target.session, id, entry.contentHash, result.rowIds, entry.title))
                                job = job.copy(projected = job.projected + result.count)
                            }
                        } else if (projection != null || entry == null || entry.everPublished) {
                            // Attempt deterministic removal even without a journal: a previous process may have died after writing the vector.
                            job = job.copy(removed = job.removed + remove(target, id, projection?.rowIds.orEmpty()))
                            projection?.let { dao.deleteProjection(it._id) }
                        }
                    }
                    kotlinx.coroutines.currentCoroutineContext().ensureActive()
                    if (entry != null) dao.acknowledge(entry)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    if (entry != null) dao.markProjectionPending(entry, job._id)
                    val safeError = e.message?.takeIf { it.startsWith("knowledge-base.") } ?: "knowledge-base.job.projection_failed"
                    job = job.copy(failures = job.failures + KnowledgeBaseJobFailure(id, entry?.title ?: projection?.title ?: id, safeError))
                }
                job = job.copy(progress = job.progress.copy(done = job.progress.done + 1, failed = job.failures.size))
                dao.saveJob(job)
            }
            if (target != null && job.failures.isNotEmpty()) {
                try {
                    verify(target)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                }
            }
            if (create && job.failures.isEmpty()) {
                check(target != null && ids.isNotEmpty()) { "knowledge-base.validation.index_configuration" }
                val current = checkNotNull(ragDAO.findByNamespaceAndBotId(job.namespace, job.botId))
                val freshTarget = service.target(job.namespace, job.botId, target.session)
                check(freshTarget?.id == target.id && freshTarget.embeddingFingerprint == target.embeddingFingerprint) { "knowledge-base.job.configuration_changed" }
                check(current.indexSessionId.isNullOrBlank() || current.indexSessionId == target.session) { "knowledge-base.job.configuration_changed" }
                val updated = current.copy(indexSessionId = target.session, enabled = true)
                if (current != updated) RAGService.activateKnowledgeBaseIndex(current, updated)
            }
            dao.saveJob(job.copy(state = KnowledgeBaseJobState.COMPLETED, endedAt = Instant.now()))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            dao.saveJob(
                job.copy(
                    state = KnowledgeBaseJobState.FAILED,
                    endedAt = Instant.now(),
                    error = e.message?.takeIf { it.startsWith("knowledge-base.") } ?: "knowledge-base.job.projection_failed",
                ),
            )
        }
    }

    private suspend fun remove(
        target: KnowledgeBaseTarget,
        entryId: String,
        rowIds: List<String>,
    ): Int {
        val result = indexing.delete(KnowledgeBaseDeleteRequest(target.setting, target.indexName, target.indexPrefix, listOf(KnowledgeBaseDeletion(entryId, rowIds)))).results.single()
        kotlinx.coroutines.currentCoroutineContext().ensureActive()
        check(result.error == null) { result.error ?: "knowledge-base.job.delete_failed" }
        return result.count
    }

    private suspend fun verify(target: KnowledgeBaseTarget) {
        // Finish the remote read before touching the journal: a provider outage must not masquerade as an empty index.
        val rows = indexing.rows(KnowledgeBaseTargetRequest(target.setting, target.indexName, target.indexPrefix)).rows
        kotlinx.coroutines.currentCoroutineContext().ensureActive()
        val old = dao.projections(target.namespace, target.botId, target.id)
        val groups = rows.groupBy { it.entryId }
        old.filter { it.entryId !in groups }.forEach { dao.deleteProjection(it._id) }
        groups.forEach { (id, stored) ->
            val hash = if (stored.size == 1) stored.single().contentHash else ""
            dao.saveProjection(
                KnowledgeBaseProjection(
                    "${target.id}/$id",
                    target.namespace,
                    target.botId,
                    target.id,
                    target.session,
                    id,
                    hash,
                    stored.map { it.rowId },
                    stored.first().title,
                    old.firstOrNull { it.entryId == id && it.contentHash == stored.first().contentHash }?.projectedAt ?: Instant.now(),
                ),
            )
        }
    }
}

object KnowledgeBaseJobWorker {
    private val started = AtomicBoolean(false)
    private val processing = AtomicBoolean(false)
    private val logger = KotlinLogging.logger {}
    private val executor: Executor by lazy { injector.provide() }
    private val lock: UserLock by lazy { injector.provide() }

    fun start() {
        if (!started.compareAndSet(false, true)) return
        executor.setPeriodic(Duration.ofMillis(1), Duration.ofMillis(longProperty("tock_knowledge_base_worker_poll_interval_ms", 1000))) {
            if (processing.compareAndSet(false, true)) {
                executor.executeBlocking {
                    try {
                        runBlocking {
                            KnowledgeBaseJobProcessor().processPending(lock)
                        }
                    } catch (_: Exception) {
                        logger.warn { "Knowledge base worker interrupted; persisted work will be retried." }
                    } finally {
                        processing.set(false)
                    }
                }
            }
        }
    }
}
