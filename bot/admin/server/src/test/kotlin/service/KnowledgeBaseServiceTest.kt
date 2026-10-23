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
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJob
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobState
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobType
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseProjection
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseProjectionState
import ai.tock.bot.admin.model.knowledgebase.CreateIndexBlocker
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseBulkStatus
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseDuplicatePolicy
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseEntryPayload
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseImportCandidate
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseImportRequest
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseIndexState
import ai.tock.bot.engine.user.UserLock
import ai.tock.genai.orchestratorclient.requests.KnowledgeBaseIndexRequest
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorclient.responses.KnowledgeBaseIndexStateResponse
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
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
    private val indexing = mockk<KnowledgeBaseIndexingService>()
    private val service = spyk(KnowledgeBaseService(dao, lock, indexing))
    private val ragDAO = mockk<BotRAGConfigurationDAO>()
    private val target: KnowledgeBaseTarget

    init {
        val llm = OpenAILLMSetting<SecretKey>(apiKey = RawSecretKey("test"), model = "test", temperature = "0", baseUrl = "https://example.org")
        val em = OpenAIEMSetting<SecretKey>(apiKey = RawSecretKey("test"), model = "test", baseUrl = "https://example.org")
        val rag = BotRAGConfiguration(newId(), "ns", "bot", true, llm, PromptTemplate(template = "test"), llm, PromptTemplate(template = "test"), em, indexSessionId = "session")
        target = KnowledgeBaseTarget("target", "ns", "bot", "session", "index", "prefix", rag, null, "model")
        every { service.target(any(), any(), any()) } returns target
        // sync()/createIndexBlocker resolve the bot's RAG configuration through the RAGService object.
        mockkObject(RAGService)
        every { RAGService.getRAGConfiguration(any(), any()) } returns rag
        // Healthy default probe: collection exists, certified, and its embedding model matches the bot's ("model").
        every { indexing.indexState(any()) } returns
            KnowledgeBaseIndexStateResponse(true, 1, 1, mapOf("schema_version" to 1, "embedding_model" to "model"))
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
            // The collection was created with a different embedding model than the bot now uses.
            every { indexing.indexState(any()) } returns
                KnowledgeBaseIndexStateResponse(true, 1, 1, mapOf("schema_version" to 1, "embedding_model" to "another-model"))
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
            assertTrue(service.sync("ns", "bot").embeddingIncompatible)
        }

    private fun createIndexJob(
        session: String,
        switchIndex: Boolean,
        expected: String? = "session",
    ) = KnowledgeBaseJob(
        "ns",
        "bot",
        KnowledgeBaseJobType.CREATE_INDEX,
        indexSessionId = session,
        switchIndex = switchIndex,
        expectedIndexSessionId = expected,
        requestedBy = "author",
    )

    @Test fun `a switching creation points the bot at the new index and never toggles enabled`() =
        runBlocking {
            val switched = mutableListOf<BotRAGConfiguration>()
            mockkObject(RAGService)
            every { RAGService.switchKnowledgeBaseIndex(any(), capture(switched)) } returns Unit
            // The bot's `enabled` flag must be preserved whether RAG was on or off before the switch.
            for (enabled in listOf(true, false)) {
                val session = "new-$enabled"
                create()
                every { service.target("ns", "bot", session) } returns target.copy(session = session)
                every { ragDAO.findByNamespaceAndBotId("ns", "bot") } returns target.rag.copy(enabled = enabled, indexSessionId = "session")
                val job = createIndexJob(session, switchIndex = true)
                dao.saveJob(job)
                processor().process(job)
                assertEquals(KnowledgeBaseJobState.COMPLETED, dao.jobs.getValue(job._id).state)
            }
            assertEquals(listOf("new-true", "new-false"), switched.map { it.indexSessionId })
            assertEquals(listOf(true, false), switched.map { it.enabled })
        }

    @Test fun `a non-switching creation leaves the settings and pending entries untouched for the current index`() =
        runBlocking {
            val saved = create()
            val session = "shadow"
            // A distinct target id, so the shadow index projections do not masquerade as the current index's.
            every { service.target("ns", "bot", session) } returns target.copy(id = "shadow-target", session = session)
            mockkObject(RAGService)
            every { RAGService.switchKnowledgeBaseIndex(any(), any()) } returns Unit

            processor().process(createIndexJob(session, switchIndex = false).also { dao.saveJob(it) })

            // Settings never touched, and the pending change was NOT acknowledged by the shadow creation.
            verify(exactly = 0) { RAGService.switchKnowledgeBaseIndex(any(), any()) }
            assertEquals(saved.job.id, dao.entries.getValue(saved.entry.id).pendingJobId)

            // Its own SAVE job still projects it into the current index and acknowledges it.
            processor().process(dao.jobs.getValue(saved.job.id))
            assertNull(dao.entries.getValue(saved.entry.id).pendingJobId)
            assertTrue(dao.projections.values.any { it.targetId == target.id && it.entryId == saved.entry.id })
        }

    @Test fun `a failing non-switching creation records the failure on the job and leaves the current index entries and projections untouched`() =
        runBlocking {
            val saved = create()
            // First, project the entry into the CURRENT index and acknowledge it.
            processor().process(dao.jobs.getValue(saved.job.id))
            assertNull(dao.entries.getValue(saved.entry.id).pendingJobId)
            val currentProjection = dao.projections.values.single { it.targetId == target.id && it.entryId == saved.entry.id }

            val session = "shadow"
            every { service.target("ns", "bot", session) } returns target.copy(id = "shadow-target", session = session)
            mockkObject(RAGService)
            every { RAGService.switchKnowledgeBaseIndex(any(), any()) } returns Unit
            // The write into the (re-)created collection fails. This concerns the new collection, not the current index.
            every { indexing.index(any()) } throws IllegalStateException("knowledge-base.job.index_failed")

            val job = createIndexJob(session, switchIndex = false).also { dao.saveJob(it) }
            processor().process(job)

            // The failure is recorded on the job only.
            assertTrue(
                dao.jobs
                    .getValue(job._id)
                    .failures
                    .isNotEmpty(),
            )
            // The current index entry is NOT marked pending, and its live projection is left exactly as it was.
            assertNull(dao.entries.getValue(saved.entry.id).pendingJobId)
            assertEquals(currentProjection, dao.projections.values.single { it.targetId == target.id && it.entryId == saved.entry.id })
            verify(exactly = 0) { RAGService.switchKnowledgeBaseIndex(any(), any()) }
        }

    @Test fun `a collection embedding model matches the bot ignoring an Ollama latest tag`() {
        val ollamaTarget = target.copy(embeddingModel = "nomic-embed-text")
        every { indexing.indexState(any()) } returns
            KnowledgeBaseIndexStateResponse(true, 1, 1, mapOf("schema_version" to 1, "embedding_model" to "nomic-embed-text:latest"))
        assertFalse(service.probe(ollamaTarget).embeddingIncompatible)
    }

    @Test fun `a creation aborts when the settings index session changed after enqueue and succeeds once it matches`() =
        runBlocking {
            create()
            val session = "job-session"
            val job = createIndexJob(session, switchIndex = false)
            dao.saveJob(job)

            // The bot's RAG indexSessionId moved to an unrelated session after enqueue.
            every { service.target("ns", "bot", session) } returns target.copy(session = session, rag = target.rag.copy(indexSessionId = "other-session"))
            processor().process(job)
            assertEquals(KnowledgeBaseJobState.FAILED, dao.jobs.getValue(job._id).state)
            assertEquals("knowledge-base.job.configuration_changed", dao.jobs.getValue(job._id).error)
            verify(exactly = 0) { indexing.index(any()) }

            // Retry once this job's own switch has pointed the settings at its session.
            every { service.target("ns", "bot", session) } returns target.copy(session = session, rag = target.rag.copy(indexSessionId = session))
            processor().process(job)
            assertEquals(KnowledgeBaseJobState.COMPLETED, dao.jobs.getValue(job._id).state)
        }

    @Test fun `a creation writes the Tock contract metadata into the new collection`() {
        runBlocking {
            create()
            val session = "brand-new"
            every { service.target("ns", "bot", session) } returns target.copy(session = session)
            val request = slot<KnowledgeBaseIndexRequest>()
            every { indexing.index(capture(request)) } answers {
                KnowledgeBaseWriteResponse(
                    firstArg<KnowledgeBaseIndexRequest>().entries.map {
                        KnowledgeBaseWriteResult(it.entryId, listOf("kb-" + KnowledgeBaseService.hash("index/${it.entryId}")), 1)
                    },
                )
            }

            processor().process(createIndexJob(session, switchIndex = false).also { dao.saveJob(it) })

            val metadata = request.captured.collectionMetadata!!
            assertEquals(1, metadata["schema_version"])
            assertEquals("tock_kb", metadata["origin"])
            assertEquals("author", metadata["created_by"])
            assertEquals("OpenAI", metadata["embedding_provider"])
            assertEquals("model", metadata["embedding_model"])
            assertNotNull(metadata["created_at"])
        }
    }

    @Test fun `a missing index fails every write delete verify and repair job and keeps entries pending`() =
        runBlocking {
            every { indexing.indexState(any()) } returns KnowledgeBaseIndexStateResponse(false, null, null, null)
            val saved = create()
            for (job in listOf(
                dao.jobs.getValue(saved.job.id),
                KnowledgeBaseJob("ns", "bot", KnowledgeBaseJobType.DELETE_ENTRY, listOf(saved.entry.id)),
                KnowledgeBaseJob("ns", "bot", KnowledgeBaseJobType.VERIFY_INDEX),
                KnowledgeBaseJob("ns", "bot", KnowledgeBaseJobType.REPAIR_INDEX),
            )) {
                dao.saveJob(job)
                processor().process(job)
                assertEquals(KnowledgeBaseJobState.FAILED, dao.jobs.getValue(job._id).state)
                assertEquals("knowledge-base.job.index_missing", dao.jobs.getValue(job._id).error)
            }
            assertNotNull(dao.entries.getValue(saved.entry.id).pendingJobId)
            verify(exactly = 0) { indexing.index(any()) }
            verify(exactly = 0) { indexing.delete(any()) }
            verify(exactly = 0) { indexing.rows(any()) }
        }

    @Test fun `createIndexBlocker reports each cause in priority order and enqueue surfaces its key`() {
        // RAG_NOT_CONFIGURED wins even with no published entry.
        every { RAGService.getRAGConfiguration(any(), any()) } returns null
        assertEquals(CreateIndexBlocker.RAG_NOT_CONFIGURED, service.createIndexBlocker("ns", "bot"))
        val ex = assertFailsWith<IllegalArgumentException> { service.enqueue("ns", "bot", KnowledgeBaseJobType.CREATE_INDEX) }
        assertEquals(CreateIndexBlocker.RAG_NOT_CONFIGURED.validationKey, ex.message)

        // EMBEDDING_MODEL_UNDEFINED wins over a missing published entry.
        val noModelRag = target.rag.copy(emSetting = (target.rag.emSetting as OpenAIEMSetting).copy(model = " "))
        every { RAGService.getRAGConfiguration(any(), any()) } returns noModelRag
        assertEquals(CreateIndexBlocker.EMBEDDING_MODEL_UNDEFINED, service.createIndexBlocker("ns", "bot"))

        // NO_PUBLISHED_ENTRY when the RAG and embedding model are fine but nothing is published yet.
        every { RAGService.getRAGConfiguration(any(), any()) } returns target.rag
        assertEquals(CreateIndexBlocker.NO_PUBLISHED_ENTRY, service.createIndexBlocker("ns", "bot"))

        // No blocker once a published entry exists.
        create()
        assertNull(service.createIndexBlocker("ns", "bot"))
    }
}

private class MemoryKnowledgeBaseDAO : KnowledgeBaseDAO {
    val entries = linkedMapOf<String, KnowledgeBaseEntry>()
    val projections = linkedMapOf<String, KnowledgeBaseProjection>()
    val jobs = linkedMapOf<String, KnowledgeBaseJob>()
    var failJobWrite = false

    override fun deleteBot(
        namespace: String,
        botId: String,
    ) {
        entries.values.removeIf { it.namespace == namespace && botId in it.botIds }
        projections.values.removeIf { it.namespace == namespace && it.botId == botId }
        jobs.values.removeIf { it.namespace == namespace && it.botId == botId }
    }

    override fun entries(
        namespace: String,
        botId: String,
    ) = entries.values.filter { it.namespace == namespace && botId in it.botIds }

    override fun entry(
        namespace: String,
        botId: String,
        id: String,
    ) = entries[id]?.takeIf { it.namespace == namespace && botId in it.botIds }

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
