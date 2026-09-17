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

import ai.tock.bot.admin.dialog.CountByDateResult
import ai.tock.bot.admin.dialog.CountResult
import ai.tock.bot.admin.dialog.DialogStatsQuery
import ai.tock.bot.admin.dialog.DialogUsageStats
import com.mongodb.client.model.Accumulators.sum
import com.mongodb.client.model.Aggregates.group
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Aggregates.project
import com.mongodb.client.model.Aggregates.unwind
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.elemMatch
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.gte
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.Filters.lte
import com.mongodb.client.model.Filters.or
import org.bson.Document
import org.bson.conversions.Bson

/** One traversal of the relevant actions for all dashboard usage counters. */
internal object DialogUsageAggregation {
    fun pipeline(
        query: DialogStatsQuery,
        applicationIds: Set<String>,
    ): List<Bson> {
        val dates = Document()
        query.from?.let { dates.append("\$gte", it.toInstant()) }
        query.to?.let { dates.append("\$lte", it.toInstant()) }
        val scope = mutableListOf<Bson>(eq("namespace", query.namespace), `in`("applicationIds", applicationIds))
        if (dates.isNotEmpty()) {
            // Both bounds must apply to the same action, including in conversations
            // spanning the whole period. Never substitute a lastUpdateDate range here.
            scope += elemMatch("stories", elemMatch("actions", Document("date", dates)))
        }
        val actions = mutableListOf<Bson>(`in`("stories.actions.applicationId", applicationIds))
        query.from?.let { actions += gte("stories.actions.date", it.toInstant()) }
        query.to?.let { actions += lte("stories.actions.date", it.toInstant()) }
        actions +=
            or(
                eq("stories.actions.playerId.type", "user"),
                and(eq("stories.actions.playerId.type", "bot"), `in`("stories.actions.botMetadata.feedback.vote", listOf("UP", "DOWN"))),
            )
        val user = Document("\$eq", listOf("\$stories.actions.playerId.type", "user"))

        fun vote(value: String) =
            Document(
                "\$and",
                listOf(
                    Document("\$eq", listOf("\$stories.actions.playerId.type", "bot")),
                    Document("\$eq", listOf("\$stories.actions.botMetadata.feedback.vote", value)),
                ),
            )

        fun count(condition: Document) = Document("\$cond", listOf(condition, 1L, 0L))
        val day =
            Document(
                "\$dateToString",
                Document("format", "%Y-%m-%d")
                    .append("date", "\$stories.actions.date")
                    .append("timezone", "Europe/Paris"),
            )
        return listOf(
            match(and(scope)),
            // Discard large dialog state/text before unwinding the nested arrays.
            project(
                Document("stories.actions.applicationId", 1)
                    .append("stories.actions.date", 1)
                    .append("stories.actions.playerId.type", 1)
                    .append("stories.actions.botMetadata.feedback.vote", 1),
            ),
            unwind("\$stories"),
            unwind("\$stories.actions"),
            match(and(actions)),
            group(
                Document("applicationId", "\$stories.actions.applicationId").append("date", day),
                sum("messages", count(user)),
                sum("up", count(vote("UP"))),
                sum("down", count(vote("DOWN"))),
            ),
        )
    }

    fun result(rows: List<Document>): DialogUsageStats {
        val byDate =
            rows
                .mapNotNull { row ->
                    val key = row.get("_id", Document::class.java)
                    val total = (row["messages"] as Number).toLong()
                    if (total == 0L) null else CountByDateResult(key.getString("applicationId"), key.getString("date"), total)
                }.sortedWith(compareBy({ it.applicationId }, { it.date }))

        fun totals(field: String): List<CountResult> =
            rows
                .groupBy { it.get("_id", Document::class.java).getString("applicationId") }
                .map { (app, values) -> CountResult(app, values.sumOf { (it[field] as Number).toLong() }) }
                .sortedBy { it.applicationId }
        return DialogUsageStats(totals("messages"), byDate, totals("up"), totals("down"))
    }
}
