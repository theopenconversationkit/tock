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
import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.bot.admin.scenario.ScenarioVersionDAO
import ai.tock.bot.admin.scenario.ScenarioVersionState
import ai.tock.shared.exception.scenario.version.ScenarioVersionNotFoundException
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.toId
import java.time.ZonedDateTime
import kotlin.test.assertEquals

class ScenarioVersionServiceTest {

    private val dateNow = ZonedDateTime.parse("2022-01-01T00:00:00.000Z")
    private val groupId1 = "groupId1".toId<ScenarioGroup>()
    private val groupId2 = "groupId2".toId<ScenarioGroup>()
    private val versionId1 = "versionId1".toId<ScenarioVersion>()
    private val versionId2 = "versionId2".toId<ScenarioVersion>()
    private val versionId3 = "versionId3".toId<ScenarioVersion>()

    private val scenarioVersion1 = ScenarioVersion(
        _id = versionId1, scenarioGroupId = groupId1, data = "DATA1",
        state = ScenarioVersionState.DRAFT, comment = "v1", creationDate = dateNow, updateDate = dateNow
    )
    private val scenarioVersion2 = ScenarioVersion(
        _id = versionId2, scenarioGroupId = groupId1, data = "DATA2",
        state = ScenarioVersionState.DRAFT, comment = "v2", creationDate = dateNow, updateDate = dateNow
    )
    private val scenarioVersion3 = ScenarioVersion(
        _id = versionId3, scenarioGroupId = groupId2, data = "DATA3",
        state = ScenarioVersionState.CURRENT, comment = "v1", creationDate = dateNow, updateDate = dateNow
    )
    private val scenarioVersion3Copy = scenarioVersion3.copy(
        data = "DATA-COPY", comment = "comment-Copy",
        state = ScenarioVersionState.ARCHIVED
    )

