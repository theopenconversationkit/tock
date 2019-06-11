/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.admin.user.UserReportQuery
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.shared.defaultNamespace
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
internal class UserTimelineMongoDAOTest : AbstractTest() {

    @Test
    fun `getClientDialogs retrieves user timeline WHEN clientId is not null`() {
        val id = PlayerId("id", PlayerType.user, "clientId")
        val u = UserTimeline(id, dialogs = mutableListOf(Dialog(setOf(id))))
        UserTimelineMongoDAO.save(u)
        assertEquals(
            u.dialogs,
            UserTimelineMongoDAO.getClientDialogs(id.clientId!!, { error("no story provided") })
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
    fun `get userTimeLine with temporaryIds `() {
        val id = PlayerId("id", PlayerType.user, "clientId")
        val u = UserTimeline(id, dialogs = mutableListOf(Dialog(setOf(id))), temporaryIds = mutableSetOf("123456879", "1477854545"))
        UserTimelineMongoDAO.save(u)
        assertEquals(
            u.toString(),
            UserTimelineMongoDAO.loadByTemporaryIdsWithoutDialogs(listOf("123456879", "99999999")).firstOrNull()?.toString()
        )
    }

    @Test
    fun `updatePlayerId update timeline and dialog player id`() {
        val id = PlayerId("id", PlayerType.user)
        val u = UserTimeline(id, dialogs = mutableListOf(Dialog(setOf(id))))
        UserTimelineMongoDAO.save(u)
        println(UserTimelineMongoDAO.loadWithLastValidDialog(id, null) { error("no story provided") })

        val newId = PlayerId("id", PlayerType.user, "a")
        UserTimelineMongoDAO.updatePlayerId(id, newId)
        println(UserTimelineMongoDAO.loadWithLastValidDialog(newId, null) { error("no story provided") })
        assertEquals(
            u.dialogs.map { it.copy(playerIds = setOf(newId)) },
            UserTimelineMongoDAO.getClientDialogs(newId.clientId!!) { error("no story provided") }
        )
        assertEquals(
            newId,
            UserTimelineMongoDAO.loadWithoutDialogs(newId).playerId
        )

    }

    @Test
    fun `disableBot update userState`() {
        val id = PlayerId("userid")
        val u = UserTimeline(id)
        UserTimelineMongoDAO.save(u)
        UserTimelineMongoDAO.disableBot("userid")
        assertEquals(
            UserTimelineMongoDAO.loadWithoutDialogs(id).userState.botDisabled,
            true
        )
    }

    @Test
    fun `enableBot update userState`() {
        val id = PlayerId("userid", PlayerType.user)
        val u = UserTimeline(id)
        UserTimelineMongoDAO.save(u)
        UserTimelineMongoDAO.enableBot("userid")
        assertEquals(
            UserTimelineMongoDAO.loadWithoutDialogs(id).userState.botDisabled,
            false
        )
    }

}