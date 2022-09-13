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

package ai.tock.bot.mongo

import ai.tock.bot.admin.scenario.Scenario
import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.bot.admin.scenario.ScenarioState
import ai.tock.shared.exception.scenario.ScenarioNotFoundException
import ai.tock.shared.exception.scenario.ScenarioWithNoIdException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.deleteMany
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScenarioDAOTest : AbstractTest() {

    private val ID1 = "id_test_1"
    private val ID2 = "id_test_2"

    private val VERSION = "version_test_1"

    @BeforeEach
    fun clearDB() {
        ScenarioMongoDAO.scenarioDatabase.deleteMany()
    }

    @Test
    fun `findAll WHEN database empty THEN return emptyList`() {
        //WHEN
        val scenarioFound = ScenarioMongoDAO.findAll()

        //THEN
        assertTrue(scenarioFound.isEmpty())
    }

    @Test
    fun `findAll WHEN 2 scenarios exists THEN returns list of 2 scenario`() {
        //GIVEN
        ScenarioMongoDAO.update(createScenario(ID1, VERSION))
        ScenarioMongoDAO.update(createScenario(ID2, VERSION))

        //WHEN
        val scenarioFound = ScenarioMongoDAO.findAll()

        //THEN
        assertEquals(2, scenarioFound.size)
    }

    @Test
    fun `findById WHEN id exists in database THEN returns scenario`() {
        //GIVEN
        val scenario = createScenario(ID1, VERSION)
        ScenarioMongoDAO.update(scenario)

        //WHEN
        val scenarioFound = ScenarioMongoDAO.findByVersion(VERSION)

        //THEN
        assertEquals(scenario, scenarioFound)
    }

    @Test
    fun `findById WHEN id does not exists in database THEN return null`() {
        //GIVEN
        val scenario = createScenario(ID1, VERSION)
        ScenarioMongoDAO.update(scenario)

        //WHEN
        val scenarioFound = ScenarioMongoDAO.findByVersion(ID2)

        //THEN
        assertNull(scenarioFound)
    }

    @Test
    fun `create GIVEN scenario without an id THEN add scenario in database`() {
        //GIVEN
        val scenario = createScenario(null, null)

        //WHEN
        val scenarioCreated = ScenarioMongoDAO.create(scenario)

        //THEN
        assertNotNull(scenarioCreated?.id)
        assertNotNull(scenarioCreated?.versions?.first()?.version)
        assertNotNull(ScenarioMongoDAO.findByVersion(scenarioCreated?.versions?.first()?.version.toString()))
    }

    @Test
    fun `create GIVEN scenario with id THEN throws TockIllegaleArgumentException`() {
        //GIVEN
        val scenario = createScenario(ID1, null)

        //WHEN
        val scenarioCreated = ScenarioMongoDAO.create(scenario)

        //THEN
        assertEquals(ID1, scenarioCreated?.id)
        assertNotNull(scenarioCreated?.versions?.first()?.version)
        assertNotNull(ScenarioMongoDAO.findByVersion(scenarioCreated?.versions?.first()?.version.toString()))
    }

    @Test
    fun `update GIVEN scenario with id THEN update scenario in database`() {
        //GIVEN
        val scenario = createScenario(ID1, VERSION)

        //WHEN
        val scenarioCreated = ScenarioMongoDAO.update(scenario)

        //THEN
        assertEquals(ID1, scenarioCreated?.id)
        assertEquals(VERSION, scenarioCreated?.versions?.first()?.version)
        assertNotNull(ScenarioMongoDAO.findById(ID1))
        assertNotNull(ScenarioMongoDAO.findByVersion(VERSION))
    }

    @Test
    fun `update GIVEN scenario without an id THEN throws TockIllegalArgumentException`() {
        //GIVEN
        val scenario = createScenario(null, VERSION)

        //WHEN //THEN
        assertThrows<ScenarioWithNoIdException> {  ScenarioMongoDAO.update(scenario) }
    }

    @Test
    fun `delete GIVEN scenario exists in database THEN is deleted`() {
        //GIVEN
        ScenarioMongoDAO.update(createScenario(ID1, VERSION))

        //WHEN //THEN
        ScenarioMongoDAO.delete(ID1)
    }

    @Test
    fun `delete GIVEN scenario does not exists in database THEN throws TockNotFound`() {
        //GIVEN //WHEN //THEN
        assertThrows<ScenarioNotFoundException> {  ScenarioMongoDAO.delete(ID1) }
    }

    private fun createScenario(id: String?, version: String?): Scenario {
        return Scenario(
            id = id,
            versions = listOf(ScenarioVersion(version = version, name = "test", applicationId = "test", state = ScenarioState.DRAFT))
        )
    }
}