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

package ai.tock.bot.mongo.indicator

import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.IndicatorValue
import ai.tock.bot.mongo.AbstractTest
import indicator.IndicatorMongoDAO
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class IndicatorMongoDAOTest : AbstractTest() {
    private val namespace1 = "namespace1"
    private val botId1 = "botId1"

    private val indicatorSatisfaction =
        Indicator(
            name = "satisfaction",
            label = "the satisfaction label",
            dimensions = setOf("satisfaction", "customer survey"),
            botId = botId1,
            namespace = namespace1,
            values = setOf(IndicatorValue("ok", "Ok"), IndicatorValue("ko", "Ko")),
        )

    private fun initDb(indicators: List<Indicator>) {
        indicators.forEach(IndicatorMongoDAO::save)
    }

    @BeforeEach
    fun clearDB() {
        // Delete all documents
        IndicatorMongoDAO.col.drop()
    }

    @AfterEach
    fun clearMockk() {
        clearAllMocks()
    }

    @Test
    fun `delete an indicator when it not exists`() {
        // GIVEN
        initDb(emptyList())
        // WHEN
        val result = IndicatorMongoDAO.deleteByNameAndApplicationName(indicatorSatisfaction.name, namespace1, botId1)
        // THEN
        assertEquals(false, result)
    }

    @Test
    fun `delete an indicator when present`() {
        // GIVEN
        initDb(listOf(indicatorSatisfaction))
        // WHEN
        val result = IndicatorMongoDAO.deleteByNameAndApplicationName(indicatorSatisfaction.name, namespace1, botId1)
        // THEN
        assertEquals(true, result)
    }

    @Test
    fun `findByNameAndBotId an indicator when it not exists`() {
        // GIVEN
        initDb(emptyList())
        // WHEN
        val result = IndicatorMongoDAO.findByNameAndBotId(indicatorSatisfaction.name, namespace1, botId1)
        // THEN
        assertEquals(null, result)
    }

    @Test
    fun `findByNameAndBotId an indicator when it exists`() {
        // GIVEN
        initDb(listOf(indicatorSatisfaction))
        // WHEN
        val result = IndicatorMongoDAO.findByNameAndBotId(indicatorSatisfaction.name, namespace1, botId1)
        // THEN
        assertEquals(indicatorSatisfaction, result)
    }

    @Test
    fun `check an indicator exists if present`() {
        // GIVEN
        initDb(listOf(indicatorSatisfaction))
        // WHEN
        val result = IndicatorMongoDAO.existByNameAndBotId(indicatorSatisfaction.name, namespace1, botId1)
        // THEN
        assertEquals(true, result)
    }

    @Test
    fun `check an indicator exists if not present`() {
        // GIVEN
        initDb(emptyList())
        // WHEN
        val result = IndicatorMongoDAO.existByNameAndBotId(indicatorSatisfaction.name, namespace1, botId1)
        // THEN
        assertEquals(false, result)
    }
}
