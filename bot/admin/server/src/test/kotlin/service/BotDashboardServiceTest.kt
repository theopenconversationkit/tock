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

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfigurationDAO
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfigurationDAO
import ai.tock.bot.admin.bot.observability.BotObservabilityConfigurationDAO
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.bot.sentencegeneration.BotSentenceGenerationConfigurationDAO
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfiguration
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfigurationDAO
import ai.tock.bot.admin.dashboard.BotContact
import ai.tock.bot.admin.dashboard.BotDashboardDAO
import ai.tock.bot.admin.dashboard.BotHistoryEvent
import ai.tock.bot.admin.dashboard.BotHistorySnapshot
import ai.tock.bot.admin.dataset.DatasetDAO
import ai.tock.bot.admin.evaluation.EvaluationSampleDAO
import ai.tock.bot.admin.indicators.IndicatorDAO
import ai.tock.bot.admin.indicators.metric.MetricDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.genai.orchestratorcore.models.vectorstore.PGVectorStoreSetting
import ai.tock.genai.orchestratorcore.utils.SecurityUtils
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.security.key.RawSecretKey
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.I18nDAO
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BotDashboardServiceTest {
    private val originalInjector = tockInternalInjector
    private val dao = mockk<BotDashboardDAO>(relaxed = true)

    @BeforeEach
    fun setup() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(Kodein { bind<BotDashboardDAO>() with singleton { dao } })
        every { dao.latest(any(), any(), any()) } returns null
    }

    @AfterEach
    fun cleanup() {
        tockInternalInjector = originalInjector
    }

    @Test
    fun `snapshots recursively remove all credential types including nested lists`() {
        val source =
            mapOf(
                "apiKey" to "llm",
                "setting" to mapOf("password" to "db", "host" to "localhost"),
                "nested" to listOf(mapOf("secretKey" to "langfuse", "template" to "keep me")),
            )
        val clean = BotHistoryService.snapshot(source)!!
        assertEquals(mapOf("setting" to mapOf("host" to "localhost"), "nested" to listOf(mapOf("template" to "keep me"))), clean)
        val entity =
            BotVectorStoreConfiguration(
                newId(),
                "ns",
                "bot",
                true,
                PGVectorStoreSetting("localhost", 5432, "user", RawSecretKey("never-store"), "db"),
            )
        val actual = BotHistoryService.snapshot(entity)!!
        assertFalse(actual.toString().contains("never-store"))
        assertFalse(actual.toString().contains("password"))
        assertFalse(actual.containsKey("_id"))
        assertEquals("localhost", (actual["setting"] as Map<*, *>)["host"])
    }

    @Test
    fun `first real change has no predecessor and later change uses last recorded snapshot`() {
        val event = slot<BotHistoryEvent>()
        every { dao.append(capture(event)) } returns Unit
        BotHistoryService.configuration("ns", "bot", "rag-settings", "alice", { mapOf("enabled" to false) }, { mapOf("enabled" to true) })
        assertNull(event.captured.snapshot!!.previous)
        val first = event.captured
        every { dao.latest("ns", "bot", "rag-settings") } returns first
        BotHistoryService.configuration("ns", "bot", "rag-settings", "bob", { first.snapshot!!.current }, { mapOf("enabled" to false) })
        assertEquals(first.snapshot!!.current, event.captured.snapshot!!.previous)
        assertEquals("bob", event.captured.author)
    }

    @Test
    fun `no op and credential only saves never create events even without history`() {
        BotHistoryService.configuration(
            "ns",
            "bot",
            "rag-settings",
            "alice",
            { mapOf("apiKey" to "old", "enabled" to true) },
            { mapOf("apiKey" to "new", "enabled" to true) },
        )
        verify(exactly = 0) { dao.append(any()) }
    }

    @Test
    fun `failed save creates no event and history failure preserves successful save`() {
        assertFailsWith<IllegalStateException> {
            BotHistoryService.configuration("ns", "bot", "rag-settings", "alice", { null }, { error("save failed") })
        }
        verify(exactly = 0) { dao.append(any()) }
        every { dao.append(any()) } throws IllegalStateException("write failed")
        val saved = mapOf("enabled" to true)
        assertEquals(saved, BotHistoryService.configuration("ns", "bot", "rag-settings", "alice", { null }, { saved }))
    }

    @Test
    fun `contacts receive stable ids and duplicate ids are rejected`() {
        val contacts = BotDashboardService.saveContacts("ns", "bot", listOf(BotContact("owner", "Alice"), BotContact("tech", "Bob")))
        assertEquals(2, contacts.map { it.id }.distinct().size)
        assertTrue(contacts.all { !it.id.isNullOrBlank() })
        assertEquals(contacts, BotDashboardService.saveContacts("ns", "bot", contacts))
        assertFailsWith<BadRequestException> { BotDashboardService.saveContacts("ns", "bot", listOf(contacts.first(), contacts.first())) }
    }

    @Test
    fun `identity author and note ownership come from server arguments`() {
        val saved = BotDashboardService.saveIdentity("ns", "bot", BotIdentityRequest("Lea", "notes"), "alice")
        assertEquals("alice", saved.updatedBy)
        assertNotNull(saved.updatedAt)
        BotDashboardService.saveNote("ns", "bot", "purged-session", BotNoteRequest("still useful"), "alice")
        verify { dao.saveNote(match { it.namespace == "ns" && it.botId == "bot" && it.indexSessionId == "purged-session" && it.updatedBy == "alice" }) }
    }

    @Test
    fun `dashboard purge failure occurs after configuration and secret cleanup and is propagated`() {
        val vectorStoreDAO = mockk<BotVectorStoreConfigurationDAO>(relaxed = true)
        val evaluationDAO = mockk<EvaluationSampleDAO>(relaxed = true)
        val secret = RawSecretKey("test-secret")
        val config = BotVectorStoreConfiguration(newId(), "ns", "bot", true, PGVectorStoreSetting("localhost", 5432, "user", secret, "db"))
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein {
                bind<BotDashboardDAO>() with singleton { dao }
                bind<BotApplicationConfigurationDAO>() with singleton { mockk(relaxed = true) }
                bind<StoryDefinitionConfigurationDAO>() with singleton { mockk(relaxed = true) }
                bind<BotBusinessRulesConfigurationDAO>() with singleton { mockk { every { findByNamespaceAndBotId(any(), any()) } returns null } }
                bind<BotRAGConfigurationDAO>() with singleton { mockk { every { findByNamespaceAndBotId(any(), any()) } returns null } }
                bind<BotSentenceGenerationConfigurationDAO>() with singleton { mockk { every { findByNamespaceAndBotId(any(), any()) } returns null } }
                bind<BotObservabilityConfigurationDAO>() with singleton { mockk { every { findByNamespaceAndBotId(any(), any()) } returns null } }
                bind<BotDocumentCompressorConfigurationDAO>() with singleton { mockk { every { findByNamespaceAndBotId(any(), any()) } returns null } }
                bind<BotVectorStoreConfigurationDAO>() with singleton { vectorStoreDAO }
                bind<DatasetDAO>() with singleton { mockk(relaxed = true) }
                bind<IndicatorDAO>() with singleton { mockk(relaxed = true) }
                bind<MetricDAO>() with singleton { mockk(relaxed = true) }
                bind<EvaluationSampleDAO>() with singleton { evaluationDAO }
                bind<I18nDAO>() with singleton { mockk(relaxed = true) }
            },
        )
        every { vectorStoreDAO.findByNamespaceAndBotId("ns", "bot") } returns config
        val failure = IllegalStateException("Dashboard purge failed")
        every { dao.delete("ns", "bot") } throws failure
        mockkObject(SecurityUtils)
        try {
            every { SecurityUtils.deleteSecret(secret) } returns Unit
            assertEquals(failure, assertFailsWith<IllegalStateException> { BotAdminService.deleteApplication(ApplicationDefinition("bot", namespace = "ns")) })
            verifyOrder {
                vectorStoreDAO.delete(config._id)
                SecurityUtils.deleteSecret(secret)
                evaluationDAO.deleteByNamespaceAndBotId("ns", "bot")
                dao.delete("ns", "bot")
            }
        } finally {
            unmockkObject(SecurityUtils)
        }
    }

    @Test
    fun `page cursor round trips date and id and limits are validated`() {
        val first = BotHistoryEvent("ns", "bot", "created", "alice", date = Instant.parse("2026-09-15T12:00:00Z"), _id = "69a005172e453ac08b2a4d39".toId())
        every { dao.history("ns", "bot", null, 2) } returns listOf(first, first.copy(_id = "69a005172e453ac08b2a4d38".toId()))
        val page = BotDashboardService.history("ns", "bot", null, "1")
        assertTrue(page.hasMore)
        assertEquals(1, page.events.size)
        val cursor = BotDashboardService.decodeCursor(page.nextCursor!!)
        assertEquals(first.date, cursor.date)
        assertEquals(first._id, cursor.id)
        assertFailsWith<BadRequestException> { BotDashboardService.history("ns", "bot", null, "0") }
        assertFailsWith<BadRequestException> { BotDashboardService.history("ns", "bot", "broken", null) }
    }

    @Test
    fun `estimated creation is merged once across pages including equal dates without writing`() {
        val appId = "69a005172e453ac08b2a4d36"
        val date = Instant.parse("2026-02-26T08:32:23Z")
        val events =
            listOf(
                BotHistoryEvent("ns", "bot", "connector", "alice", date = date.plusSeconds(1), _id = "69a005172e453ac08b2a4d38".toId()),
                BotHistoryEvent("ns", "bot", "connector", "alice", date = date, _id = "69a005172e453ac08b2a4d37".toId()),
                BotHistoryEvent("ns", "bot", "connector", "alice", date = date, _id = "69a005172e453ac08b2a4d35".toId()),
            )
        every { dao.history("ns", "bot", any(), any()) } answers {
            val cursor = thirdArg<ai.tock.bot.admin.dashboard.BotHistoryCursor?>()
            events.filter { cursor == null || it.date < cursor.date || (it.date == cursor.date && it._id.toString() < cursor.id.toString()) }.take(arg(3))
        }
        for (limit in listOf("1", "2", "3", "10")) {
            val collected = mutableListOf<BotHistoryEventResponse>()
            var cursor: String? = null
            do {
                val page = BotDashboardService.history("ns", "bot", cursor, limit, appId)
                collected.addAll(page.events)
                cursor = page.nextCursor
                assertEquals(page.hasMore, cursor != null)
                assertTrue(collected.size <= 4)
            } while (cursor != null)
            assertEquals(listOf(events[0]._id.toString(), events[1]._id.toString(), appId, events[2]._id.toString()), collected.map { it.id })
            val creation = collected.single { it.type == "created" }
            assertTrue(creation.estimated)
            assertEquals(date, creation.date)
            assertNull(creation.author)
        }
        verify(exactly = 0) { dao.append(any()) }
    }

    @Test
    fun `real creation takes precedence and invalid application ids have no estimate`() {
        val created = BotHistoryEvent("ns", "bot", "created", "alice")
        every { dao.latest("ns", "bot", "created") } returns created
        every { dao.history("ns", "bot", null, 51) } returns listOf(created)
        val real = BotDashboardService.history("ns", "bot", null, null, "69a005172e453ac08b2a4d36").events.single()
        assertFalse(real.estimated)
        assertEquals("alice", real.author)
        assertEquals(created.date, real.date)
        every { dao.latest("ns", "bot", "created") } returns null
        every { dao.history("ns", "bot", null, 51) } returns emptyList()
        assertTrue(BotDashboardService.history("ns", "bot", null, null, "legacy-id").events.isEmpty())
        val estimate = BotDashboardService.history("ns", "bot", null, null, "69a005172e453ac08b2a4d36")
        assertEquals(1, estimate.events.size)
        assertFalse(estimate.hasMore)
    }
}
