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

package ai.tock.bot.mongo.indicator.metric

import ai.tock.bot.admin.indicators.metric.CustomMetric
import ai.tock.bot.admin.indicators.metric.Metric
import ai.tock.bot.admin.indicators.metric.MetricFilter
import ai.tock.bot.admin.indicators.metric.MetricGroupBy
import ai.tock.bot.admin.indicators.metric.MetricType
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.mongo.AbstractTest
import ai.tock.shared.sumByLong
import indicator.metric.MetricMongoDAO
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MetricAggregationMongoDAOTest : AbstractTest() {
    private val namespace1 = "namespace1"
    private val applicationId1 = "app1"
    private val botId1 = "botId1"
    private val botId2 = "botId2"
    private val storyId1 = "storyId1"
    private val storyId2 = "storyId2"
    private val storyId3 = "storyId3"
    private val storyId10 = "storyId10"
    private val storyId20 = "storyId20"
    private val storyId30 = "storyId30"
    private val storyId40 = "storyId40"
    private val indicatorName1 = "indicatorName1"
    private val indicatorName2 = "indicatorName2"
    private val indicatorValue1 = "indicatorValue1"
    private val indicatorValue2 = "indicatorValue2"

    private var creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS)

    private val dialogId = "dialogId1".toId<Dialog>()
    private val playerIds =
        setOf(
            PlayerId(id = "playerId1", PlayerType.user),
            PlayerId(id = "playerId2", PlayerType.bot),
        )

    private val metrics =
        listOf(
            // --------------------CASE 01--------------------------
            Metric(
                type = MetricType.STORY_HANDLED,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            Metric(
                type = MetricType.QUESTION_ASKED,
                indicatorName = indicatorName1,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            Metric(
                type = MetricType.QUESTION_REPLIED,
                indicatorName = indicatorName1,
                indicatorValueName = indicatorValue1,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            // --------------------CASE 02--------------------------
            Metric(
                type = MetricType.STORY_HANDLED,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            Metric(
                type = MetricType.QUESTION_ASKED,
                indicatorName = indicatorName1,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            Metric(
                type = MetricType.QUESTION_REPLIED,
                indicatorName = indicatorName1,
                indicatorValueName = indicatorValue1,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            // --------------------CASE 03--------------------------
            Metric(
                type = MetricType.STORY_HANDLED,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            Metric(
                type = MetricType.QUESTION_ASKED,
                indicatorName = indicatorName1,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            Metric(
                type = MetricType.QUESTION_REPLIED,
                indicatorName = indicatorName1,
                indicatorValueName = indicatorValue2,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            // --------------------CASE 04--------------------------
            Metric(
                type = MetricType.STORY_HANDLED,
                trackedStoryId = storyId1,
                emitterStoryId = storyId3,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            Metric(
                type = MetricType.QUESTION_ASKED,
                indicatorName = indicatorName2,
                trackedStoryId = storyId1,
                emitterStoryId = storyId3,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            Metric(
                type = MetricType.QUESTION_REPLIED,
                indicatorName = indicatorName2,
                indicatorValueName = indicatorValue1,
                trackedStoryId = storyId1,
                emitterStoryId = storyId3,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            // --------------------CASE 05--------------------------
            Metric(
                type = MetricType.STORY_HANDLED,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            Metric(
                type = MetricType.QUESTION_ASKED,
                indicatorName = indicatorName1,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            Metric(
                type = MetricType.QUESTION_ASKED,
                indicatorName = indicatorName1,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            // --------------------CASE 06--------------------------
            Metric(
                type = MetricType.STORY_HANDLED,
                trackedStoryId = storyId1,
                emitterStoryId = storyId2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = next(),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            // --------------------CASE 07--------------------------
            Metric(
                type = MetricType.STORY_HANDLED,
                trackedStoryId = storyId10,
                emitterStoryId = storyId20,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = Instant.now().minus(5, ChronoUnit.DAYS),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
            // --------------------CASE 08--------------------------
            Metric(
                type = MetricType.QUESTION_REPLIED,
                trackedStoryId = storyId30,
                emitterStoryId = storyId40,
                indicatorName = indicatorName1,
                indicatorValueName = indicatorValue2,
                playerIds = playerIds,
                dialogId = dialogId,
                creationDate = Instant.now().plus(5, ChronoUnit.DAYS),
                botId = botId1,
                namespace = namespace1,
                applicationId = applicationId1,
            ),
        )

    private fun next() = creationDate.plusMillis(100).also { creationDate = it }

    private fun initDb(metrics: List<Metric> = emptyList()) = metrics.forEach(MetricMongoDAO::save)

    @BeforeEach
    fun clearDB() {
        MetricMongoDAO.col.drop()
    }

    @AfterEach
    fun clearMockk() {
        clearAllMocks()
    }

    @Test
    fun `filter by botId and one metric type without grouping`() {
        // GIVEN
        initDb(metrics)

        // WHEN
        val filter = MetricFilter(namespace1, botId1, listOf(MetricType.STORY_HANDLED))
        val groupBy = emptyList<MetricGroupBy>()
        val result = MetricMongoDAO.filterAndGroupBy(filter, groupBy)

        // THEN
        val predicates =
            listOf({ m: Metric -> m.botId == botId1 }, { m: Metric -> filter.types?.contains(m.type) == true })
        assertEquals(metrics.count { m -> predicates.all { it(m) } }, result.count())
        assertTrue(result.all { it.count == 1 })
        assertGroupByAttribute(groupBy, result)
    }

    @Test
    fun `filter by botId and multiple metric types without grouping`() {
        // GIVEN
        initDb(metrics)

        // WHEN
        val filter = MetricFilter(namespace1, botId1, listOf(MetricType.QUESTION_ASKED, MetricType.QUESTION_REPLIED))
        val groupBy = emptyList<MetricGroupBy>()
        val result = MetricMongoDAO.filterAndGroupBy(filter, groupBy)

        // THEN
        val predicates =
            listOf({ m: Metric -> m.botId == botId1 }, { m: Metric -> filter.types?.contains(m.type) == true })
        assertEquals(metrics.count { m -> predicates.all { it(m) } }, result.count())
        assertTrue(result.all { it.count == 1 })
        assertGroupByAttribute(groupBy, result)
    }

    @Test
    fun `filter by botId and multiple metric types and one indicator value without grouping`() {
        // GIVEN
        initDb(metrics)

        // WHEN
        val filter =
            MetricFilter(
                namespace1,
                botId1,
                listOf(MetricType.QUESTION_ASKED, MetricType.QUESTION_REPLIED),
                indicatorValueNames = listOf(indicatorValue1),
            )
        val groupBy = emptyList<MetricGroupBy>()
        val result = MetricMongoDAO.filterAndGroupBy(filter, groupBy)

        // THEN
        val predicates =
            listOf(
                { m: Metric -> m.botId == botId1 },
                { m: Metric -> filter.types?.contains(m.type) == true },
                { m: Metric -> filter.indicatorValueNames?.contains(m.indicatorValueName) == true },
            )
        assertEquals(metrics.count { m -> predicates.all { it(m) } }, result.count())
        assertTrue(result.all { it.count == 1 })
        assertGroupByAttribute(groupBy, result)
    }

    @Test
    fun `filter by botId without grouping`() {
        // GIVEN
        initDb(metrics)

        // WHEN
        val groupBy = emptyList<MetricGroupBy>()
        val result1 = MetricMongoDAO.filterAndGroupBy(MetricFilter(namespace1, botId1), groupBy)
        val result2 = MetricMongoDAO.filterAndGroupBy(MetricFilter(namespace1, botId2), groupBy)

        // THEN
        assertEquals(metrics.count { it.botId == botId1 }, result1.count())
        assertEquals(metrics.count { it.botId == botId2 }, result2.count())
        assertTrue(result1.all { it.count == 1 })
        assertTrue(result2.all { it.count == 1 })
        assertGroupByAttribute(groupBy, result1)
        assertGroupByAttribute(groupBy, result2)
    }

    @Test
    fun `filter by botId and group by metric type`() {
        // GIVEN
        initDb(metrics)

        // WHEN
        val filter = MetricFilter(namespace1, botId1)
        val groupBy = listOf(MetricGroupBy.TYPE)
        val result = MetricMongoDAO.filterAndGroupBy(filter, groupBy)

        // THEN
        assertEquals(7, result.first { it.type == MetricType.STORY_HANDLED }.count)
        assertEquals(6, result.first { it.type == MetricType.QUESTION_ASKED }.count)
        assertEquals(5, result.first { it.type == MetricType.QUESTION_REPLIED }.count)
        assertGroupByAttribute(groupBy, result)
    }

    @Test
    fun `filter by botId, creationDate and group by metric type`() {
        // GIVEN
        initDb(metrics)

        // WHEN
        val filter =
            MetricFilter(
                namespace1,
                botId1,
                creationDateSince = creationDate.minus(2, ChronoUnit.DAYS),
                creationDateUntil = creationDate.plus(1, ChronoUnit.DAYS),
            )
        val groupBy = listOf(MetricGroupBy.TYPE)
        val result = MetricMongoDAO.filterAndGroupBy(filter, groupBy)

        // THEN
        assertEquals(6, result.first { it.type == MetricType.STORY_HANDLED }.count)
        assertEquals(6, result.first { it.type == MetricType.QUESTION_ASKED }.count)
        assertEquals(4, result.first { it.type == MetricType.QUESTION_REPLIED }.count)
        assertGroupByAttribute(groupBy, result)
    }

    @Test
    fun `filter by botId, multiple metric types and group by metric type`() {
        // GIVEN
        initDb(metrics)

        // WHEN
        val filter = MetricFilter(namespace1, botId1, listOf(MetricType.QUESTION_ASKED, MetricType.QUESTION_REPLIED))
        val groupBy = listOf(MetricGroupBy.TYPE)
        val result = MetricMongoDAO.filterAndGroupBy(filter, groupBy)

        // THEN
        assertFalse { result.any { it.type == MetricType.STORY_HANDLED } }
        assertEquals(6, result.first { it.type == MetricType.QUESTION_ASKED }.count)
        assertEquals(5, result.first { it.type == MetricType.QUESTION_REPLIED }.count)
        assertGroupByAttribute(groupBy, result)
    }

    @Test
    fun `filter by botId, multiple metric types and group by metric type, trackedStoryId`() {
        // GIVEN
        initDb(metrics)

        // WHEN
        val filter = MetricFilter(namespace1, botId1, listOf(MetricType.QUESTION_ASKED, MetricType.QUESTION_REPLIED))
        val groupBy = listOf(MetricGroupBy.TYPE, MetricGroupBy.TRACKED_STORY_ID)
        val result = MetricMongoDAO.filterAndGroupBy(filter, groupBy)

        // THEN
        assertFalse { result.any { it.type == MetricType.STORY_HANDLED } }
        assertFalse { result.any { it.type == MetricType.QUESTION_ASKED && it.trackedStoryId != storyId1 } }
        assertFalse {
            result.any {
                it.type == MetricType.QUESTION_REPLIED && it.trackedStoryId !in
                    listOf(
                        storyId1, storyId30,
                    )
            }
        }
        assertEquals(6, result.filter { it.type == MetricType.QUESTION_ASKED }.sumByLong { it.count.toLong() })
        assertEquals(5, result.filter { it.type == MetricType.QUESTION_REPLIED }.sumByLong { it.count.toLong() })
        assertEquals(6, result.first { it.type == MetricType.QUESTION_ASKED && it.trackedStoryId == storyId1 }.count)
        assertEquals(4, result.first { it.type == MetricType.QUESTION_REPLIED && it.trackedStoryId == storyId1 }.count)
        assertEquals(1, result.first { it.type == MetricType.QUESTION_REPLIED && it.trackedStoryId == storyId30 }.count)
        assertGroupByAttribute(groupBy, result)
    }

    /**
     * Utility function that checks for the presence of data based on "group by" attributes
     */
    private fun assertGroupByAttribute(
        groupBy: List<MetricGroupBy>,
        result: List<CustomMetric>,
    ) {
        val predicates =
            with(groupBy) {
                listOfNotNull(
                    if (!isEmpty()) { cm: CustomMetric -> cm.id == null } else { cm: CustomMetric -> cm.id != null },
                    if (!contains(MetricGroupBy.TYPE)) { cm: CustomMetric -> cm.type == null } else { cm: CustomMetric -> cm.type != null },
                    if (!contains(MetricGroupBy.EMITTER_STORY_ID)) { cm: CustomMetric -> cm.emitterStoryId == null } else { cm: CustomMetric -> cm.emitterStoryId != null },
                    if (!contains(MetricGroupBy.TRACKED_STORY_ID)) { cm: CustomMetric -> cm.trackedStoryId == null } else { cm: CustomMetric -> cm.trackedStoryId != null },
                    if (!contains(MetricGroupBy.INDICATOR_NAME)) { cm: CustomMetric -> cm.indicatorName == null } else null,
                    if (!contains(MetricGroupBy.INDICATOR_VALUE_NAME)) { cm: CustomMetric -> cm.indicatorValueName == null } else null,
                )
            }

        assertTrue(result.all { m -> predicates.all { it(m) } })
    }
}
