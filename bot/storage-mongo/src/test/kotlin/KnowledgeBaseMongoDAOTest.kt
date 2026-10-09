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

import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntry
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntryStatus
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJob
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobState
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobType
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseProjection
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorcore.models.em.OpenAIEMSetting
import ai.tock.genai.orchestratorcore.models.llm.OpenAILLMSetting
import ai.tock.shared.security.key.RawSecretKey
import ai.tock.shared.security.key.SecretKey
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KnowledgeBaseMongoDAOTest : AbstractTest() {
    @Test fun `outbox acknowledgements preserve newer revisions and purge is scoped`() {
        val ns = "kb_test_" + UUID.randomUUID()
        val dao = KnowledgeBaseMongoDAO
        try {
            val entry =
                KnowledgeBaseEntry(
                    ns,
                    listOf("bot"),
                    "Titre",
                    emptyList(),
                    "Contenu",
                    null,
                    emptyList(),
                    KnowledgeBaseEntryStatus.PUBLISHED,
                    "hash",
                    "author",
                    pendingJobId = "job",
                    createdAt = Instant.ofEpochMilli(1700000000000),
                )
            dao.saveEntry(entry)
            assertEquals(entry, dao.entries(ns, "bot").single())
            val next = entry.copy(revision = "next", pendingJobId = "next-job")
            dao.saveEntry(next)
            dao.acknowledge(entry)
            assertEquals("next-job", dao.entries(ns, "bot").single().pendingJobId)
            dao.acknowledge(next)
            assertNull(dao.entries(ns, "bot").single().pendingJobId)
            val projection = KnowledgeBaseProjection("$ns/one/${entry._id}", ns, "bot", "one", "session", entry._id, "hash", listOf("row"), "Titre")
            dao.saveProjection(projection)
            dao.saveProjection(projection.copy(contentHash = "new"))
            dao.saveProjection(projection.copy(_id = "$ns/two/${entry._id}", targetId = "two"))
            assertEquals("new", dao.projections(ns, "bot", "one").single().contentHash)
            assertEquals(1, dao.projections(ns, "bot", "two").size)
            val job = KnowledgeBaseJob(ns, "bot", KnowledgeBaseJobType.SAVE_ENTRY, listOf(entry._id), startedAt = Instant.ofEpochMilli(1700000000000))
            dao.saveJob(job)
            assertEquals(job, dao.activeJobs(ns, "bot").single())
            dao.saveJob(job.copy(state = KnowledgeBaseJobState.COMPLETED))
            assertTrue(dao.activeJobs(ns, "bot").isEmpty())
            val other = entry.copy(_id = UUID.randomUUID().toString(), botIds = listOf("other"))
            dao.saveEntry(other)
            dao.deleteBot(ns, "bot")
            assertTrue(dao.entries(ns, "bot").isEmpty())
            assertTrue(dao.projections(ns, "bot", "one").isEmpty())
            assertNull(dao.job(job._id))
            assertEquals(other, dao.entries(ns, "other").single())
        } finally {
            dao.deleteBot(ns, "bot")
            dao.deleteBot(ns, "other")
        }
    }

    @Test fun `index activation refuses to overwrite a concurrent RAG edit`() {
        val ns = "kb_test_" + UUID.randomUUID()
        val llm = OpenAILLMSetting<SecretKey>(apiKey = RawSecretKey("test"), model = "test", temperature = "0", baseUrl = "https://example.org")
        val em = OpenAIEMSetting<SecretKey>(apiKey = RawSecretKey("test"), model = "test", baseUrl = "https://example.org")
        val config = BotRAGConfiguration(newId(), ns, "bot", false, llm, PromptTemplate(template = "test"), llm, PromptTemplate(template = "test"), em)
        val dao = BotRAGConfigurationMongoDAO
        try {
            dao.save(config)
            val active = config.copy(enabled = true, indexSessionId = "new-session")
            assertTrue(dao.saveIfUnchanged(config, active))
            assertEquals(active, dao.findByNamespaceAndBotId(ns, "bot"))
            assertFalse(dao.saveIfUnchanged(config, config.copy(indexSessionId = "obsolete")))
            assertEquals(active, dao.findByNamespaceAndBotId(ns, "bot"))
        } finally {
            dao.delete(config._id)
        }
    }
}
