/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.mongo.scenario

import ai.tock.bot.admin.scenario.ScenarioGroup
import ai.tock.bot.mongo.AbstractTest
import ai.tock.shared.exception.scenario.group.ScenarioGroupDuplicatedException
import ai.tock.shared.exception.scenario.group.ScenarioGroupNotFoundException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import scenario.ScenarioGroupMongoDAO
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScenarioGroupDAOTest : AbstractTest() {


    private val dateNow = ZonedDateTime.now()
    private val botId1 = "botId1"
    private val botId2 = "botId2"

    private val scenarioGroup1 = ScenarioGroup(botId = botId1, name = "name1", creationDate = dateNow, updateDate = dateNow)
    private val scenarioGroup2 = ScenarioGroup(botId = botId1, name = "name2", creationDate = dateNow, updateDate = dateNow)
    private val scenarioGroup3 = ScenarioGroup(botId = botId2, name = "name3", creationDate = dateNow, updateDate = dateNow)
    private val scenarioGroup3Copy = scenarioGroup3.copy(name = "name3Copy", category = "categoryCopy", description = "descCopy")

    private fun initDb(scenarioGroups: List<ScenarioGroup>){
        scenarioGroups.forEach(ScenarioGroupMongoDAO::createOne)
    }

    @BeforeEach
    fun clearDB() {
        // Delete all documents
        ScenarioGroupMongoDAO.collection.drop()
    }

    @AfterEach
    fun clearMockk() {
        clearAllMocks()
    }



    @Test
    fun `createOne WHEN database empty THEN return created scenario group`() {
        // GIVEN
        initDb(emptyList())
        // WHEN
        val result = ScenarioGroupMongoDAO.createOne(scenarioGroup1)
        // THEN
        assertEquals(scenarioGroup1, result)
    }

    @Test
    fun `createOne WHEN an other scenario group exits with same name THEN throws exception`() {
        // GIVEN
        initDb(listOf(scenarioGroup1))
        // WHEN // THEN
        assertThrows<ScenarioGroupDuplicatedException> {
            ScenarioGroupMongoDAO.createOne(scenarioGroup1)
        }
    }



    @Test
    fun `findAll WHEN database empty THEN return emptyList`() {
        // GIVEN
        initDb(emptyList())
        // WHEN
        val result = ScenarioGroupMongoDAO.findAll()
        // THEN
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findAll WHEN 3 scenarios group exists for THEN return 3 scenarios group`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup2, scenarioGroup3))
        // WHEN
        val result = ScenarioGroupMongoDAO.findAll()
        // THEN
        assertEquals(3, result.count())
    }

    @Test
    fun `findAllByBotId WHEN 2 scenarios group exists for botId1 THEN return 2 scenarios group`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup2, scenarioGroup3))
        // WHEN
        val result = ScenarioGroupMongoDAO.findAllByBotId(botId1)
        // THEN
        assertEquals(2, result.count())
    }

    @Test
    fun `findAllByBotId WHEN 0 scenarios group exists for botId1 THEN return emptyList`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup2))
        // WHEN
        val result = ScenarioGroupMongoDAO.findAllByBotId(botId2)
        // THEN
        assertEquals(0, result.count())
    }

    @Test
    fun `findOneById WHEN id exists in database THEN returns scenario`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup3))
        // WHEN
        val result = ScenarioGroupMongoDAO.findOneById(scenarioGroup3._id)
        // THEN
        assertEquals(scenarioGroup3, result?.copy(creationDate = dateNow, updateDate = dateNow))
    }

    @Test
    fun `findOneById WHEN id does not exists in database THEN return null`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup3))
        // WHEN
        val result = ScenarioGroupMongoDAO.findOneById(scenarioGroup2._id)
        // THEN
        assertNull(result)
    }

    @Test
    fun `deleteOneById WHEN id exists in database THEN delete a scenario group`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup3))
        // WHEN
        ScenarioGroupMongoDAO.deleteOneById(scenarioGroup1._id)
        // THEN
        assertNull(ScenarioGroupMongoDAO.findOneById(scenarioGroup1._id))
    }
    
    @Test
    fun `deleteOneById WHEN id does not exists in database THEN throw exception`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup3))
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            ScenarioGroupMongoDAO.deleteOneById(scenarioGroup2._id)
        }
    }

    @Test
    fun `updateOne WHEN scenario group does not exists in database THEN throw exception`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup2))
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            ScenarioGroupMongoDAO.updateOne(scenarioGroup3Copy)
        }
    }

    @Test
    fun `updateOne WHEN scenario group exists in database THEN update a scenario group`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup3))
        // WHEN
        ScenarioGroupMongoDAO.updateOne(scenarioGroup3Copy)
        val result = ScenarioGroupMongoDAO.findOneById(scenarioGroup3._id)
            // THEN
        assertEquals(scenarioGroup3Copy, result?.copy(creationDate = dateNow, updateDate = dateNow))
    }
}