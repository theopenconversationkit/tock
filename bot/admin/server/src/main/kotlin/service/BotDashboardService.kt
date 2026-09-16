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

import ai.tock.bot.admin.dashboard.BotContact
import ai.tock.bot.admin.dashboard.BotDashboardDAO
import ai.tock.bot.admin.dashboard.BotHistoryCursor
import ai.tock.bot.admin.dashboard.BotHistorySnapshot
import ai.tock.bot.admin.dashboard.BotIdentity
import ai.tock.bot.admin.dashboard.BotIndexSessionNote
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import org.bson.types.ObjectId
import org.litote.kmongo.toId
import java.time.Instant
import java.util.Base64
import java.util.UUID

data class BotIdentityRequest(
    val displayName: String? = null,
    val notes: String? = null,
)

data class BotNoteRequest(
    val text: String,
)

data class BotNoteResponse(
    val indexSessionId: String,
    val text: String,
    val updatedAt: Instant?,
    val updatedBy: String?,
)

data class BotHistoryEventResponse(
    val id: String,
    val date: Instant,
    val type: String,
    val author: String?,
    val params: Map<String, Any?>,
    val snapshot: BotHistorySnapshot?,
    val estimated: Boolean = false,
)

data class BotHistoryResponse(
    val events: List<BotHistoryEventResponse>,
    val hasMore: Boolean,
    val nextCursor: String?,
)

object BotDashboardService {
    private val dao: BotDashboardDAO get() = injector.provide()

    fun identity(
        namespace: String,
        botId: String,
    ): BotIdentity = dao.metadata(namespace, botId)?.identity ?: BotIdentity()

    fun saveIdentity(
        namespace: String,
        botId: String,
        request: BotIdentityRequest,
        author: String,
    ): BotIdentity {
        if (request.displayName == null || request.notes == null) throw BadRequestException("Identity displayName and notes are required")
        val saved = BotIdentity(request.displayName, request.notes, Instant.now(), author)
        dao.saveIdentity(namespace, botId, saved)
        return saved
    }

    fun contacts(
        namespace: String,
        botId: String,
    ): List<BotContact> = dao.metadata(namespace, botId)?.contacts.orEmpty()

    fun saveContacts(
        namespace: String,
        botId: String,
        contacts: List<BotContact>,
    ): List<BotContact> {
        if (contacts.any { it.role.isBlank() || it.name.isBlank() }) throw BadRequestException("Contact role and name are required")
        val ids = contacts.mapNotNull { it.id }
        if (ids.any { it.isBlank() } || ids.distinct().size != ids.size) throw BadRequestException("Contact ids must be nonempty and unique")
        val saved = contacts.map { it.copy(id = it.id ?: UUID.randomUUID().toString()) }
        dao.saveContacts(namespace, botId, saved)
        return saved
    }

    fun note(
        namespace: String,
        botId: String,
        sessionId: String,
    ): BotNoteResponse = (dao.note(namespace, botId, sessionId) ?: BotIndexSessionNote(namespace, botId, sessionId)).response()

    fun saveNote(
        namespace: String,
        botId: String,
        sessionId: String,
        request: BotNoteRequest,
        author: String,
    ): BotNoteResponse {
        val note = BotIndexSessionNote(namespace, botId, sessionId, request.text, Instant.now(), author)
        dao.saveNote(note)
        return note.response()
    }

    private fun BotIndexSessionNote.response() = BotNoteResponse(indexSessionId, text, updatedAt, updatedBy)

    fun history(
        namespace: String,
        botId: String,
        before: String?,
        limit: String?,
        applicationId: String? = null,
    ): BotHistoryResponse {
        val size = if (limit == null) 50 else limit.toIntOrNull() ?: throw BadRequestException("Invalid limit")
        if (size !in 1..200) throw BadRequestException("Limit must be between 1 and 200")
        val cursor = before?.let { decodeCursor(it) }
        val creation =
            applicationId
                ?.takeIf { ObjectId.isValid(it) }
                ?.takeIf { dao.latest(namespace, botId, "created") == null }
                ?.let {
                    // The application's identifier dates its creation in this database, not necessarily its original creation before an import.
                    BotHistoryEventResponse(it, ObjectId(it).date.toInstant(), "created", null, emptyMap(), null, estimated = true)
                }?.takeIf { cursor == null || it.date < cursor.date || (it.date == cursor.date && it.id < cursor.id.toString()) }
        val found =
            (
                dao
                    .history(namespace, botId, cursor, size + 1)
                    .map { BotHistoryEventResponse(it._id.toString(), it.date, it.type, it.author, it.params, it.snapshot) } + listOfNotNull(creation)
            ).sortedWith(compareByDescending<BotHistoryEventResponse> { it.date }.thenByDescending { it.id })
        val page = found.take(size)
        val hasMore = found.size > size
        return BotHistoryResponse(
            page,
            hasMore,
            if (hasMore) encodeCursor(page.last()) else null,
        )
    }

    internal fun encodeCursor(event: BotHistoryEventResponse): String =
        Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString("${event.date.toEpochMilli()}|${event.id}".toByteArray(Charsets.UTF_8))

    internal fun decodeCursor(value: String): BotHistoryCursor =
        try {
            require(value.length <= 256)
            val parts = String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8).split('|')
            require(parts.size == 2 && parts[1].matches(Regex("[0-9a-fA-F]{24}")))
            BotHistoryCursor(Instant.ofEpochMilli(parts[0].toLong()), parts[1].toId())
        } catch (_: Exception) {
            throw BadRequestException("Invalid history cursor")
        }

    fun delete(
        namespace: String,
        botId: String,
    ) = dao.delete(namespace, botId)
}