    companion object {
        private val scenarioVersionDAO: ScenarioVersionDAO = mockk(relaxed = true)

        init {
            tockInternalInjector = KodeinInjector()
            val module = Kodein.Module {
                bind<ScenarioVersionDAO>() with provider { scenarioVersionDAO }
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
    fun `createMany WHEN creating many THEN return created scenario versions`() {
        // GIVEN
        val versions = listOf(scenarioVersion1, scenarioVersion2, scenarioVersion3)
        every { scenarioVersionDAO.createMany(versions) } returns versions
        // WHEN
        val result = ScenarioVersionService.createMany(versions)
        // THEN
        assertEquals(versions, result)
        verify(exactly = 1) { scenarioVersionDAO.createMany(versions) }
    }

    @Test
    fun `createOne WHEN creating one THEN return created scenario version`() {
        // GIVEN
        every { scenarioVersionDAO.createOne(scenarioVersion1) } returns scenarioVersion1
        // WHEN
        val result = ScenarioVersionService.createOne(scenarioVersion1)
        // THEN
        assertEquals(scenarioVersion1, result)
        verify(exactly = 1) { scenarioVersionDAO.createOne(scenarioVersion1) }
    }

    @Test
    fun `findAllByScenarioGroupIdAndState WHEN 2 scenario draft versions exists THEN return 2 scenario versions`() {
        // GIVEN
        val versions = listOf(scenarioVersion1, scenarioVersion2)
        every {
            scenarioVersionDAO.findAllByScenarioGroupIdAndState(
                groupId1,
                ScenarioVersionState.DRAFT
            )
        } returns versions
        // WHEN
        val result =
            ScenarioVersionService.findAllByScenarioGroupIdAndState(groupId1.toString(), ScenarioVersionState.DRAFT)
        // THEN
        assertEquals(versions, result)
        verify(exactly = 1) {
            scenarioVersionDAO.findAllByScenarioGroupIdAndState(
                groupId1,
                ScenarioVersionState.DRAFT
            )
        }
    }

    @Test
    fun `findOneById WHEN id exists in database THEN returns scenario version`() {
        // GIVEN
        every { scenarioVersionDAO.findOneById(versionId1) } returns scenarioVersion1
        // WHEN
        val result = ScenarioVersionService.findOneById(versionId1.toString())
        // THEN
        assertEquals(scenarioVersion1, result)
        verify(exactly = 1) { scenarioVersionDAO.findOneById(versionId1) }
    }

    @Test
    fun `findOneById WHEN id does not exists in database THEN throw exception`() {
        // GIVEN
        every { scenarioVersionDAO.findOneById(versionId1) } returns null
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            ScenarioVersionService.findOneById(versionId1.toString())
        }
        verify(exactly = 1) { scenarioVersionDAO.findOneById(versionId1) }
    }

    @Test
    fun `countByScenarioGroupId WHEN 10 scenario versions matching the group id exists THEN count 10`() {
        // GIVEN
        every { scenarioVersionDAO.countAllByScenarioGroupId(groupId1) } returns 10
        // WHEN
        val result = ScenarioVersionService.countAllByScenarioGroupId(groupId1.toString())
        // THEN
        assertEquals(10, result)
        verify(exactly = 1) { scenarioVersionDAO.countAllByScenarioGroupId(groupId1) }
    }

    @Test
    fun `updateOne WHEN id exists in database THEN update a scenario version`() {
        // GIVEN
        every { scenarioVersionDAO.updateOne(scenarioVersion3Copy) } returns scenarioVersion3Copy
        // WHEN
        val result = ScenarioVersionService.updateOne(scenarioVersion3Copy)
        // THEN
        assertEquals(scenarioVersion3Copy, result)
        verify(exactly = 1) { scenarioVersionDAO.updateOne(scenarioVersion3Copy) }
    }

    @Test
    fun `updateOne WHEN id does not exists in database THEN throw exception`() {
        // GIVEN
        every { scenarioVersionDAO.updateOne(scenarioVersion3Copy) } throws ScenarioVersionNotFoundException(versionId1.toString())
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            ScenarioVersionService.updateOne(scenarioVersion3Copy)
        }
        verify(exactly = 1) { scenarioVersionDAO.updateOne(scenarioVersion3Copy) }
    }

    @Test
    fun `deleteAllByScenarioGroupId WHEN scenario versions matching the group id exists in database THEN delete those scenario versions` () {
        // GIVEN
        justRun { scenarioVersionDAO.deleteAllByScenarioGroupId(groupId1) }
        // WHEN
        ScenarioVersionService.deleteAllByScenarioGroupId(groupId1.toString())
        // THEN
        verify(exactly = 1) { scenarioVersionDAO.deleteAllByScenarioGroupId(groupId1) }
    }

    @Test
    fun `deleteAllByScenarioGroupId WHEN scenario versions matching the group id do not exist in the database THEN throw exception`() {
        // GIVEN
        every { scenarioVersionDAO.deleteAllByScenarioGroupId(groupId1) } throws ScenarioVersionNotFoundException(
            versionId1.toString()
        )
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            ScenarioVersionService.deleteAllByScenarioGroupId(groupId1.toString())
        }
        verify(exactly = 1) { scenarioVersionDAO.deleteAllByScenarioGroupId(groupId1) }
    }

    @Test
    fun `deleteOneById WHEN id exists in database THEN delete a scenario version`() {
        // GIVEN
        justRun { scenarioVersionDAO.deleteOneById(versionId1) }
        // WHEN
        ScenarioVersionService.deleteOneById(versionId1.toString())
        // THEN
        verify(exactly = 1) { scenarioVersionDAO.deleteOneById(versionId1) }
    }

    @Test
    fun `deleteOneById WHEN id does not exists in database THEN throw exception`() {
        // GIVEN
        every { scenarioVersionDAO.deleteOneById(versionId1) } throws ScenarioVersionNotFoundException(versionId1.toString())
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            ScenarioVersionService.deleteOneById(versionId1.toString())
        }
        verify(exactly = 1) { scenarioVersionDAO.deleteOneById(versionId1) }
    }

}