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
import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.bot.admin.scenario.ScenarioVersionState
import ai.tock.bot.mongo.AbstractTest
import ai.tock.shared.exception.scenario.version.ScenarioVersionNotFoundException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.toId
import scenario.ScenarioGroupMongoDAO
import scenario.ScenarioVersionMongoDAO
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScenarioVersionDAOTest : AbstractTest() {


    private val dateNow = ZonedDateTime.now()
    private val botId1 = "botId1"
    private val botId2 = "botId2"
    private val unknownAnswerId = "unknownAnswerId"
    private val groupId1 = "groupId1".toId<ScenarioGroup>()
    private val groupId2 = "groupId2".toId<ScenarioGroup>()

    private val scenarioVersion1 = ScenarioVersion(scenarioGroupId = groupId1, data = "DATA1", state = ScenarioVersionState.DRAFT, comment = "v1", creationDate = dateNow, updateDate = dateNow)
    private val scenarioVersion2 = ScenarioVersion(scenarioGroupId = groupId1, data = "DATA2", state = ScenarioVersionState.DRAFT, comment = "v2", creationDate = dateNow, updateDate = dateNow)
    private val scenarioVersion3 = ScenarioVersion(scenarioGroupId = groupId2, data = "DATA3", state = ScenarioVersionState.CURRENT, comment = "v1", creationDate = dateNow, updateDate = dateNow)
    private val scenarioVersion3Copy = scenarioVersion3.copy(data = "DATA-COPY", comment = "comment-Copy", state = ScenarioVersionState.ARCHIVED)
    private val scenarioGroup1 = ScenarioGroup(_id = groupId1, botId = botId1, name = "name1", creationDate = dateNow, updateDate = dateNow,
        versions = listOf(scenarioVersion1, scenarioVersion2),
        unknownAnswerId = unknownAnswerId
    )
    private val scenarioGroup2 = ScenarioGroup(_id = groupId2, botId = botId2, name = "name2", creationDate = dateNow, updateDate = dateNow,
        versions = listOf(scenarioVersion3),
        unknownAnswerId = unknownAnswerId
    )


    private fun initDb(scenarioGroups: List<ScenarioGroup>){
        scenarioGroups.forEach {
            ScenarioGroupMongoDAO.createOne(it)
            ScenarioVersionMongoDAO.createMany(it.versions)
        }
    }

    @BeforeEach
    fun clearDB() {
        // Delete all documents
        ScenarioGroupMongoDAO.collection.drop()
        ScenarioVersionMongoDAO.collection.drop()
    }

    @AfterEach
    fun clearMockk() {
        clearAllMocks()
    }

    @Test
    fun `createOne WHEN database empty THEN return created scenario version`() {
        // GIVEN
        initDb(emptyList())
        // WHEN
        val result = ScenarioVersionMongoDAO.createOne(scenarioVersion1)
        // THEN
        assertEquals(scenarioVersion1, result)
    }

    @Test
    fun `createMany WHEN database empty THEN return created scenario versions`() {
        // GIVEN
        initDb(emptyList())
        // WHEN
        val versions = listOf(scenarioVersion1, scenarioVersion2, scenarioVersion3)
        val result = ScenarioVersionMongoDAO.createMany(listOf(scenarioVersion1, scenarioVersion2, scenarioVersion3))
        // THEN
        assertEquals(versions, result)
    }

    @Test
    fun `findAll WHEN database empty THEN return emptyList`() {
        // GIVEN
        initDb(emptyList())
        // WHEN
        val result = ScenarioVersionMongoDAO.findAll()
        // THEN
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findAll WHEN 3 scenario versions exists THEN return 3 scenarios versions`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup2))
        // WHEN
        val result = ScenarioVersionMongoDAO.findAll()
        // THEN
        assertEquals(3, result.count())
    }

    @Test
    fun `countAllByScenarioGroupId WHEN 2 scenario versions matching the group id exists THEN count 2`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup2))
        // WHEN
        val result = ScenarioVersionMongoDAO.countAllByScenarioGroupId(groupId1)
        // THEN
        assertEquals(2, result)
    }

    @Test
    fun `findOneById WHEN id exists in database THEN returns scenario version`() {
        // GIVEN
        initDb(listOf(scenarioGroup1))
        // WHEN
        val result = ScenarioVersionMongoDAO.findOneById(scenarioVersion1._id)
        // THEN
        assertEquals(scenarioVersion1, result?.copy(creationDate = dateNow, updateDate = dateNow))
    }

    @Test
    fun `findOneById WHEN id does not exists in database THEN return null`() {
        // GIVEN
        initDb(listOf(scenarioGroup1))
        // WHEN
        val result = ScenarioVersionMongoDAO.findOneById(scenarioVersion3._id)
        // THEN
        assertNull(result)
    }


    @Test
    fun `findAllByScenarioGroupIdAndState WHEN 2 scenario draft versions exists THEN return 2 scenario versions`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup2))
        // WHEN
        val result = ScenarioVersionMongoDAO.findAllByScenarioGroupIdAndState(groupId1, ScenarioVersionState.DRAFT)
        // THEN
        assertEquals(2, result.count())
    }

    @Test
    fun `deleteOneById WHEN id exists in database THEN delete a scenario version`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup2))
        // WHEN
        ScenarioVersionMongoDAO.deleteOneById(scenarioVersion1._id)
        // THEN
        assertNull(ScenarioVersionMongoDAO.findOneById(scenarioVersion1._id))
    }
    
    @Test
    fun `deleteOneById WHEN id does not exists in database THEN throw exception`() {
        // GIVEN
        initDb(listOf(scenarioGroup1))
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            ScenarioVersionMongoDAO.deleteOneById(scenarioVersion3._id)
        }
    }

    @Test
    fun `deleteAllByScenarioGroupId WHEN scenario versions matching the group id exist in the database THEN delete those scenario versions`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup2))
        // WHEN
        ScenarioVersionMongoDAO.deleteAllByScenarioGroupId(groupId1)
        // THEN
        assertNull(ScenarioVersionMongoDAO.findOneById(scenarioVersion1._id))
    }

    @Test
    fun `deleteAllByScenarioGroupId WHEN scenario versions matching the group id do not exist in the database THEN throw exception`() {
        // GIVEN
        initDb(listOf(scenarioGroup1))
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            ScenarioVersionMongoDAO.deleteAllByScenarioGroupId(groupId2)
        }
    }

    @Test
    fun `updateOne WHEN id does not exists in database THEN throw exception`() {
        // GIVEN
        initDb(listOf(scenarioGroup1))
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            ScenarioVersionMongoDAO.updateOne(scenarioVersion3Copy)
        }
    }

    @Test
    fun `updateOne WHEN id exists in database THEN update a scenario version`() {
        // GIVEN
        initDb(listOf(scenarioGroup1, scenarioGroup2))
        // WHEN
        ScenarioVersionMongoDAO.updateOne(scenarioVersion3Copy)
        val result = ScenarioVersionMongoDAO.findOneById(scenarioVersion3._id)
        // THEN
        assertEquals(scenarioVersion3Copy, result?.copy(creationDate = dateNow, updateDate = dateNow))
    }
}