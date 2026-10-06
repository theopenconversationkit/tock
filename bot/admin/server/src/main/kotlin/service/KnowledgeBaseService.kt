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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseDAO
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntry
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntryStatus
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJob
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobState
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobType
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseProjection
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseProjectionState
import ai.tock.bot.admin.model.genai.VectorStoreInspectionCondenseRequestDTO
import ai.tock.bot.admin.model.genai.VectorStoreInspectionSearchRequestDTO
import ai.tock.bot.admin.model.knowledgebase.CreateIndexBlocker
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseBulkStatus
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseCollectionInfo
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseCounts
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseEntryDTO
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseEntryPayload
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseIndexState
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseJobDTO
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBasePage
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseRetrievalHit
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseRetrievalRequest
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseRetrievalTest
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseSaveResult
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseSyncStatus
import ai.tock.bot.engine.user.UserLock
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseTargetRequest
import ai.tock.genai.orchestratorclient.responses.KnowledgeBaseIndexStateResponse
import ai.tock.genai.orchestratorclient.services.KnowledgeBaseIndexingService
import ai.tock.genai.orchestratorcore.models.em.EMSettingBase
import ai.tock.genai.orchestratorcore.models.vectorstore.DocumentSearchType
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreProvider
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreSetting
import ai.tock.genai.orchestratorcore.utils.VectorStoreUtils
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.provide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URI
import java.security.MessageDigest
import java.time.Instant
import java.util.Locale
import java.util.UUID

