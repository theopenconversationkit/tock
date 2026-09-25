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

import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseDAO
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntry
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntryStatus
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseIndex
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJob
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobState
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobType
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseProjection
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseProjectionState
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseBulkStatus
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseDuplicatePolicy
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseEntryPayload
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseImportCandidate
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseImportRequest
import ai.tock.bot.engine.user.UserLock
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorclient.responses.KnowledgeBaseRowsResponse
import ai.tock.genai.orchestratorclient.responses.KnowledgeBaseStoredRow
import ai.tock.genai.orchestratorclient.responses.KnowledgeBaseWriteResponse
import ai.tock.genai.orchestratorclient.responses.KnowledgeBaseWriteResult
import ai.tock.genai.orchestratorclient.services.KnowledgeBaseIndexingService
import ai.tock.genai.orchestratorcore.models.em.OpenAIEMSetting
import ai.tock.genai.orchestratorcore.models.llm.OpenAILLMSetting
import ai.tock.shared.security.key.RawSecretKey
import ai.tock.shared.security.key.SecretKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KnowledgeBaseServiceTest {
    private val dao = MemoryKnowledgeBaseDAO()
    private val acquiredLocks = mutableListOf<String>()
    private var beforeWorkerLock: (() -> Unit)? = null
    private val lock =
        object : UserLock {
            override suspend fun tryLock(userId: String) = true

            override suspend fun releaseLock(userId: String) = Unit

            override suspend fun <T> withLock(
                userId: String,
                abortOnLockLoss: Boolean,
                postLockRelease: (() -> Unit)?,
                op: suspend () -> T,
            ): T {
                acquiredLocks.add(userId)
                if (userId == "knowledge-base-projection-worker") beforeWorkerLock?.invoke()
                return op()
            }
        }
    private val service = spyk(KnowledgeBaseService(dao, lock))
    private val indexing = mockk<KnowledgeBaseIndexingService>()
    private val ragDAO = mockk<BotRAGConfigurationDAO>()
    private val target: KnowledgeBaseTarget

    init {
        val llm = OpenAILLMSetting<SecretKey>(apiKey = RawSecretKey("test"), model = "test", temperature = "0", baseUrl = "https://example.org")
        val em = OpenAIEMSetting<SecretKey>(apiKey = RawSecretKey("test"), model = "test", baseUrl = "https://example.org")
        val rag = BotRAGConfiguration(newId(), "ns", "bot", true, llm, PromptTemplate(template = "test"), llm, PromptTemplate(template = "test"), em, indexSessionId = "session")
        target = KnowledgeBaseTarget("target", "ns", "bot", "session", "index", "prefix", rag, null, "model", "test")
        every { service.target(any(), any(), any()) } returns target
        every { indexing.rows(any()) } returns KnowledgeBaseRowsResponse(emptyList())
        every { indexing.index(any()) } answers {
            KnowledgeBaseWriteResponse(
                firstArg<ai.tock.genai.orchestratorclient.requests.KnowledgeBaseIndexRequest>().entries.map {
                    KnowledgeBaseWriteResult(
                        it.entryId,
                        listOf("kb-" + KnowledgeBaseService.hash("index/${it.entryId}")),
                        1,
                    )
                },
            )
        }
        every { indexing.delete(any()) } answers {
            KnowledgeBaseWriteResponse(firstArg<ai.tock.genai.orchestratorclient.requests.KnowledgeBaseDeleteRequest>().entries.map { KnowledgeBaseWriteResult(it.entryId, it.rowIds, it.rowIds.size) })
        }
    }

    private fun payload(status: KnowledgeBaseEntryStatus = KnowledgeBaseEntryStatus.PUBLISHED) = KnowledgeBaseEntryPayload("Titre", listOf("synonyme"), "Contenu", status = status)

    private fun create(status: KnowledgeBaseEntryStatus = KnowledgeBaseEntryStatus.PUBLISHED) = service.save("ns", "bot", payload(status), "author")

    private fun processor() = KnowledgeBaseJobProcessor(service, indexing, ragDAO)

    private fun projection(
        id: String,
        hash: String = "old",
    ) = KnowledgeBaseProjection("target/$id", "ns", "bot", "target", "session", id, hash, listOf("kb-" + KnowledgeBaseService.hash("index/$id")), "Titre")

    @Test
    fun `idle polling does not acquire any Mongo lock`() =
        runBlocking {
            repeat(2) { processor().processPending(lock) }
            assertTrue(acquiredLocks.isEmpty())
            verify(exactly = 0) { indexing.index(any()) }
            verify(exactly = 0) { indexing.delete(any()) }
        }

    @Test
    fun `queued and interrupted jobs are processed under the worker lock`() =
        runBlocking {
            val queued = create(KnowledgeBaseEntryStatus.DRAFT)
            val interrupted = create(KnowledgeBaseEntryStatus.DRAFT)
            dao.saveJob(dao.jobs.getValue(interrupted.job.id).copy(state = KnowledgeBaseJobState.RUNNING))
            acquiredLocks.clear()

            processor().processPending(lock)

            assertEquals(1, acquiredLocks.count { it == "knowledge-base-projection-worker" })
            assertEquals(KnowledgeBaseJobState.COMPLETED, dao.jobs.getValue(queued.job.id).state)
            assertEquals(KnowledgeBaseJobState.COMPLETED, dao.jobs.getValue(interrupted.job.id).state)
            assertTrue(dao.pendingEntries().isEmpty())
        }

    @Test
    fun `a missing outbox job still wakes the worker`() =
        runBlocking {
            dao.failJobWrite = true
            assertFails { create(KnowledgeBaseEntryStatus.DRAFT) }
            dao.failJobWrite = false
            acquiredLocks.clear()

            processor().processPending(lock)

            assertEquals(1, acquiredLocks.count { it == "knowledge-base-projection-worker" })
            assertEquals(
                KnowledgeBaseJobState.COMPLETED,
                dao.jobs.values
                    .single()
                    .state,
            )
            assertTrue(dao.pendingEntries().isEmpty())
        }

    @Test
    fun `reported projection failures do not wake an otherwise idle worker`() =
        runBlocking {
            val saved = create()
            every { indexing.index(any()) } throws IllegalStateException("unavailable")
            processor().process(dao.jobs.getValue(saved.job.id))
            acquiredLocks.clear()

            repeat(2) { processor().processPending(lock) }

            assertTrue(acquiredLocks.isEmpty())
            verify(exactly = 1) { indexing.index(any()) }
            assertEquals("knowledge-base.job.projection_failed", service.get("ns", "bot", saved.entry.id).projectionError)
        }

    @Test
    fun `work completed by another server before lock acquisition is not replayed`() =
        runBlocking {
            val saved = create()
            beforeWorkerLock = {
                dao.saveJob(dao.jobs.getValue(saved.job.id).copy(state = KnowledgeBaseJobState.COMPLETED))
                dao.acknowledge(dao.entries.getValue(saved.entry.id))
            }

            processor().processPending(lock)

            verify(exactly = 0) { indexing.index(any()) }
            verify(exactly = 0) { indexing.delete(any()) }
        }

    @Test
    fun `never published drafts can be saved without reaching the vector store`() =
        runBlocking {
            val saved = create(KnowledgeBaseEntryStatus.DRAFT)
            processor().process(dao.jobs.getValue(saved.job.id))
            verify(exactly = 0) { indexing.index(any()) }
            verify(exactly = 0) { indexing.delete(any()) }
            assertEquals(KnowledgeBaseJobState.COMPLETED, dao.jobs.getValue(saved.job.id).state)
        }

    @Test
    fun `repair failures remain attached to previously acknowledged entries`() =
        runBlocking {
            val saved = create()
            dao.acknowledge(dao.entries.getValue(saved.entry.id))
            val repair = KnowledgeBaseJob("ns", "bot", KnowledgeBaseJobType.REPAIR_INDEX)
            dao.saveJob(repair)
            every { indexing.index(any()) } throws IllegalStateException("unavailable")
            processor().process(repair)
            assertEquals(repair._id, dao.entries.getValue(saved.entry.id).pendingJobId)
            assertEquals("knowledge-base.job.projection_failed", service.get("ns", "bot", saved.entry.id).projectionError)
        }

    @Test
    fun `Studio nullable fields are explicit and internal outbox fields are absent`() {
        val result = create()
        val json =
            ai.tock.shared.jackson.mapper
                .valueToTree<com.fasterxml.jackson.databind.JsonNode>(result)
        assertTrue(json["entry"]["sourceUrl"].isNull)
        assertTrue(json["entry"]["projectedAt"].isNull)
        assertTrue(json["job"]["endedAt"].isNull)
        assertTrue(json["job"]["syncStatus"].isNull)
        assertTrue(!json["entry"].has("pendingJobId"))
        assertTrue(!json["entry"].has("revision"))
    }

    @Test
    fun `actual Retrofit converter sends the Python snake case contract`() {
        val request =
            ai.tock.genai.orchestratorclient.requests.KnowledgeBaseIndexRequest(
                null,
                "index",
                "prefix",
                target.rag.emSetting,
                "session",
                listOf(
                    ai.tock.genai.orchestratorclient.requests
                        .KnowledgeBaseDocument("entry", "Titre", emptyList(), "Contenu", null),
                ),
            )
        val converter =
            ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorClient
                .getClient()
                .requestBodyConverter<ai.tock.genai.orchestratorclient.requests.KnowledgeBaseIndexRequest>(request.javaClass, emptyArray(), emptyArray())
        val buffer = okio.Buffer()
        converter.convert(request)!!.writeTo(buffer)
        val json =
            ai.tock.shared.jackson.mapper
                .readTree(buffer.readUtf8())
        assertEquals("session", json["index_session_id"].asText())
        assertEquals("entry", json["entries"][0]["entry_id"].asText())
        assertEquals("OpenAI", json["em_setting"]["provider"].asText())
        assertTrue(!json.has("indexSessionId"))
    }

    @Test fun `draft and deleted entries remain orphaned until the vector is removed`() {
        val e = create().entry
        val persisted = dao.entries.getValue(e.id)
        assertEquals(KnowledgeBaseProjectionState.PENDING, KnowledgeBaseService.projectionState(persisted, null))
        assertEquals(KnowledgeBaseProjectionState.ORPHAN, KnowledgeBaseService.projectionState(persisted.copy(status = KnowledgeBaseEntryStatus.DRAFT), projection(e.id)))
        assertEquals(KnowledgeBaseProjectionState.ORPHAN, KnowledgeBaseService.projectionState(persisted.copy(deleted = true), projection(e.id)))
        assertEquals(KnowledgeBaseProjectionState.NONE, KnowledgeBaseService.projectionState(persisted.copy(deleted = true), null))
    }

    @Test fun `content hash changes for references and matches the canonical UTF8 document`() {
        assertNotEquals(KnowledgeBaseService.contentHash(payload()), KnowledgeBaseService.contentHash(payload().copy(sourceUrl = "https://example.org")))
        assertEquals(KnowledgeBaseService.hash("Titre\n\n```markdown\nsynonyme\n\nContenu\n```\n"), KnowledgeBaseService.contentHash(payload()))
        assertEquals("titre avec espaces", KnowledgeBaseService.normalizeTitle(" Titre  avec\n espaces "))
    }

    @Test fun `outbox recovers a crash between entry and job persistence`() {
        dao.failJobWrite = true
        assertFails { create() }
        assertEquals(1, dao.pendingEntries().size)
        assertTrue(dao.jobs.isEmpty())
        dao.failJobWrite = false
        service.recoverOutbox()
        assertEquals(
            dao.pendingEntries().single().pendingJobId,
            dao.jobs.values
                .single()
                ._id,
        )
    }

    @Test fun `import revalidates duplicates and unpublishes an existing publication`() {
        val existing = create().entry
        val candidate = KnowledgeBaseImportCandidate(payload(), "origin", "NEW", "foreign-entry", emptyList(), false)
        val result = KnowledgeBaseImportService(service).apply("ns", "bot", KnowledgeBaseImportRequest(listOf(candidate), KnowledgeBaseDuplicatePolicy.UPDATE), "importer")
        assertEquals(1, result.updated)
        assertEquals(listOf(existing.id), result.entryIds)
        assertEquals(KnowledgeBaseEntryStatus.DRAFT, dao.entries.getValue(existing.id).status)
        assertEquals(KnowledgeBaseJobType.UNPUBLISH, result.job?.type)
        assertNotNull(dao.entries.getValue(existing.id).pendingJobId)
    }

    @Test fun `duplicates inside an import are not accidentally created by skip policy`() {
        val candidate = KnowledgeBaseImportCandidate(payload(), null, "NEW", null, emptyList(), false)
        val result = KnowledgeBaseImportService(service).apply("ns", "bot", KnowledgeBaseImportRequest(listOf(candidate, candidate), KnowledgeBaseDuplicatePolicy.SKIP), "author")
        assertEquals(1, result.created)
        assertEquals(1, result.skipped)
    }

    @Test fun `bulk validates every selected entry before writing any status`() {
        val id = create(KnowledgeBaseEntryStatus.DRAFT).entry.id
        assertFails { service.bulk("ns", "bot", KnowledgeBaseBulkStatus(listOf(id, "foreign"), KnowledgeBaseEntryStatus.PUBLISHED), "author") }
        assertEquals(KnowledgeBaseEntryStatus.DRAFT, dao.entries.getValue(id).status)
    }

    @Test fun `old save task reads the latest unpublication instead of restoring published content`() =
        runBlocking {
            val saved = create()
            dao.saveProjection(projection(saved.entry.id))
            service.save("ns", "bot", payload(KnowledgeBaseEntryStatus.DRAFT), "author", saved.entry.id)
            processor().process(dao.jobs.getValue(saved.job.id))
            verify(exactly = 0) { indexing.index(any()) }
            verify(exactly = 1) { indexing.delete(any()) }
            assertTrue(dao.projections.isEmpty())
            assertEquals(KnowledgeBaseJobState.COMPLETED, dao.jobs.getValue(saved.job.id).state)
        }

    @Test fun `entry failures keep a persistent repair signal after completion`() =
        runBlocking {
            val saved = create()
            every { indexing.index(any()) } throws IllegalStateException("secret password")
            processor().process(dao.jobs.getValue(saved.job.id))
            val job = dao.jobs.getValue(saved.job.id)
            assertEquals(KnowledgeBaseJobState.COMPLETED, job.state)
            assertEquals(1, job.failures.size)
            assertEquals("knowledge-base.job.projection_failed", job.failures.single().error)
            assertNotNull(dao.entries.getValue(saved.entry.id).pendingJobId)
            assertEquals(1, service.sync("ns", "bot").counts.failed)
            assertEquals(KnowledgeBaseProjectionState.PENDING, service.get("ns", "bot", saved.entry.id).projectionState)
        }

    @Test fun `repair discovers and removes a deleted entry with no Mongo journal`() =
        runBlocking {
            every { indexing.rows(any()) } returns KnowledgeBaseRowsResponse(listOf(KnowledgeBaseStoredRow("orphan-row", "gone", "hash", "Gone")))
            val job = KnowledgeBaseJob("ns", "bot", KnowledgeBaseJobType.REPAIR_INDEX)
            dao.saveJob(job)
            processor().process(job)
            verify { indexing.delete(match { it.entries.single().rowIds == listOf("orphan-row") }) }
            assertTrue(dao.projections.isEmpty())
            assertEquals(1, dao.jobs.getValue(job._id).removed)
        }

    @Test fun `a new session ignores projections in the old index`() {
        val e = create().entry
        dao.saveProjection(projection(e.id, e.contentHash).copy(targetId = "old-target", indexSessionId = "old-session"))
        val counts = service.sync("ns", "bot").counts
        assertEquals(1, counts.pending)
        assertEquals(0, counts.indexed)
        assertEquals(0, counts.orphan)
    }

    @Test fun `resuming a job does not create another row for an acknowledged projection`() =
        runBlocking {
            val saved = create()
            dao.saveProjection(projection(saved.entry.id, saved.entry.contentHash))
            processor().process(dao.jobs.getValue(saved.job.id).copy(state = KnowledgeBaseJobState.RUNNING))
            verify(exactly = 0) { indexing.index(any()) }
            assertEquals(KnowledgeBaseJobState.COMPLETED, dao.jobs.getValue(saved.job.id).state)
        }

    @Test fun `embedding model mismatch blocks writes even when dimensions could match`() =
        runBlocking {
            val saved = create()
            dao.saveIndex(KnowledgeBaseIndex("target", "ns", "bot", "session", "another-model", "another-model", true))
            processor().process(dao.jobs.getValue(saved.job.id))
            verify(exactly = 0) { indexing.index(any()) }
            assertEquals(
                "knowledge-base.job.embedding_changed",
                dao.jobs
                    .getValue(saved.job.id)
                    .failures
                    .single()
                    .error,
            )
            assertTrue(service.sync("ns", "bot").embeddingMismatch)
        }
}

