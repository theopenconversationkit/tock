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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.scenario.ScenarioGroup
import ai.tock.bot.admin.scenario.ScenarioGroupDAO
import ai.tock.shared.exception.scenario.group.ScenarioGroupDuplicatedException
import ai.tock.shared.exception.scenario.group.ScenarioGroupNotFoundException
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.toId
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenarioGroupServiceTest {
    private val dateNow = ZonedDateTime.parse("2022-01-01T00:00:00.000Z")
    private val groupId1 = "groupId1".toId<ScenarioGroup>()
    private val groupId2 = "groupId2".toId<ScenarioGroup>()

    private val botId1 = "botId1"
    private val unknownAnswerId = "unknownAnswerId"

    private val scenarioGroup1 = ScenarioGroup(_id =groupId1, botId = botId1, name = "name1",
        creationDate = dateNow, updateDate = dateNow, unknownAnswerId = unknownAnswerId)
    private val scenarioGroup2 = ScenarioGroup(_id =groupId2, botId = botId1, name = "name2",
        creationDate = dateNow, updateDate = dateNow, unknownAnswerId = unknownAnswerId)


    companion object {
        private val scenarioGroupDAO: ScenarioGroupDAO = mockk(relaxed = true)

        init {
            tockInternalInjector = KodeinInjector()
            val module = Kodein.Module {
                bind<ScenarioGroupDAO>() with provider { scenarioGroupDAO }
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

    @Test fun `createOne WHEN an other scenario group exits with same name THEN throws exception`() {
        // GIVEN
        every { scenarioGroupDAO.createOne(scenarioGroup1) } throws ScenarioGroupDuplicatedException()
        // WHEN // THEN
        assertThrows<ScenarioGroupDuplicatedException> {
            ScenarioGroupService.createOne(scenarioGroup1)
        }
        verify(exactly = 1) { scenarioGroupDAO.createOne(scenarioGroup1) }
    }

    @Test fun `createOne WHEN no other scenario group exits with same name THEN return created scenario group`() {
        // GIVEN
        every { scenarioGroupDAO.createOne(scenarioGroup1) } returns scenarioGroup1
        // WHEN
        val result = ScenarioGroupService.createOne(scenarioGroup1)
        // THEN
        assertEquals(scenarioGroup1, result)
        verify(exactly = 1) { scenarioGroupDAO.createOne(scenarioGroup1) }
    }

    @Test fun `findAllByBotId WHEN 0 scenarios group exists for botId1 THEN return emptyList`() {
        // GIVEN
        every { scenarioGroupDAO.findAllByBotId(botId1) } returns emptyList()
        // WHEN
        val result = ScenarioGroupService.findAllByBotId(botId1)
        // THEN
        assertTrue(result.isEmpty())
        verify(exactly = 1) { scenarioGroupDAO.findAllByBotId(botId1) }
    }
    @Test fun `findAllByBotId WHEN 2 scenarios group exists for botId1 THEN return 2 scenarios group`() {
        // GIVEN
        val groups = listOf(scenarioGroup1, scenarioGroup2)
        every { scenarioGroupDAO.findAllByBotId(botId1) } returns groups
        // WHEN
        val result = ScenarioGroupService.findAllByBotId(botId1)
        // THEN
        assertEquals(groups, result)
        verify(exactly = 1) { scenarioGroupDAO.findAllByBotId(botId1) }
    }
    @Test fun `findOneById WHEN id does not exists in database THEN throw exception`() {
        // GIVEN
        every { scenarioGroupDAO.findOneById(groupId1) } returns null
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            ScenarioGroupService.findOneById(groupId1.toString())
        }
        verify(exactly = 1) { scenarioGroupDAO.findOneById(groupId1) }
    }
    @Test fun `findOneById WHEN id exists in database THEN returns scenario group`() {
        // GIVEN
        every { scenarioGroupDAO.findOneById(groupId1) } returns scenarioGroup1
        // WHEN
        val result = ScenarioGroupService.findOneById(groupId1.toString())
        // THEN
        assertEquals(scenarioGroup1, result)
        verify(exactly = 1) { scenarioGroupDAO.findOneById(groupId1) }
    }

    @Test fun `updateOne WHEN scenario group does not exists in database THEN throw exception`() {
        // GIVEN
        every { scenarioGroupDAO.updateOne(scenarioGroup1) } throws ScenarioGroupNotFoundException(groupId1.toString())
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            ScenarioGroupService.updateOne(scenarioGroup1)
        }
        verify(exactly = 1) { scenarioGroupDAO.updateOne(scenarioGroup1) }
    }
    @Test fun `updateOne WHEN scenario group exists in database THEN update a scenario group`() {
        // GIVEN
        every { scenarioGroupDAO.updateOne(scenarioGroup1) } returns scenarioGroup1
        // WHEN
        val result = ScenarioGroupService.updateOne(scenarioGroup1)
        // THEN
        assertEquals(scenarioGroup1, result)
        verify(exactly = 1) { scenarioGroupDAO.updateOne(scenarioGroup1) }
    }
    @Test fun `deleteOneById WHEN id does not exists in database THEN throw exception`() {
        // GIVEN
        every { scenarioGroupDAO.deleteOneById(groupId1) } throws ScenarioGroupNotFoundException(groupId1.toString())
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            ScenarioGroupService.deleteOneById(groupId1.toString())
        }
        verify(exactly = 1) { scenarioGroupDAO.deleteOneById(groupId1) }
    }
    @Test fun `deleteOneById WHEN id exists in database THEN delete a scenario group`() {
        // GIVEN
        justRun { scenarioGroupDAO.deleteOneById(groupId1) }
        // WHEN
        ScenarioGroupService.deleteOneById(groupId1.toString())
        // THEN
        verify(exactly = 1) { scenarioGroupDAO.deleteOneById(groupId1) }
    }

}