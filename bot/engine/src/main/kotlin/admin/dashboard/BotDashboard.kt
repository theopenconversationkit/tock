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

package ai.tock.bot.admin.dashboard

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

data class BotIdentity(
    val displayName: String = "",
    val notes: String = "",
    val updatedAt: Instant? = null,
    val updatedBy: String? = null,
)

data class BotContact(
    val role: String,
    val name: String,
    val id: String? = null,
    val email: String? = null,
    val link: String? = null,
    val note: String? = null,
    val comment: String? = null,
)

data class BotDashboardMetadata(
    val namespace: String,
    val botId: String,
    val identity: BotIdentity = BotIdentity(),
    val contacts: List<BotContact> = emptyList(),
    val _id: Id<BotDashboardMetadata> = newId(),
)

data class BotIndexSessionNote(
    val namespace: String,
    val botId: String,
    val indexSessionId: String,
    val text: String = "",
    val updatedAt: Instant? = null,
    val updatedBy: String? = null,
    val _id: Id<BotIndexSessionNote> = newId(),
)

data class BotHistorySnapshot(
    val previous: Map<String, Any?>?,
    val current: Map<String, Any?>,
)

data class BotHistoryEvent(
    val namespace: String,
    val botId: String,
    val type: String,
    val author: String,
    val params: Map<String, Any?> = emptyMap(),
    val snapshot: BotHistorySnapshot? = null,
    val date: Instant = Instant.ofEpochMilli(System.currentTimeMillis()),
    val _id: Id<BotHistoryEvent> = newId(),
)

data class BotHistoryCursor(
    val date: Instant,
    val id: Id<BotHistoryEvent>,
)

interface BotDashboardDAO {
    fun metadata(
        namespace: String,
        botId: String,
    ): BotDashboardMetadata?

    fun saveIdentity(
        namespace: String,
        botId: String,
        identity: BotIdentity,
    )

    fun saveContacts(
        namespace: String,
        botId: String,
        contacts: List<BotContact>,
    )

    fun note(
        namespace: String,
        botId: String,
        sessionId: String,
    ): BotIndexSessionNote?

    fun saveNote(note: BotIndexSessionNote)

    fun append(event: BotHistoryEvent)

    fun latest(
        namespace: String,
        botId: String,
        type: String,
    ): BotHistoryEvent?

    fun history(
        namespace: String,
        botId: String,
        before: BotHistoryCursor?,
        limit: Int,
    ): List<BotHistoryEvent>

    fun delete(
        namespace: String,
        botId: String,
    )
}
