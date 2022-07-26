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

package ai.tock.bot.admin.scenario

import ai.tock.shared.exception.TockIllegalArgumentException
import ai.tock.shared.exception.TockNotFound
import ai.tock.shared.tockInternalInjector
import ai.tock.shared.exception.rest.ConflictException
import ai.tock.shared.exception.rest.InternalServerException
import ai.tock.shared.exception.rest.NotFoundException
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenarioServiceImplTest {

    private val ID1 = "id_test_1"
    private val ID2 = "id_test_2"

    private val datePrevious = ZonedDateTime.parse("2021-01-01T00:00:00+01:00")
    private val dateNow = ZonedDateTime.parse("2022-01-01T00:00:00+01:00")

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


    @BeforeEach
    fun prepareMockk() {
        mockkStatic(ZonedDateTime::class)
        every { ZonedDateTime.now() } returns dateNow
    }

    @AfterEach
    fun clearMockk() {
        clearAllMocks()
    }

    @Test
    fun `findAll WHEN dao findAll return list of 1 valid scenario THEN return a list with 1 Scenario`() {
        //GIVEN
        every { scenarioDAO.findAll() } returns listOf(createScenarioForId(ID1))

        //WHEN
        val scenariosFound =  scenarioService.findAll()

        //THEN
        assertEquals(1, scenariosFound.size)
        assertTrue { scenariosFound.fold(true) { condition, element -> (condition && element != null) } }
    }

    @Test
    fun `findAll WHEN dao findAll return list of 1 invalid scenario THEN throw InternalServerException`() {
        //GIVEN
        every { scenarioDAO.findAll() } returns listOf(createScenarioForId(null))

        //WHEN //THEN
        assertThrows<InternalServerException> { scenarioService.findAll() }
    }

    @Test
    fun `findById WHEN dao findById return a valid scenario THEN return Scenario`() {
        val id: String = ID1

        //GIVEN
        every { scenarioDAO.findById(id) } returns createScenarioForId(id)

        //WHEN
        val scenarioFind = scenarioService.findById(id)

        //THEN
        assertEquals(createScenarioForId(id), scenarioFind)
    }

    @Test
    fun `findById WHEN dao findAll return list of 1 invalid scenario THEN throw InternalServerException`() {
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
    fun `create WHEN dao create return a valid scenario THEN return Scenario`() {
        val id: String = ID1

        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(null, null)
        val scenarioToCreate: Scenario = createScenarioForId(null, dateNow)
        val scenarioCreated: Scenario = createScenarioForId(id, dateNow)
        every { scenarioDAO.create(scenarioToCreate) } returns scenarioCreated

        //WHEN
        val scenario =  scenarioService.create(scenarioRequest)

        //THEN
        assertEquals(createScenarioForId(id, dateNow), scenario)
    }

    @Test
    fun `create WHEN dao create return invalid scenario THEN throw InternalServerException`() {
        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(null, null)
        val scenarioCreated: Scenario = createScenarioForId(null, dateNow)
        every { scenarioDAO.create(scenarioCreated) } returns scenarioCreated

        //WHEN //THEN
        assertThrows<InternalServerException> { scenarioService.create(scenarioRequest) }
    }

    @Test
    fun `create WHEN dao create return null THEN throw InternalServerException`() {
        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(null, null)
        val scenarioCreated: Scenario = createScenarioForId(null, dateNow)
        every { scenarioDAO.create(scenarioCreated) } returns null

        //WHEN //THEN
        assertThrows<NotFoundException> { scenarioService.create(scenarioRequest) }
    }

    @Test
    fun `create WHEN dao create throw exception THEN throw InternalServerException`() {
        val id: String = ID1

        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(id)
        every { scenarioDAO.create(scenarioRequest) } throws TockIllegalArgumentException("test_illegal_argument")

        //WHEN //THEN
        assertThrows<ConflictException> { scenarioService.create(scenarioRequest) }
    }

    @Test
    fun `update WHEN dao update return a valid scenario THEN return Scenario`() {
        val id: String = ID1

        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(id)
        val scenarioFind: Scenario = createScenarioForId(id, datePrevious, null)
        val scenarioUpdated: Scenario = createScenarioForId(id, datePrevious, dateNow)
        every { scenarioDAO.findById(id) } returns scenarioFind
        every { scenarioDAO.update(scenarioUpdated) } returns scenarioUpdated

        //WHEN
        val scenario =  scenarioService.update(id, scenarioRequest)

        //THEN
        assertEquals(createScenarioForId(id, datePrevious, dateNow), scenario)
    }

    @Test
    fun `update GIVEN scenario with id different than id on url THEN throw InternalServerException`() {
        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(ID1)
        val scenarioFind: Scenario = createScenarioForId(ID2)
        every { scenarioDAO.findById(ID2) } returns scenarioFind

        //WHEN //THEN
        assertThrows<ConflictException> { scenarioService.update(ID2, scenarioRequest) }
    }

    @Test
    fun `update WHEN dao update return invalid scenario THEN throw InternalServerException`() {
        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(ID1)
        val scenarioFind: Scenario = createScenarioForId(ID1, datePrevious)
        val scenarioToUpdate: Scenario = createScenarioForId(ID1, datePrevious, dateNow)
        val scenarioUpdated: Scenario = createScenarioForId(null, datePrevious, dateNow)
        every { scenarioDAO.findById(ID1) } returns scenarioFind
        every { scenarioDAO.update(scenarioToUpdate) } returns scenarioUpdated

        //WHEN //THEN
        assertThrows<InternalServerException> { scenarioService.update(ID1, scenarioRequest) }
    }

    @Test
    fun `update WHEN dao update return null THEN throw NotFoundException`() {
        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(ID1)
        val scenarioFind: Scenario = createScenarioForId(ID1, datePrevious)
        val scenarioToUpdate: Scenario = createScenarioForId(ID1, datePrevious, dateNow)
        every { scenarioDAO.findById(ID1) } returns scenarioFind
        every { scenarioDAO.update(scenarioToUpdate) } returns null

        //WHEN //THEN
        assertThrows<NotFoundException> { scenarioService.update(ID1, scenarioRequest) }
    }

    @Test
    fun `update WHEN dao update throw exception THEN throw TockIllegalArgumentException`() {
        val id: String = ID1

        //GIVEN
        val scenarioRequest: Scenario = createScenarioForId(id)
        val scenarioFind: Scenario = createScenarioForId(id, datePrevious)
        val scenarioUpdated: Scenario = createScenarioForId(id, datePrevious, dateNow)
        every { scenarioDAO.findById(id) } returns scenarioFind
        every { scenarioDAO.update(scenarioUpdated) } throws TockIllegalArgumentException("test_illegal_argument")

        //WHEN //THEN
        assertThrows<TockIllegalArgumentException> { scenarioService.update(id, scenarioRequest) }
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
    fun `delete GIVEN id of scenario exist THEN scenario is delete`() {
        val id: String = ID1

        //WHEN
        assertDoesNotThrow { scenarioService.delete(id) }

        //THEN
        verify(exactly = 1) { scenarioDAO.delete(any()) }
    }

    @Test
    fun `delete GIVEN id of scenario don't exist THEN nothing`() {
        val id: String = ID2

        //GIVEN
        every { scenarioDAO.delete(id) } throws TockNotFound("test_not_found")

        //WHEN
        assertDoesNotThrow { scenarioService.delete(id) }

        //THEN
        verify(exactly = 1) { scenarioDAO.delete(any()) }
    }

    private fun createScenarioForId(id: String?,
                                    createDate: ZonedDateTime? = null,
                                    updateDate: ZonedDateTime? = null): Scenario {
        return Scenario(
            id = id,
            name = "test",
            applicationId = "test",
            createDate = createDate,
            updateDate = updateDate,
            state = "test")
    }
}