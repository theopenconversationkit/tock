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

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.dialog.DialogStatsQuery
import ai.tock.bot.admin.dialog.DialogUsageStats
import ai.tock.bot.connector.ConnectorType
import com.mongodb.ExplainVerbosity
import com.mongodb.client.model.Filters.eq
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.junit.jupiter.api.Test
import org.litote.kmongo.coroutine.coroutine
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DialogUsageMongoDAOTest : AbstractTest() {
    private fun dialog(
        namespace: String,
        app: String,
        date: Instant,
        vote: String? = null,
    ): Document {
        val action =
            Document("applicationId", app)
                .append("date", Date.from(date))
                .append("playerId", Document("type", if (vote == null) "user" else "bot"))
        vote?.let { action.append("botMetadata", Document("feedback", Document("vote", it))) }
        return Document("namespace", namespace)
            .append("applicationIds", listOf(app))
            .append("lastUpdateDate", Date())
            .append("stories", listOf(Document("actions", listOf(action))))
    }

    @Test
    fun `real DAO resolves connectors and reads dialog usage including empty configurations`() =
        runBlocking {
            val namespace = "usage_" + UUID.randomUUID().toString()
            val collection = MongoBotConfiguration.asyncDatabase.coroutine.getCollection<Document>("dialog")
            val from = ZonedDateTime.parse("2030-03-01T00:00:00Z")
            val query = DialogStatsQuery(namespace, "model", from, from.plusDays(1))

            fun configuration(
                app: String,
                ns: String = namespace,
                model: String = "model",
            ) = BotApplicationConfiguration(app, "technical-bot", ns, model, ConnectorType("rest"), path = "/$ns/$app")
            val configurations = listOf(configuration("prod"), configuration("test-bot"), configuration("foreign", model = "other-model"), configuration("prod", ns = "$namespace-other"))
            try {
                configurations.forEach { BotApplicationConfigurationMongoDAO.col.insertOne(it) }
                collection.insertMany(
                    listOf(
                        dialog(namespace, "prod", from.toInstant()),
                        dialog(namespace, "prod", from.toInstant(), "UP"),
                        dialog(namespace, "prod", from.plusDays(1).toInstant(), "DOWN"),
                        dialog(namespace, "test-bot", from.toInstant()),
                        dialog(namespace, "test-bot", from.toInstant(), "UP"),
                        dialog(namespace, "foreign", from.toInstant()),
                        dialog("$namespace-other", "prod", from.toInstant()),
                        dialog(namespace, "prod", from.minusDays(1).toInstant()),
                    ),
                )
                val result = UserTimelineMongoDAO.calculateDialogUsage(query)
                assertEquals(mapOf("prod" to 1L, "test-bot" to 1L), result.allUserActions.associate { it.applicationId to it.total })
                assertEquals(mapOf("prod" to 1L, "test-bot" to 1L), result.allFeedbackUp.associate { it.applicationId to it.total })
                assertEquals(mapOf("prod" to 1L, "test-bot" to 0L), result.allFeedbackDown.associate { it.applicationId to it.total })
                assertEquals(setOf("2030-03-01"), result.allUserActionsByDate.map { it.date }.toSet())
                // A model with no configurations must not accidentally fall back to all namespace dialogs.
                assertEquals(DialogUsageStats(), UserTimelineMongoDAO.calculateDialogUsage(query.copy(applicationName = "missing")))
                assertEquals(DialogUsageStats(), UserTimelineMongoDAO.calculateDialogUsage(query.copy(from = from.plusDays(2), to = from.plusDays(3))))
            } finally {
                configurations.forEach { BotApplicationConfigurationMongoDAO.delete(it) }
                collection.deleteMany(Document("namespace", Document("\$in", listOf(namespace, "$namespace-other"))))
            }
        }

    @Test
    fun `action date index reduces examined documents on selective periods`() =
        runBlocking {
            val namespace = "usage_index_" + UUID.randomUUID().toString()
            val collection = MongoBotConfiguration.asyncDatabase.coroutine.getCollection<Document>("dialog")
            val from = ZonedDateTime.parse("2030-03-01T00:00:00Z")
            val query = DialogStatsQuery(namespace, "model", from, from.plusDays(1))
            try {
                collection.insertMany(
                    (0 until 1000).map { dialog(namespace, "prod", from.minusDays(30).toInstant()) } +
                        (0 until 10).map { dialog(namespace, "prod", from.toInstant()) },
                )
                val pipeline = DialogUsageAggregation.pipeline(query, setOf("prod"))
                // Use the indexes actually created by the production DAO initialization.
                val baselineIndex = Document("applicationIds", 1).append("namespace", 1).append("rating", 1).append("test", 1)

                fun examined(explain: Document): Long {
                    fun find(value: Any?): Long? =
                        when (value) {
                            is Map<*, *> -> (value["totalDocsExamined"] as? Number)?.toLong() ?: value.values.firstNotNullOfOrNull { find(it) }
                            is Iterable<*> -> value.firstNotNullOfOrNull { find(it) }
                            else -> null
                        }
                    return requireNotNull(find(explain)) { explain.toJson() }
                }
                val baseline =
                    examined(
                        collection
                            .aggregate<Document>(pipeline)
                            .hint(baselineIndex)
                            .publisher
                            .explain(ExplainVerbosity.EXECUTION_STATS)
                            .awaitFirst(),
                    )
                val indexedPlan =
                    collection
                        .aggregate<Document>(pipeline)
                        .hint(DialogUsageAggregation.index)
                        .publisher
                        .explain(ExplainVerbosity.EXECUTION_STATS)
                        .awaitFirst()
                val indexed = examined(indexedPlan)
                val selected =
                    examined(
                        collection
                            .aggregate<Document>(pipeline)
                            .publisher
                            .explain(ExplainVerbosity.EXECUTION_STATS)
                            .awaitFirst(),
                    )
                assertTrue(indexed < baseline, "Indexed=$indexed, baseline=$baseline: ${indexedPlan.toJson()}")
                assertEquals(10, collection.aggregate<Document>(pipeline.take(1)).toList().size)
                assertEquals(
                    10L,
                    DialogUsageAggregation
                        .result(collection.aggregate<Document>(pipeline).toList())
                        .allUserActions
                        .single()
                        .total,
                )
                println("Dashboard usage explain: baselineDocs=$baseline indexedDocs=$indexed plannerDocs=$selected")
            } finally {
                collection.deleteMany(eq("namespace", namespace))
            }
        }
}
