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

import ai.tock.bot.admin.dashboard.BotContact
import ai.tock.bot.admin.dashboard.BotHistoryCursor
import ai.tock.bot.admin.dashboard.BotHistoryEvent
import ai.tock.bot.admin.dashboard.BotHistorySnapshot
import ai.tock.bot.admin.dashboard.BotIdentity
import ai.tock.bot.admin.dashboard.BotIndexSessionNote
import ai.tock.shared.getDatabase
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BotDashboardMongoDAOTest : AbstractTest(false) {
    @Test
    fun `metadata updates preserve other fields and pagination never loses equal dates`() {
        val database = getDatabase("dashboard_test_" + UUID.randomUUID().toString().replace("-", ""))
        try {
            val dao = BotDashboardMongoDAO(database)
            val contacts = listOf(BotContact("owner", "Alice", "contact-1"))
            dao.saveContacts("ns", "bot", contacts)
            dao.saveIdentity("ns", "bot", BotIdentity("Lea", "notes"))
            assertEquals(contacts, dao.metadata("ns", "bot")!!.contacts)
            assertEquals("Lea", dao.metadata("ns", "bot")!!.identity.displayName)
            dao.saveContacts("ns", "bot", emptyList())
            assertEquals("Lea", dao.metadata("ns", "bot")!!.identity.displayName)
            dao.saveNote(BotIndexSessionNote("ns", "bot", "purged", "old"))
            dao.saveNote(BotIndexSessionNote("ns", "bot", "purged", "new"))
            assertEquals("new", dao.note("ns", "bot", "purged")!!.text)
            val date = Instant.parse("2026-09-15T12:00:00Z")
            repeat(5) {
                dao.append(
                    BotHistoryEvent(
                        "ns",
                        "bot",
                        "rag-settings",
                        "alice",
                        snapshot = BotHistorySnapshot(null, mapOf("maxDocumentsRetrieved" to it)),
                        date = date,
                        _id = ObjectId().toHexString().toId(),
                    ),
                )
            }
            dao.append(BotHistoryEvent("other", "bot", "created", "bob", date = date))
            val first = dao.history("ns", "bot", null, 2)
            val second = dao.history("ns", "bot", first.last().let { BotHistoryCursor(it.date, it._id.toString().toId()) }, 2)
            val third = dao.history("ns", "bot", second.last().let { BotHistoryCursor(it.date, it._id.toString().toId()) }, 2)
            assertEquals(5, (first + second + third).map { it._id }.distinct().size)
            assertEquals(first.first(), dao.latest("ns", "bot", "rag-settings"))
            dao.delete("ns", "bot")
            assertNull(dao.metadata("ns", "bot"))
            assertNull(dao.note("ns", "bot", "purged"))
            assertTrue(dao.history("ns", "bot", null, 10).isEmpty())
            assertEquals(1, dao.history("other", "bot", null, 10).size)
        } finally {
            database.drop()
        }
    }
}