private class MemoryKnowledgeBaseDAO : KnowledgeBaseDAO {
    val entries = linkedMapOf<String, KnowledgeBaseEntry>()
    val projections = linkedMapOf<String, KnowledgeBaseProjection>()
    val jobs = linkedMapOf<String, KnowledgeBaseJob>()
    val indexes = linkedMapOf<String, KnowledgeBaseIndex>()
    var failJobWrite = false

    override fun deleteBot(
        namespace: String,
        botId: String,
    ) {
        entries.values.removeIf { it.namespace == namespace && botId in it.botIds }
        projections.values.removeIf { it.namespace == namespace && it.botId == botId }
        jobs.values.removeIf { it.namespace == namespace && it.botId == botId }
        indexes.values.removeIf { it.namespace == namespace && it.botId == botId }
    }

    override fun entries(
        namespace: String,
        botId: String,
    ) = entries.values.filter { it.namespace == namespace && botId in it.botIds }

    override fun saveEntry(entry: KnowledgeBaseEntry) {
        entries[entry._id] = entry
    }

    override fun pendingEntries() = entries.values.filter { it.pendingJobId != null }

    override fun acknowledge(entry: KnowledgeBaseEntry) {
        if (entries[entry._id]?.revision == entry.revision) entries[entry._id] = entry.copy(pendingJobId = null)
    }

