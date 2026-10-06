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
import ai.tock.bot.admin.dialog.DialogStatsQuery
import ai.tock.bot.admin.dialog.DialogUsageStats
import ai.tock.shared.getDatabase
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.`in`
import org.bson.Document
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID
import kotlin.test.assertEquals

class DialogUsageAggregationTest : AbstractTest(false) {
    @Test
    fun `counts messages and feedback with exact period application and namespace isolation`() {
        val database = getDatabase("dashboard_usage_test_" + UUID.randomUUID().toString().replace("-", ""))
        try {
            val collection = database.getCollection("dialog")
            // DST starts on this day: midnight in Paris is 23:00 UTC the day before.
            val from = "2026-03-28T23:00:00Z"
            val to = "2026-03-29T22:00:00Z"
            val query = DialogStatsQuery("ns", "bot", ZonedDateTime.parse(from), ZonedDateTime.parse(to))

            fun action(
                date: String,
                player: String = "user",
                app: String = "prod",
                vote: String? = null,
            ): Document =
                Document("date", Date.from(Instant.parse(date)))
                    .append("applicationId", app)
                    .append("playerId", Document("type", player))
                    .append("botMetadata", if (vote == null) Document() else Document("feedback", Document("vote", vote)))

            fun dialog(
                namespace: String,
                vararg stories: List<Document>,
            ): Document =
                Document("namespace", namespace)
                    .append("applicationIds", listOf("prod", "test-bot"))
                    .append("lastUpdateDate", Date.from(Instant.parse("2026-04-10T00:00:00Z")))
                    .append("stories", stories.map { Document("actions", it) })
            collection.insertMany(
                listOf(
                    dialog(
                        "ns",
                        listOf(
                            action("2026-03-28T22:59:59Z"),
                            action(from),
                            action("2026-03-29T12:00:00Z", vote = "DOWN"), // User metadata is not a bot vote.
                            action(to),
                            action("2026-03-29T22:00:01Z"),
                            action(from, "bot", vote = "UP"),
                            action(to, "bot", vote = "DOWN"),
                            action(from, "bot"),
                            action(from, "bot", vote = "OTHER"),
                            action(from, app = "foreign"),
                            action(from, "bot", "foreign", "UP"),
                        ),
                        listOf(action(from, app = "test-bot"), action(from, "bot", "test-bot", "UP")),
                    ),
                    // Bounds on different actions must not admit this dialog in the prescreen.
                    dialog("ns", listOf(action("2026-03-01T00:00:00Z")), listOf(action("2026-04-01T00:00:00Z"))),
                    dialog("other", listOf(action(from), action(from, "bot", vote = "UP"))),
                    dialog("ns", listOf(action("2026-01-01T00:00:00Z"))),
                    // The in-range action belongs to another app, even though the dialog belongs to both.
                    dialog("ns", listOf(action("2026-01-01T00:00:00Z"), action(from, app = "foreign"))),
                ),
            )
            val pipeline = DialogUsageAggregation.pipeline(query, setOf("prod", "test-bot"))
            assertEquals(1, collection.aggregate(pipeline.take(1)).toList().size)
            val stats = DialogUsageAggregation.result(collection.aggregate(pipeline).toList())
            assertEquals(mapOf("prod" to 3L, "test-bot" to 1L), stats.allUserActions.associate { it.applicationId to it.total })
            assertEquals(mapOf("prod" to 1L, "test-bot" to 1L), stats.allFeedbackUp.associate { it.applicationId to it.total })
            assertEquals(mapOf("prod" to 1L, "test-bot" to 0L), stats.allFeedbackDown.associate { it.applicationId to it.total })
            assertEquals(
                listOf(
                    CountByDateResult("prod", "2026-03-29", 2),
                    CountByDateResult("prod", "2026-03-30", 1),
                    CountByDateResult("test-bot", "2026-03-29", 1),
                ),
                stats.allUserActionsByDate,
            )
            // Removing only the early date filter must preserve exact counts.
            val unfiltered = listOf(match(and(eq("namespace", "ns"), `in`("applicationIds", listOf("prod", "test-bot"))))) + pipeline.drop(1)
            assertEquals(stats, DialogUsageAggregation.result(collection.aggregate(unfiltered).toList()))
            val instant = DialogUsageAggregation.pipeline(query.copy(to = query.from), setOf("prod"))
            val instantStats = DialogUsageAggregation.result(collection.aggregate(instant).toList())
            assertEquals(1L, instantStats.allUserActions.single().total)
            assertEquals(1L, instantStats.allFeedbackUp.single().total)
            assertEquals(0L, instantStats.allFeedbackDown.single().total)
            val empty = DialogUsageAggregation.pipeline(query.copy(from = query.to!!.plusDays(10), to = query.to!!.plusDays(11)), setOf("prod"))
            assertEquals(DialogUsageStats(), DialogUsageAggregation.result(collection.aggregate(empty).toList()))
        } finally {
            database.drop()
        }
    }
}
