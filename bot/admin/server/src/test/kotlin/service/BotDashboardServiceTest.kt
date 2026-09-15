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

import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfiguration
import ai.tock.bot.admin.dashboard.BotContact
import ai.tock.bot.admin.dashboard.BotDashboardDAO
import ai.tock.bot.admin.dashboard.BotHistoryEvent
import ai.tock.bot.admin.dashboard.BotHistorySnapshot
import ai.tock.genai.orchestratorcore.models.vectorstore.PGVectorStoreSetting
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.security.key.RawSecretKey
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
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
    fun `page cursor round trips date and id and limits are validated`() {
        val first = BotHistoryEvent("ns", "bot", "created", "alice", date = Instant.parse("2026-09-15T12:00:00Z"))
        every { dao.history("ns", "bot", null, 2) } returns listOf(first, first.copy(_id = newId()))
        val page = BotDashboardService.history("ns", "bot", null, "1")
        assertTrue(page.hasMore)
        assertEquals(1, page.events.size)
        val cursor = BotDashboardService.decodeCursor(page.nextCursor!!)
        assertEquals(first.date, cursor.date)
        assertEquals(first._id, cursor.id)
        assertFailsWith<BadRequestException> { BotDashboardService.history("ns", "bot", null, "0") }
        assertFailsWith<BadRequestException> { BotDashboardService.history("ns", "bot", "broken", null) }
    }
}