    override fun markProjectionPending(
        entry: KnowledgeBaseEntry,
        jobId: String,
    ) {
        if (entries[entry._id]?.revision == entry.revision) entries[entry._id] = entry.copy(pendingJobId = jobId)
    }

    override fun projections(
        namespace: String,
        botId: String,
        targetId: String,
    ) = projections.values.filter { it.namespace == namespace && it.botId == botId && it.targetId == targetId }

    override fun saveProjection(projection: KnowledgeBaseProjection) {
        projections[projection._id] = projection
    }

    override fun deleteProjection(id: String) {
        projections.remove(id)
    }

    override fun index(id: String) = indexes[id]

    override fun saveIndex(index: KnowledgeBaseIndex) {
        indexes[index._id] = index
    }

    override fun saveJob(job: KnowledgeBaseJob) {
        check(!failJobWrite)
        jobs[job._id] = job
    }

    override fun job(id: String) = jobs[id]

    override fun activeJobs(
        namespace: String,
        botId: String,
    ) = jobs.values.filter {
        it.namespace == namespace && it.botId == botId &&
            it.state in listOf(KnowledgeBaseJobState.QUEUED, KnowledgeBaseJobState.RUNNING)
    }

    override fun nextJob() = jobs.values.firstOrNull { it.state == KnowledgeBaseJobState.RUNNING } ?: jobs.values.firstOrNull { it.state == KnowledgeBaseJobState.QUEUED }
}
