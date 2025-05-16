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

import ai.tock.bot.admin.indicators.metric.Metric
import ai.tock.bot.admin.indicators.metric.MetricType
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.mongo.AbstractTest
import indicator.metric.MetricMongoDAO
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class MetricMongoDAOTest : AbstractTest() {

    private val botId1 = "botId1"
    private val botId2 = "botId2"
    private val creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS)

    private val metric1 = Metric(
        type = MetricType.QUESTION_REPLIED,
        indicatorName = "name1",
        indicatorValueName = "value1",
        emitterStoryId = "storyId1",
        trackedStoryId = "storyId2",
        playerIds = setOf(
            PlayerId(id = "playerId1", PlayerType.user),
            PlayerId(id = "playerId2", PlayerType.bot),
        ),
        dialogId = "dialogId1".toId(),
        creationDate = creationDate,
        botId = botId1,
    )

    private val metric2 = Metric(
        type = MetricType.QUESTION_REPLIED,
        indicatorName = "name2",
        indicatorValueName = "value2",
        emitterStoryId = "storyId3",
        trackedStoryId = "storyId3",
        playerIds = setOf(
            PlayerId(id = "playerId1", PlayerType.user),
            PlayerId(id = "playerId2", PlayerType.bot),
        ),
        dialogId = "dialogId2".toId(),
        creationDate = creationDate,
        botId = botId1,
    )

    private val metric3 = Metric(
        type = MetricType.QUESTION_REPLIED,
        indicatorName = "name3",
        indicatorValueName = "value3",
        emitterStoryId = "storyId4",
        trackedStoryId = "storyId5",
        playerIds = setOf(
            PlayerId(id = "playerId1", PlayerType.user),
            PlayerId(id = "playerId2", PlayerType.bot),
        ),
        dialogId = "dialogId3".toId(),
        creationDate = creationDate,
        botId = botId2,
    )

    private fun initDb(metrics: List<Metric> = emptyList()) {
        metrics.forEach(MetricMongoDAO::save)
    }

    @BeforeEach
    fun clearDB() {
        // Delete all documents
        MetricMongoDAO.col.drop()
    }

    @AfterEach
    fun clearMockk() {
        clearAllMocks()
    }

    @Test
    fun `findByBotId all metrics when it exists`() {
        // GIVEN
        initDb(listOf(metric1, metric2, metric3))
        // WHEN
        val result = MetricMongoDAO.findAllByBotId(botId1)
        // THEN
        assertEquals(listOf(metric1, metric2), result)
    }

    @Test
    fun `save one metric`() {
        // GIVEN
        initDb()
        // WHEN
        MetricMongoDAO.save(metric1)
        // THEN
        assertEquals(listOf(metric1), MetricMongoDAO.findAllByBotId(botId1))
    }

    @Test
    fun `save many metrics`() {
        // GIVEN
        initDb()
        // WHEN
        MetricMongoDAO.saveAll(listOf(metric1, metric2))
        // THEN
        assertEquals(listOf(metric1, metric2), MetricMongoDAO.findAllByBotId(botId1))
    }

}
