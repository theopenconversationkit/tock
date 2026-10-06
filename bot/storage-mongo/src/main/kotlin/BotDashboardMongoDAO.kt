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
import ai.tock.bot.admin.dashboard.BotDashboardDAO
import ai.tock.bot.admin.dashboard.BotDashboardMetadata
import ai.tock.bot.admin.dashboard.BotHistoryCursor
import ai.tock.bot.admin.dashboard.BotHistoryEvent
import ai.tock.bot.admin.dashboard.BotIdentity
import ai.tock.bot.admin.dashboard.BotIndexSessionNote
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import org.litote.kmongo.getCollection

internal class BotDashboardMongoDAO(
    database: MongoDatabase,
) : BotDashboardDAO {
    private val metadata = database.getCollection<BotDashboardMetadata>("bot_dashboard_metadata")
    private val notes = database.getCollection<BotIndexSessionNote>("bot_index_session_note")
    private val events = database.getCollection<BotHistoryEvent>("bot_history_event")

    init {
        metadata.createIndex(Indexes.ascending("namespace", "botId"), IndexOptions().unique(true))
        notes.createIndex(Indexes.ascending("namespace", "botId", "indexSessionId"), IndexOptions().unique(true))
        events.createIndex(Indexes.compoundIndex(Indexes.ascending("namespace", "botId"), Indexes.descending("date", "_id")))
        events.createIndex(Indexes.compoundIndex(Indexes.ascending("namespace", "botId", "type"), Indexes.descending("date", "_id")))
    }

    private fun scope(
        namespace: String,
        botId: String,
    ) = Filters.and(Filters.eq("namespace", namespace), Filters.eq("botId", botId))

    override fun metadata(
        namespace: String,
        botId: String,
    ): BotDashboardMetadata? = metadata.find(scope(namespace, botId)).first()

    override fun saveIdentity(
        namespace: String,
        botId: String,
        identity: BotIdentity,
    ) {
        metadata.updateOne(scope(namespace, botId), Updates.set("identity", identity), UpdateOptions().upsert(true))
    }

    override fun saveContacts(
        namespace: String,
        botId: String,
        contacts: List<BotContact>,
    ) {
        metadata.updateOne(scope(namespace, botId), Updates.set("contacts", contacts), UpdateOptions().upsert(true))
    }

    override fun note(
        namespace: String,
        botId: String,
        sessionId: String,
    ): BotIndexSessionNote? = notes.find(Filters.and(scope(namespace, botId), Filters.eq("indexSessionId", sessionId))).first()

    override fun saveNote(note: BotIndexSessionNote) {
        notes.updateOne(
            Filters.and(scope(note.namespace, note.botId), Filters.eq("indexSessionId", note.indexSessionId)),
            Updates.combine(Updates.set("text", note.text), Updates.set("updatedAt", note.updatedAt), Updates.set("updatedBy", note.updatedBy)),
            UpdateOptions().upsert(true),
        )
    }

    override fun append(event: BotHistoryEvent) {
        events.insertOne(event)
    }

    override fun latest(
        namespace: String,
        botId: String,
        type: String,
    ): BotHistoryEvent? = events.find(Filters.and(scope(namespace, botId), Filters.eq("type", type))).sort(Indexes.descending("date", "_id")).first()

    override fun history(
        namespace: String,
        botId: String,
        before: BotHistoryCursor?,
        limit: Int,
    ): List<BotHistoryEvent> {
        val filter =
            before?.let {
                Filters.and(
                    scope(namespace, botId),
                    Filters.or(
                        Filters.lt("date", it.date),
                        Filters.and(Filters.eq("date", it.date), Filters.lt("_id", it.id)),
                    ),
                )
            } ?: scope(namespace, botId)
        return events
            .find(filter)
            .sort(Indexes.descending("date", "_id"))
            .limit(limit)
            .toList()
    }

    override fun delete(
        namespace: String,
        botId: String,
    ) {
        metadata.deleteMany(scope(namespace, botId))
        notes.deleteMany(scope(namespace, botId))
        events.deleteMany(scope(namespace, botId))
    }
}