/** Short Mongo mutations use a separate lock from the projection worker: editing remains possible during indexing. */
class KnowledgeBaseService(
    val dao: KnowledgeBaseDAO = injector.provide(),
    private val lock: UserLock = injector.provide(),
    private val indexing: KnowledgeBaseIndexingService = injector.provide(),
) {
    companion object {
        val default: KnowledgeBaseService by lazy { KnowledgeBaseService() }

        fun hash(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

        fun normalizeTitle(value: String): String = value.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

        fun contentHash(payload: KnowledgeBaseEntryPayload): String {
            val hints = payload.searchHints.joinToString("\n").let { if (it.isEmpty()) "" else "$it\n\n" }
            return hash("${payload.title}\n\n```markdown\n$hints${payload.content}\n```\n${payload.sourceUrl.orEmpty()}")
        }

        fun validated(payload: KnowledgeBaseEntryPayload): KnowledgeBaseEntryPayload {
            val clean =
                payload.copy(
                    title = payload.title.trim(),
                    searchHints =
                        payload.searchHints
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct(),
                    content = payload.content.trim(),
                    sourceUrl = payload.sourceUrl?.trim()?.takeIf { it.isNotEmpty() },
                    tags =
                        payload.tags
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct(),
                )
            require(clean.title.isNotEmpty() && clean.title.length <= 300) { "knowledge-base.validation.title" }
            require(clean.content.isNotEmpty() && clean.content.length <= 12000) { "knowledge-base.validation.content" }
            require(clean.searchHints.size <= 30 && clean.searchHints.all { it.length <= 300 }) { "knowledge-base.validation.hints" }
            require(clean.tags.size <= 30 && clean.tags.all { it.length <= 100 }) { "knowledge-base.validation.tags" }
            require(
                clean.sourceUrl == null ||
                    runCatching {
                        URI(clean.sourceUrl).let { it.scheme in setOf("http", "https") && !it.host.isNullOrBlank() && it.userInfo == null }
                    }.getOrDefault(false),
            ) { "knowledge-base.validation.url" }
            return clean
        }

        fun projectionState(
            entry: KnowledgeBaseEntry,
            projection: KnowledgeBaseProjection?,
            compatible: Boolean = true,
        ): KnowledgeBaseProjectionState =
            if (entry.deleted || entry.status == KnowledgeBaseEntryStatus.DRAFT) {
                if (projection != null) KnowledgeBaseProjectionState.ORPHAN else KnowledgeBaseProjectionState.NONE
            } else if (projection != null && projection.contentHash == entry.contentHash && compatible) {
                KnowledgeBaseProjectionState.INDEXED
            } else {
                KnowledgeBaseProjectionState.PENDING
            }
    }

    fun <T> mutate(
        namespace: String,
        botId: String,
        action: () -> T,
    ): T =
        runBlocking {
            lock.withLock("knowledge-base-mutations:${hash("$namespace/$botId")}") { withContext(Dispatchers.IO) { action() } }
        }

    fun purge(
        namespace: String,
        botId: String,
    ) = runBlocking {
        lock.withLock("knowledge-base-projection-worker") {
            withContext(Dispatchers.IO) { mutate(namespace, botId) { dao.deleteBot(namespace, botId) } }
        }
    }

    fun target(
        namespace: String,
        botId: String,
        session: String? = null,
    ): KnowledgeBaseTarget? {
        val rag = RAGService.getRAGConfiguration(namespace, botId) ?: return null
        val indexSession = session ?: rag.indexSessionId?.takeIf { it.isNotBlank() } ?: return null
        val setting = VectorStoreService.getVectorStoreConfiguration(namespace, botId, enabled = true)?.setting
        val (_, name) = VectorStoreUtils.getVectorStoreElements(namespace, botId, indexSession, rag.maxDocumentsRetrieved, rag.documentSearchType, setting)
        val (_, prefix) = VectorStoreUtils.getVectorStoreElements(namespace, botId, "", rag.maxDocumentsRetrieved, rag.documentSearchType, setting)
        val identity = mapper.writeValueAsString(BotHistoryService.snapshot(setting))
        return KnowledgeBaseTarget(
            hash("$namespace/$botId/$name/$identity"),
            namespace,
            botId,
            indexSession,
            name,
            prefix,
            rag,
            setting,
            // Normalized embedding model (single source of truth); null when unknown (e.g. Azure without a model).
            EmbeddingModelIdentity.normalized(rag.emSetting),
        )
    }

    /**
     * Probes the orchestrator once for the collection backing [target] and derives the index state, row counts,
     * contract metadata and embedding compatibility. Errors are not swallowed: an orchestrator outage must surface,
     * never masquerade as a missing index.
     */
    fun probe(target: KnowledgeBaseTarget): KnowledgeBaseIndexProbe {
        val state = indexing.indexState(KnowledgeBaseTargetRequest(target.setting, target.indexName, target.indexPrefix))
        val cmetadata = state.cmetadata
        val hasContractMetadata = cmetadata?.containsKey("schema_version") == true
        val collectionModel = EmbeddingModelIdentity.normalizeCollectionModel(cmetadata?.get("embedding_model") as? String)
        val rowCount = state.rowCount
        val kbRowCount = state.kbRowCount
        return KnowledgeBaseIndexProbe(
            deriveIndexState(state),
            kbRowCount,
            if (rowCount != null && kbRowCount != null) rowCount - kbRowCount else null,
            if (hasContractMetadata) {
                KnowledgeBaseCollectionInfo(
                    cmetadata["origin"] as? String,
                    cmetadata["created_at"] as? String,
                    cmetadata["created_by"] as? String,
                    cmetadata["embedding_provider"] as? String,
                    cmetadata["embedding_model"] as? String,
                )
            } else {
                null
            },
            EmbeddingModelIdentity.coherence(collectionModel, target.embeddingModel) == EmbeddingCoherence.MISMATCH,
        )
    }

    // PGVector only: MISSING when the collection is absent, or empty with no Tock contract metadata (created implicitly
    // by a runtime query). OpenSearch reports null counts/cmetadata, so exists → READY, which is not covered here.
    private fun deriveIndexState(state: KnowledgeBaseIndexStateResponse): KnowledgeBaseIndexState =
        when {
            !state.exists -> {
                KnowledgeBaseIndexState.MISSING
            }

            state.rowCount != null && state.rowCount == 0 && state.cmetadata?.containsKey("schema_version") != true -> {
                KnowledgeBaseIndexState.MISSING
            }

            else -> {
                KnowledgeBaseIndexState.READY
            }
        }

    /**
     * Resolves the collection a candidate RAG configuration would use (same resolution as [target]) and reports its
     * state and embedding coherence against the candidate embedding setting. Used by the RAG settings save validation
     * and its index-status endpoint. PGVector only. Errors are not swallowed.
     */
    fun indexStatusFor(
        namespace: String,
        botId: String,
        indexSessionId: String?,
        emSetting: EMSettingBase<*>,
    ): KnowledgeBaseIndexStatus {
        val botModel = EmbeddingModelIdentity.normalized(emSetting)
        val session =
            indexSessionId?.takeIf { it.isNotBlank() }
                ?: return KnowledgeBaseIndexStatus(KnowledgeBaseIndexState.NONE, null, EmbeddingCoherence.UNKNOWN)
        val setting = VectorStoreService.getVectorStoreConfiguration(namespace, botId, enabled = true)?.setting
        // maxDocuments/searchType do not influence the collection name, so fixed defaults are fine here.
        val (_, name) = VectorStoreUtils.getVectorStoreElements(namespace, botId, session, 4, DocumentSearchType.HYBRID_SEARCH, setting)
        val (_, prefix) = VectorStoreUtils.getVectorStoreElements(namespace, botId, "", 4, DocumentSearchType.HYBRID_SEARCH, setting)
        val response = indexing.indexState(KnowledgeBaseTargetRequest(setting, name, prefix))
        val collectionModel = EmbeddingModelIdentity.normalizeCollectionModel(response.cmetadata?.get("embedding_model") as? String)
        return KnowledgeBaseIndexStatus(
            deriveIndexState(response),
            collectionModel,
            EmbeddingModelIdentity.coherence(collectionModel, botModel),
        )
    }

    /**
     * Why creating an index is currently impossible, evaluated in priority order. null means creation is allowed.
     * A missing RAG configuration is never blocking for the vector store itself (it falls back to the environment
     * default), but Tock cannot create a certified KB collection without one, nor without a known embedding model.
     */
    fun createIndexBlocker(
        namespace: String,
        botId: String,
    ): CreateIndexBlocker? {
        val rag = RAGService.getRAGConfiguration(namespace, botId) ?: return CreateIndexBlocker.RAG_NOT_CONFIGURED
        if (EmbeddingModelIdentity.normalized(rag.emSetting) == null) return CreateIndexBlocker.EMBEDDING_MODEL_UNDEFINED
        if (dao.entries(namespace, botId).none { !it.deleted && it.status == KnowledgeBaseEntryStatus.PUBLISHED }) {
            return CreateIndexBlocker.NO_PUBLISHED_ENTRY
        }
        return null
    }

    private fun entry(
        namespace: String,
        botId: String,
        id: String,
    ): KnowledgeBaseEntry = dao.entries(namespace, botId).firstOrNull { it._id == id && !it.deleted } ?: throw NotFoundException(404, "Knowledge base entry not found")

    fun sync(
        namespace: String,
        botId: String,
    ): KnowledgeBaseSyncStatus {
        val entries = dao.entries(namespace, botId)
        val target = target(namespace, botId)
        val rag = RAGService.getRAGConfiguration(namespace, botId)
        // Single orchestrator probe per request (never in a loop). Errors are not swallowed here.
        val probe = target?.let { probe(it) }
        val projections = target?.let { dao.projections(namespace, botId, it.id) }.orEmpty()
        val byId = projections.associateBy { it.entryId }
        val compatible = probe?.embeddingIncompatible != true
        val live = entries.filterNot { it.deleted }
        val states = live.map { projectionState(it, byId[it._id], compatible) }
        val wanted = live.filter { it.status == KnowledgeBaseEntryStatus.PUBLISHED }.map { it._id }.toSet()
        return KnowledgeBaseSyncStatus(
            probe?.state ?: KnowledgeBaseIndexState.NONE,
            target?.session,
            target?.indexName,
            probe?.kbRowCount,
            probe?.otherRowCount,
            probe?.collection,
            probe?.embeddingIncompatible == true,
            createIndexBlocker(namespace, botId),
            rag?.enabled == true,
            projections.maxOfOrNull { it.projectedAt },
            KnowledgeBaseCounts(
                live.size,
                live.count { it.status == KnowledgeBaseEntryStatus.DRAFT },
                wanted.size,
                states.count { it == KnowledgeBaseProjectionState.INDEXED },
                states.count { it == KnowledgeBaseProjectionState.PENDING },
                projections.count { it.entryId !in wanted },
                entries.count { projectionError(it) != null },
            ),
        )
    }

    private fun dto(
        entry: KnowledgeBaseEntry,
        projection: KnowledgeBaseProjection?,
        compatible: Boolean,
    ): KnowledgeBaseEntryDTO =
        KnowledgeBaseEntryDTO(
            entry._id,
            entry.namespace,
            entry.botIds,
            entry.title,
            entry.searchHints,
            entry.content,
            entry.sourceUrl,
            entry.tags,
            entry.status,
            entry.contentHash,
            projectionState(entry, projection, compatible),
            projection?.projectedAt,
            projection?.indexSessionId,
            entry.createdAt,
            entry.createdBy,
            entry.updatedAt,
            entry.updatedBy,
            projectionError(entry),
        )

    private fun projectionError(entry: KnowledgeBaseEntry): String? =
        entry.pendingJobId?.let { id ->
            dao.job(id)?.let { job -> job.failures.firstOrNull { it.entryId == entry._id }?.error ?: job.error }
        }

    fun get(
        namespace: String,
        botId: String,
        id: String,
    ): KnowledgeBaseEntryDTO {
        val target = target(namespace, botId)
        val projection = target?.let { dao.projections(namespace, botId, it.id).firstOrNull { p -> p.entryId == id } }
        // Listing must stay usable even if the orchestrator is down: assume compatible on probe failure.
        val compatible = target == null || runCatching { !probe(target).embeddingIncompatible }.getOrDefault(true)
        return dto(entry(namespace, botId, id), projection, compatible)
    }

    fun search(
        namespace: String,
        botId: String,
        params: Map<String, String>,
    ): KnowledgeBasePage {
        val start = params["start"]?.toIntOrNull() ?: 0
        val size = params["size"]?.toIntOrNull() ?: 25
        require(start >= 0 && size in 1..100) { "Invalid pagination" }
        val target = target(namespace, botId)
        val projections = target?.let { dao.projections(namespace, botId, it.id) }.orEmpty().associateBy { it.entryId }
        // Listing must stay usable even if the orchestrator is down: assume compatible on probe failure.
        val compatible = target == null || runCatching { !probe(target).embeddingIncompatible }.getOrDefault(true)
        val query = params["search"]?.trim().orEmpty()
        val filtered =
            dao.entries(namespace, botId).filterNot { it.deleted }.map { dto(it, projections[it._id], compatible) }.filter { e ->
                (query.isEmpty() || listOf(e.title, e.content, e.searchHints.joinToString(" ")).any { it.contains(query, ignoreCase = true) }) &&
                    (params["status"].isNullOrBlank() || e.status.name == params["status"]) &&
                    (params["projectionState"].isNullOrBlank() || e.projectionState.name == params["projectionState"]) &&
                    (params["tag"].isNullOrBlank() || params["tag"] in e.tags)
            }
        val sorted =
            when (params["sort"]) {
                "title" -> filtered.sortedBy { normalizeTitle(it.title) }
                "status" -> filtered.sortedBy { it.status.name }
                else -> filtered.sortedBy { it.updatedAt ?: it.createdAt }
            }.let { if (params["direction"] == "asc") it else it.reversed() }
        val rows = sorted.drop(start).take(size)
        return KnowledgeBasePage(rows, sorted.size, start, start + rows.size)
    }

    fun tags(
        namespace: String,
        botId: String,
    ): List<String> =
        dao
            .entries(namespace, botId)
            .filterNot { it.deleted }
            .flatMap { it.tags }
            .distinct()
            .sorted()

    internal fun write(
        namespace: String,
        botId: String,
        payload: KnowledgeBaseEntryPayload,
        author: String,
        jobId: String,
        previous: KnowledgeBaseEntry? = null,
        sourceId: String? = null,
    ): KnowledgeBaseEntry {
        val p = validated(payload)
        val e =
            KnowledgeBaseEntry(
                namespace,
                listOf(botId),
                p.title,
                p.searchHints,
                p.content,
                p.sourceUrl,
                p.tags,
                p.status,
                contentHash(p),
                previous?.createdBy ?: author,
                previous?.createdAt ?: Instant.now(),
                if (previous != null) author else null,
                if (previous != null) Instant.now() else null,
                _id = previous?._id ?: UUID.randomUUID().toString(),
                pendingJobId = jobId,
                sourceId = previous?.sourceId ?: sourceId,
                everPublished = previous?.everPublished == true || p.status == KnowledgeBaseEntryStatus.PUBLISHED,
            )
        dao.saveEntry(e)
        return e
    }

    fun save(
        namespace: String,
        botId: String,
        payload: KnowledgeBaseEntryPayload,
        author: String,
        id: String? = null,
    ): KnowledgeBaseSaveResult =
        mutate(namespace, botId) {
            val jobId = UUID.randomUUID().toString()
            val e = write(namespace, botId, payload, author, jobId, id?.let { entry(namespace, botId, it) })
            val job = KnowledgeBaseJob(namespace, botId, KnowledgeBaseJobType.SAVE_ENTRY, listOf(e._id), _id = jobId)
            dao.saveJob(job)
            KnowledgeBaseSaveResult(get(namespace, botId, e._id), jobDTO(job))
        }

    fun delete(
        namespace: String,
        botId: String,
        id: String,
        author: String,
    ): KnowledgeBaseJobDTO =
        mutate(namespace, botId) {
            val previous = entry(namespace, botId, id)
            val job = KnowledgeBaseJob(namespace, botId, KnowledgeBaseJobType.DELETE_ENTRY, listOf(id))
            dao.saveEntry(
                previous.copy(deleted = true, status = KnowledgeBaseEntryStatus.DRAFT, updatedAt = Instant.now(), updatedBy = author, revision = UUID.randomUUID().toString(), pendingJobId = job._id),
            )
            dao.saveJob(job)
            jobDTO(job)
        }

    fun bulk(
        namespace: String,
        botId: String,
        request: KnowledgeBaseBulkStatus,
        author: String,
    ): KnowledgeBaseJobDTO =
        mutate(namespace, botId) {
            require(request.entryIds.isNotEmpty() && request.entryIds.size <= 1000) { "Invalid selection" }
            val entries = request.entryIds.distinct().map { entry(namespace, botId, it) } // Validate ownership before any mutation.
            val job =
                KnowledgeBaseJob(
                    namespace,
                    botId,
                    if (request.status ==
                        KnowledgeBaseEntryStatus.PUBLISHED
                    ) {
                        KnowledgeBaseJobType.PUBLISH
                    } else {
                        KnowledgeBaseJobType.UNPUBLISH
                    },
                    entries.map { it._id },
                )
            entries.forEach {
                dao.saveEntry(
                    it.copy(
                        status = request.status,
                        everPublished = it.everPublished || request.status == KnowledgeBaseEntryStatus.PUBLISHED,
                        updatedAt = Instant.now(),
                        updatedBy = author,
                        revision = UUID.randomUUID().toString(),
                        pendingJobId = job._id,
                    ),
                )
            }
            dao.saveJob(job)
            jobDTO(job)
        }

    fun enqueue(
        namespace: String,
        botId: String,
        type: KnowledgeBaseJobType,
        switchIndex: Boolean = false,
        requestedBy: String? = null,
    ): KnowledgeBaseJobDTO =
        mutate(namespace, botId) {
            val create = type == KnowledgeBaseJobType.CREATE_INDEX
            if (create) {
                // CREATE_INDEX is allowed whether or not an index already exists ("re-create"); only the blockers gate it.
                createIndexBlocker(namespace, botId)?.let { throw IllegalArgumentException(it.validationKey) }
            } else {
                require(target(namespace, botId) != null) { "knowledge-base.validation.index_configuration" }
            }
            if (create) dao.activeJobs(namespace, botId).firstOrNull { it.type == type }?.let { return@mutate jobDTO(it) }
            val rag = if (create) RAGService.getRAGConfiguration(namespace, botId) else null
            val job =
                KnowledgeBaseJob(
                    namespace,
                    botId,
                    type,
                    indexSessionId = if (create) UUID.randomUUID().toString() else null,
                    switchIndex = create && switchIndex,
                    expectedIndexSessionId = if (create) rag?.indexSessionId?.takeIf { it.isNotBlank() } else null,
                    requestedBy = if (create) requestedBy else null,
                )
            dao.saveJob(job)
            jobDTO(job)
        }

    fun job(
        namespace: String,
        botId: String,
        id: String,
    ): KnowledgeBaseJobDTO = dao.job(id)?.takeIf { it.namespace == namespace && it.botId == botId }?.let { jobDTO(it) } ?: throw NotFoundException(404, "Knowledge base job not found")

    fun activeJob(
        namespace: String,
        botId: String,
    ): KnowledgeBaseJobDTO? = dao.activeJobs(namespace, botId).firstOrNull()?.let { jobDTO(it) }

    fun jobDTO(job: KnowledgeBaseJob): KnowledgeBaseJobDTO =
        KnowledgeBaseJobDTO(
            job._id,
            job.type,
            job.state,
            job.startedAt,
            job.endedAt,
            job.progress,
            job.failures,
            job.projected,
            job.removed,
            job.indexSessionId,
            if (job.state in listOf(KnowledgeBaseJobState.COMPLETED, KnowledgeBaseJobState.FAILED)) sync(job.namespace, job.botId) else null,
            job.error,
        )

    /** Repairs the tiny save-entry/save-job crash window without requiring Mongo transactions or a broker. */
    fun recoverOutbox() {
        dao.pendingEntries().groupBy { it.namespace to it.botIds.single() }.forEach { (scope, _) ->
            mutate(scope.first, scope.second) {
                dao.entries(scope.first, scope.second).filter { it.pendingJobId != null }.groupBy { it.pendingJobId!! }.forEach { (jobId, entries) ->
                    if (dao.job(jobId) == null) dao.saveJob(KnowledgeBaseJob(scope.first, scope.second, KnowledgeBaseJobType.SAVE_ENTRY, entries.map { it._id }, _id = jobId))
                }
            }
        }
    }

    fun retrieval(
        namespace: String,
        botId: String,
        request: KnowledgeBaseRetrievalRequest,
    ): KnowledgeBaseRetrievalTest {
        require(request.question.isNotBlank() && request.question.length <= 4000) { "Invalid question" }
        request.entryId?.let { entry(namespace, botId, it) }
        val target = requireNotNull(target(namespace, botId)) { "knowledge-base.validation.index_configuration" }
        val condensed = checkNotNull(VectorStoreInspectionAdminService.condense(VectorStoreInspectionCondenseRequestDTO(request.question), namespace, botId))
        val openSearch = target.setting?.provider == VectorStoreProvider.OpenSearch || target.indexName.startsWith("ns-")
        val mode =
            if (openSearch ||
                (target.rag.documentSearchType == DocumentSearchType.HYBRID_SEARCH && condensed.keyWords.isEmpty())
            ) {
                DocumentSearchType.SIMILARITY_SEARCH
            } else {
                target.rag.documentSearchType
            }
        val k = target.rag.maxDocumentsRetrieved.coerceIn(1, 500)
        if (mode == DocumentSearchType.FULL_TEXT_SEARCH && condensed.keyWords.isEmpty()) return KnowledgeBaseRetrievalTest(request.question, target.session, k, null, emptyList())
        val response =
            checkNotNull(
                VectorStoreInspectionAdminService.search(
                    VectorStoreInspectionSearchRequestDTO(
                        target.indexName,
                        mode,
                        condensed.condensedQuestion,
                        condensed.keyWords,
                        k,
                        k,
                        false,
                        "afterCut",
                        pinnedChunkIds = request.entryId?.let { listOf("$it:1/1") }.orEmpty(),
                        pinnedRankStrategy = "truncated",
                    ),
                    namespace,
                    botId,
                ),
            )
        val hits =
            response.results.filter { it.outcome == "kept" }.mapIndexed { i, r ->
                val metadata = r.metadata.orEmpty()
                KnowledgeBaseRetrievalHit(
                    i + 1,
                    r.scores.rrf ?: r.scores.vector ?: r.scores.fts,
                    r.title,
                    metadata["source"] as? String,
                    if (metadata["source_type"] == "internal_kb") "internal_kb" else "document",
                    metadata["kb_entry_id"] as? String,
                    r.content,
                )
            }
        return KnowledgeBaseRetrievalTest(request.question, target.session, k, request.entryId?.let { id -> hits.firstOrNull { it.kbEntryId == id }?.rank }, hits)
    }
}

data class KnowledgeBaseTarget(
    val id: String,
    val namespace: String,
    val botId: String,
    val session: String,
    val indexName: String,
    val indexPrefix: String,
    val rag: BotRAGConfiguration,
    val setting: VectorStoreSetting?,
    // Normalized embedding model of the bot, or null when unknown (never blocking).
    val embeddingModel: String?,
)

/** Result of a single orchestrator probe of the collection backing a [KnowledgeBaseTarget]. */
data class KnowledgeBaseIndexProbe(
    val state: KnowledgeBaseIndexState,
    val kbRowCount: Int?,
    val otherRowCount: Int?,
    val collection: KnowledgeBaseCollectionInfo?,
    val embeddingIncompatible: Boolean,
)

/**
 * Coherence of a candidate RAG embedding setting against the collection its indexSessionId resolves to. Used by the RAG
 * settings save guard and its index-status endpoint. [collectionEmbeddingModel] is the model read from the collection's
 * contract metadata, null when the collection is missing or has none (PGVector only).
 */
data class KnowledgeBaseIndexStatus(
    val indexState: KnowledgeBaseIndexState,
    val collectionEmbeddingModel: String?,
    val coherence: EmbeddingCoherence,
)
