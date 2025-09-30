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

import ai.tock.bot.admin.user.UserReportQuery
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.shared.defaultNamespace
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 *
 */
internal class UserTimelineMongoDAOTest : AbstractTest() {

    @Test
    fun `getClientDialogs retrieves user timeline WHEN clientId is not null`() = runBlocking {
        val id = PlayerId("id", PlayerType.user, "clientId")
        val u = UserTimeline(id, dialogs = mutableListOf(Dialog(setOf(id))))
        UserTimelineMongoDAO.save(u, "namespace")
        assertNotEquals(
            u.dialogs,
            UserTimelineMongoDAO.getClientDialogs("namespace", id.clientId!!) { error("no story provided") }
        )
        delay(1000)
        assertEquals(
            u.dialogs,
            UserTimelineMongoDAO.getClientDialogs("namespace", id.clientId!!) { error("no story provided") }
        )
    }

    @Test
    fun `search with flags does not fail`() {
        UserTimelineMongoDAO.search(
            UserReportQuery(
                defaultNamespace,
                "bot_open_data",
                Locale.FRENCH,
                flags = mapOf("tock_profile_loaded" to "true")
            )
        )
    }

    @Test
    fun `get userTimeLine with temporaryIds `()  = runBlocking{
        val id = PlayerId("id", PlayerType.user, "clientId")
        val u = UserTimeline(id, dialogs = mutableListOf(Dialog(setOf(id))), temporaryIds = mutableSetOf("123456879", "1477854545"))
        UserTimelineMongoDAO.save(u, "namespace")
        assertEquals(
            u.toString(),
            UserTimelineMongoDAO.loadByTemporaryIdsWithoutDialogs("namespace", listOf("123456879", "99999999")).firstOrNull()?.toString()
        )
    }

    @Test
    fun `updatePlayerId update timeline and dialog player id`()  = runBlocking {
        val id = PlayerId("id", PlayerType.user)
        val u = UserTimeline(id, dialogs = mutableListOf(Dialog(setOf(id))))
        UserTimelineMongoDAO.save(u, "namespace")
        println(UserTimelineMongoDAO.loadWithLastValidDialog("namespace", id, null) { error("no story provided") })

        val newId = PlayerId("id", PlayerType.user, "a")
        UserTimelineMongoDAO.updatePlayerId("namespace", id, newId)
        println(UserTimelineMongoDAO.loadWithLastValidDialog("namespace", newId, null) { error("no story provided") })
        assertEquals(
            u.dialogs.map { it.copy(playerIds = setOf(newId)) },
            UserTimelineMongoDAO.getClientDialogs("namespace", newId.clientId!!) { error("no story provided") }
        )
        assertEquals(
            newId,
            UserTimelineMongoDAO.loadWithoutDialogs("namespace", newId).playerId
        )
    }
}
