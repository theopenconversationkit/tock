/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin.scenario

import ai.tock.shared.exception.TockIllegaleArgumentException
import ai.tock.shared.exception.TockNotFound
import ai.tock.shared.tockInternalInjector
import ai.tock.shared.vertx.ConflictException
import ai.tock.shared.vertx.InternalServerException
import ai.tock.shared.vertx.NotFoundException
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenarioServiceTest {

    private val ID1 = "id_test_1"
    private val ID2 = "id_test_2"

    val scenarioService = ScenarioServiceImpl()

    companion object {
        val scenarioDAO: ScenarioDAO = mockk(relaxed = true)

        init {
            tockInternalInjector = KodeinInjector()
            val module = Kodein.Module {
                bind<ScenarioDAO>() with provider { scenarioDAO }
            }
            tockInternalInjector.inject(
                Kodein {
                    import(module)
                }
            )
        }
    }

    @AfterEach
    fun clearMockk() {
        clearAllMocks()
    }

    @Test
    fun `findAll WHEN dao findAll return list of 1 valide scenario THEN return list of 1 Scenario`() {
        //GIVEN
        every { scenarioDAO.findAll() } returns listOf(createScenarioForId(ID1))

        //WHEN
        val scenariosFound =  scenarioService.findAll()

        //THEN
        assertEquals(1, scenariosFound.size)
        assertTrue { scenariosFound.fold(true) { condition, element -> (condition && element != null) } }
    }

    @Test
    fun `findAll WHEN dao findAll return list of 1 invalide scenario THEN throw InternalServerException`() {
        //GIVEN
        every { scenarioDAO.findAll() } returns listOf(createScenarioForId(null))

        //WHEN //THEN
        assertThrows<InternalServerException> { scenarioService.findAll() }
    }

    @Test
    fun `findById WHEN dao findById return a valide scenario THEN return Scenario`() {
        val id: String = ID1

        //GIVEN
        every { scenarioDAO.findById(id) } returns createScenarioForId(id)

        //WHEN
        val scenariosFind =  scenarioService.findById(id)

        //THEN
        assertEquals(createScenarioForId(id), scenariosFind)
    }

    @Test
    fun `findById WHEN dao findAll return list of 1 invalide scenario THEN throw InternalServerException`() {
        val id: String = ID1

        //GIVEN
        every { scenarioDAO.findById(id) } returns createScenarioForId(null)

        //WHEN //THEN
        assertThrows<InternalServerException> { scenarioService.findById(id) }
    }

    @Test
    fun `findById WHEN dao findAll return null THEN throw InternalServerException`() {
        val id: String = ID1

        //GIVEN
        every { scenarioDAO.findById(id) } returns null

        //WHEN //THEN
        assertThrows<NotFoundException> { scenarioService.findById(id) }
    }

    @Test
    fun `create WHEN dao create return a valide scenario THEN return Scenario`() {
        val id: String = ID1

        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(null)
        val scenarioCreated: Scenario = createScenarioForId(id)
        every { scenarioDAO.create(scenarioRequest) } returns scenarioCreated

        //WHEN
        val scenariosCreated =  scenarioService.create(scenarioRequest)

        //THEN
        assertEquals(createScenarioForId(id), scenariosCreated)
    }

    @Test
    fun `create WHEN dao create return invalide scenario THEN throw InternalServerException`() {
        //GIVEN
        val scenario: Scenario = createScenarioForId(null)
        every { scenarioDAO.create(scenario) } returns scenario

        //WHEN //THEN
        assertThrows<InternalServerException> { scenarioService.create(scenario) }
    }

    @Test
    fun `create WHEN dao create throw exception THEN throw InternalServerException`() {
        val id: String = ID1

        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(id)
        every { scenarioDAO.create(scenarioRequest) } throws TockIllegaleArgumentException("test_illegale_argument")

        //WHEN //THEN
        assertThrows<ConflictException> { scenarioService.create(scenarioRequest) }
    }

    @Test
    fun `update WHEN dao update return a valide scenario THEN return Scenario`() {
        val id: String = ID1

        //GIVEN
        val scenario: Scenario = createScenarioForId(id)
        every { scenarioDAO.findById(id) } returns scenario
        every { scenarioDAO.update(scenario) } returns scenario

        //WHEN
        val scenariosCreated =  scenarioService.update(id, scenario)

        //THEN
        assertEquals(scenario, scenariosCreated)
    }

    @Test
    fun `update GIVEN scenario with id different than id on url THEN throw InternalServerException`() {
        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(ID1)
        val scenarioToUpdate: Scenario = createScenarioForId(ID2)
        every { scenarioDAO.findById(ID2) } returns scenarioToUpdate
        every { scenarioDAO.update(scenarioToUpdate) } returns scenarioToUpdate

        //WHEN //THEN
        assertThrows<ConflictException> { scenarioService.update(ID2, scenarioRequest) }
    }

    @Test
    fun `update WHEN dao update return invalide scenario THEN throw InternalServerException`() {
        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(ID1)
        val scenarioCreated: Scenario = createScenarioForId(null)
        every { scenarioDAO.findById(ID1) } returns scenarioRequest
        every { scenarioDAO.update(scenarioRequest) } returns scenarioCreated

        //WHEN //THEN
        assertThrows<InternalServerException> { scenarioService.update(ID1, scenarioRequest) }
    }

    @Test
    fun `update WHEN dao update throw exception THEN throw TockIllegaleArgumentException`() {
        val id: String = ID1

        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(id)
        every { scenarioDAO.findById(id) } returns scenarioRequest
        every { scenarioDAO.update(scenarioRequest) } throws TockIllegaleArgumentException("test_illegale_argument")

        //WHEN //THEN
        assertThrows<TockIllegaleArgumentException> { scenarioService.update(id, scenarioRequest) }
    }

    @Test
    fun `update WHEN findById not found THEN throw NotFoundException`() {
        val id: String = ID1

        //GIVEN
        every { scenarioDAO.findById(id) } returns null

        //WHEN //THEN
        assertThrows<NotFoundException> { scenarioService.update(id, createScenarioForId(id)) }
    }

    @Test
    fun `delete GIVEN id of scenario existe THEN scenario is delete`() {
        val id: String = ID1

        //WHEN
        assertDoesNotThrow { scenarioService.delete(id) }

        //THEN
        verify(exactly = 1) { scenarioDAO.delete(any()) }
    }

    @Test
    fun `delete GIVEN id of scenario don't existe THEN nothing`() {
        val id: String = ID2

        //GIVEN
        every { scenarioDAO.delete(id) } throws TockNotFound("test_not_found")

        //WHEN
        assertDoesNotThrow { scenarioService.delete(id) }

        //THEN
        verify(exactly = 1) { scenarioDAO.delete(any()) }
    }

    private fun createScenarioForId(id: String?): Scenario {
        return Scenario(id = id, name = "test", applicationId = "test", state = "test")
    }
}