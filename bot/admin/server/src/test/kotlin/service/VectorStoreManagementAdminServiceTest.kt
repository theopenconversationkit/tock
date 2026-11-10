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
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseDAO
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntryStatus
import ai.tock.bot.admin.model.genai.VectorStoreDeleteChunkRequestDTO
import ai.tock.bot.admin.model.genai.VectorStoreEmbeddingModelRequestDTO
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseJobDTO
import ai.tock.genai.orchestratorclient.responses.VectorStoreCapabilitiesResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreChunkDescriptionResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreIndexMetadataResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreWriteResponse
import ai.tock.genai.orchestratorclient.services.VectorStoreInspectionService
import ai.tock.genai.orchestratorcore.models.em.OpenAIEMSetting
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreProvider
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.security.key.RawSecretKey
import ai.tock.shared.security.key.SecretKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VectorStoreManagementAdminServiceTest {
    private val inspection = mockk<VectorStoreInspectionService>()
    private val knowledgeBase = mockk<KnowledgeBaseService>()
    private val dao = mockk<KnowledgeBaseDAO>()
    private val rag = mockk<BotRAGConfiguration>()
    private val service = VectorStoreManagementAdminService(inspection, knowledgeBase)
    private val current = "ns_ns_bot_bot_session_current"
    private val old = "ns_ns_bot_bot_session_old"

    @BeforeEach
    fun setup() {
        mockkObject(RAGService, VectorStoreService, VectorStoreIndexLifecycle)
        every { VectorStoreIndexLifecycle.withLock<Any?>(any(), any(), any()) } answers { thirdArg<() -> Any?>().invoke() }
        every { VectorStoreIndexLifecycle.withProjectionLock<Any?>(any(), any(), any()) } answers { thirdArg<() -> Any?>().invoke() }
        every { RAGService.getRAGConfiguration("ns", "bot") } returns rag
        every { rag.indexSessionId } returns "current"
        every { rag.enabled } returns false
        every { rag.emSetting } returns OpenAIEMSetting<SecretKey>(apiKey = RawSecretKey("test"), model = "bot-model", baseUrl = "https://example.org")
        every { VectorStoreService.getVectorStoreConfiguration("ns", "bot", true) } returns null
        every { inspection.getCapabilities(any()) } returns VectorStoreCapabilitiesResponse(VectorStoreProvider.PGVector, emptyList(), true, true, true)
        every { knowledgeBase.dao } returns dao
        every { knowledgeBase.invalidateProjections(any(), any(), any(), any(), any()) } returns Unit
        every { dao.activeJobs("ns", "bot") } returns emptyList()
        every { inspection.getChunk(any()) } returns VectorStoreChunkDescriptionResponse(mapOf("source_type" to "document"), 2)
        every { inspection.deleteChunk(any()) } returns VectorStoreWriteResponse(1)
        every { inspection.deleteIndex(any()) } returns VectorStoreWriteResponse(3)
    }

    @AfterEach
    fun cleanup() {
        unmockkObject(RAGService, VectorStoreService, VectorStoreIndexLifecycle)
    }

    @Test
    fun `configured index is protected even when RAG is disabled and it has no chunks`() {
        assertFailsWith<RestException> { service.deleteIndex("ns", "bot", current) }
        verify(exactly = 0) { inspection.deleteIndex(any()) }
    }

    @Test
    fun `old index deletion passes the authoritative protected index`() {
        assertEquals(3, service.deleteIndex("ns", "bot", old).deletedCount)
        verify { inspection.deleteIndex(match { it.indexName == old && it.protectedIndexName == current }) }
        verify(exactly = 0) { knowledgeBase.bulk(any(), any(), any(), any()) }
    }

    @Test
    fun `foreign bot index is rejected before any remote write`() {
        assertFailsWith<RestException> { service.deleteIndex("ns", "bot", "ns_other_bot_bot_session_old") }
        verify(exactly = 0) { inspection.deleteIndex(any()) }
    }

    @Test
    fun `bot names containing the session separator cannot escape their scope`() {
        val foreign = "ns_ns_bot_bot_session_2_session_old"
        assertFailsWith<RestException> { service.metadata("ns", "bot", foreign) }
        assertFailsWith<RestException> { service.setEmbeddingModel("ns", "bot", VectorStoreEmbeddingModelRequestDTO(foreign, "model")) }
        assertFailsWith<RestException> { service.deleteChunk("ns", "bot", VectorStoreDeleteChunkRequestDTO(foreign, "row", false), "user") }
        assertFailsWith<RestException> { service.deleteIndex("ns", "bot", foreign) }
        verify(exactly = 0) { inspection.getCapabilities(any()) }
        verify(exactly = 0) { knowledgeBase.invalidateProjections(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `direct deletions invalidate the matching journal before deleting vectors`() {
        service.deleteChunk("ns", "bot", VectorStoreDeleteChunkRequestDTO(old, "row", false), "user")
        verifyOrder {
            VectorStoreIndexLifecycle.withProjectionLock<Any?>("ns", "bot", any())
            knowledgeBase.invalidateProjections("ns", "bot", old, null, "row")
            inspection.deleteChunk(any())
        }
        service.deleteIndex("ns", "bot", old)
        verifyOrder {
            knowledgeBase.invalidateProjections("ns", "bot", old, null, null)
            inspection.deleteIndex(any())
        }
    }

    @Test
    fun `a failed Mongo invalidation prevents SQL deletion`() {
        every { knowledgeBase.invalidateProjections(any(), any(), any(), any(), any()) } throws IllegalStateException("Mongo unavailable")
        assertFailsWith<IllegalStateException> { service.deleteChunk("ns", "bot", VectorStoreDeleteChunkRequestDTO(old, "row", false), "user") }
        assertFailsWith<IllegalStateException> { service.deleteIndex("ns", "bot", old) }
        verify(exactly = 0) { inspection.deleteChunk(any()) }
        verify(exactly = 0) { inspection.deleteIndex(any()) }
    }

    @Test
    fun `current KB chunk unpublishes the source and returns its synchronization job`() {
        every { inspection.getChunk(any()) } returns VectorStoreChunkDescriptionResponse(mapOf("source_type" to "internal_kb", "kb_entry_id" to "entry"), 1)
        val job = mockk<KnowledgeBaseJobDTO>()
        every { job.id } returns "job"
        every { knowledgeBase.bulk("ns", "bot", any(), "user") } returns job
        val result = service.deleteChunk("ns", "bot", VectorStoreDeleteChunkRequestDTO(current, "row", true, true, true), "user")
        assertEquals("job", result.jobId)
        verify { knowledgeBase.bulk("ns", "bot", match { it.entryIds == listOf("entry") && it.status == KnowledgeBaseEntryStatus.DRAFT }, "user") }
        verify(exactly = 0) { inspection.deleteChunk(any()) }
        verify(exactly = 0) { VectorStoreIndexLifecycle.withProjectionLock<Any?>(any(), any(), any()) }
        verify(exactly = 0) { knowledgeBase.invalidateProjections(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `old KB chunk is deleted without unpublishing the source`() {
        every { inspection.getChunk(any()) } returns VectorStoreChunkDescriptionResponse(mapOf("source_type" to "internal_kb", "kb_entry_id" to "entry"), 2)
        service.deleteChunk("ns", "bot", VectorStoreDeleteChunkRequestDTO(old, "row", false), "user")
        verify { inspection.deleteChunk(match { it.rowId == "row" && !it.protectKnowledgeBase }) }
        verify(exactly = 0) { knowledgeBase.bulk(any(), any(), any(), any()) }
    }

    @Test
    fun `a stale confirmation cannot unpublish a newly current index`() {
        assertFailsWith<RestException> { service.deleteChunk("ns", "bot", VectorStoreDeleteChunkRequestDTO(current, "row", false), "user") }
        verify(exactly = 0) { inspection.deleteChunk(any()) }
        verify(exactly = 0) { knowledgeBase.bulk(any(), any(), any(), any()) }
    }

    @Test
    fun `KB unpublication must have been explicitly confirmed`() {
        every { inspection.getChunk(any()) } returns VectorStoreChunkDescriptionResponse(mapOf("source_type" to "internal_kb", "kb_entry_id" to "entry"), 2)
        assertFailsWith<RestException> { service.deleteChunk("ns", "bot", VectorStoreDeleteChunkRequestDTO(current, "row", true), "user") }
        verify(exactly = 0) { knowledgeBase.bulk(any(), any(), any(), any()) }
    }

    @Test
    fun `last chunk requires the empty index confirmation`() {
        every { inspection.getChunk(any()) } returns VectorStoreChunkDescriptionResponse(emptyMap(), 1)
        assertFailsWith<RestException> { service.deleteChunk("ns", "bot", VectorStoreDeleteChunkRequestDTO(current, "row", true), "user") }
        service.deleteChunk("ns", "bot", VectorStoreDeleteChunkRequestDTO(current, "row", true, true), "user")
        verify(exactly = 1) { inspection.deleteChunk(any()) }
    }

    @Test
    fun `historical model differing from current bot is saved with a warning`() {
        every { inspection.setEmbeddingModel(any()) } returns VectorStoreIndexMetadataResponse(mapOf("embedding_model" to "historical-model", "custom" to 42))
        val result = service.setEmbeddingModel("ns", "bot", VectorStoreEmbeddingModelRequestDTO(current, " historical-model "))
        assertTrue(result.embeddingIncompatible)
        assertEquals("bot-model", result.currentEmbeddingModel)
        assertEquals(42, result.metadata["custom"])
        verify { inspection.setEmbeddingModel(match { it.embeddingModel == "historical-model" }) }
        verify(exactly = 0) { RAGService.saveRag(any(), any()) }
    }

    @Test
    fun `manual correction forwards the exact expected value and preserves provenance`() {
        every { inspection.setEmbeddingModel(any()) } returns
            VectorStoreIndexMetadataResponse(
                mapOf("embedding_model" to "bot-model", "embedding_model_source" to "manual"),
            )
        val result = service.setEmbeddingModel("ns", "bot", VectorStoreEmbeddingModelRequestDTO(current, " bot-model ", " old-model "))
        assertEquals("manual", result.metadata["embedding_model_source"])
        assertFalse(result.embeddingIncompatible)
        verify {
            inspection.setEmbeddingModel(match { it.embeddingModel == "bot-model" && it.expectedEmbeddingModel == " old-model " })
        }
        verify(exactly = 0) { RAGService.saveRag(any(), any()) }
    }

    @Test
    fun `concurrent model update is surfaced as a conflict`() {
        every { inspection.setEmbeddingModel(any()) } returns VectorStoreIndexMetadataResponse(error = "model_changed")
        val error = assertFailsWith<RestException> { service.setEmbeddingModel("ns", "bot", VectorStoreEmbeddingModelRequestDTO(current, "model")) }
        assertEquals(409, error.httpResponseStatus.code())
    }
}
