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
import ai.tock.bot.admin.scenario.ScenarioVersionState
import ai.tock.bot.admin.service.impl.ScenarioServiceImpl
import ai.tock.shared.exception.scenario.group.ScenarioGroupAndVersionMismatchException
import ai.tock.shared.exception.scenario.group.ScenarioGroupDuplicatedException
import ai.tock.shared.exception.scenario.group.ScenarioGroupNotFoundException
import ai.tock.shared.exception.scenario.group.ScenarioGroupWithoutVersionException
import ai.tock.shared.exception.scenario.version.ScenarioVersionBadStateException
import ai.tock.shared.exception.scenario.version.ScenarioVersionNotFoundException
import ai.tock.shared.exception.scenario.version.ScenarioVersionsInconsistentException
import ai.tock.shared.injector
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
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
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScenarioServiceTest {

    private val dateNow = ZonedDateTime.parse("2022-01-01T00:00:00.000Z")

    private val namespace = "namespace_app"
    private val botId1 = "botId1"
    private val botId2 = "botId2"

    private val groupId1 = "groupId1"
    private val groupId2 = "groupId2"
    private val groupId3 = "groupId3"
    private val versionId1 = "versionId1"
    private val versionId2 = "versionId2"
    private val versionId3 = "versionId3"
    private val versionId4 = "versionId4"
    private val versionId5 = "versionId5"

    private val scenarioVersion1 = ScenarioVersion(_id = versionId1.toId(), scenarioGroupId = groupId1.toId(), data = "DATA1",
        state = ScenarioVersionState.DRAFT, comment = "v1", creationDate = dateNow, updateDate = dateNow)
    private val scenarioVersion1Copy1 = ScenarioVersion(_id = versionId1.toId(), scenarioGroupId = groupId1.toId(), data = "DATA1-COPYYYY",
        state = ScenarioVersionState.DRAFT, comment = "v1", creationDate = dateNow, updateDate = dateNow)
    private val scenarioVersion1Copy2 = ScenarioVersion(_id = versionId1.toId(), scenarioGroupId = groupId1.toId(), data = "DATA1-COPYYYY",
        state = ScenarioVersionState.CURRENT, comment = "v1", creationDate = dateNow, updateDate = dateNow)
    private val scenarioVersion2 = ScenarioVersion(_id = versionId2.toId(), scenarioGroupId = groupId1.toId(), data = "DATA2",
        state = ScenarioVersionState.DRAFT, comment = "v2", creationDate = dateNow, updateDate = dateNow)
    private val scenarioVersion3 = ScenarioVersion(_id = versionId3.toId(), scenarioGroupId = groupId2.toId(), data = "DATA3",
        state = ScenarioVersionState.CURRENT, comment = "v1", creationDate = dateNow, updateDate = dateNow)
    private val scenarioVersion4 = ScenarioVersion(_id = versionId4.toId(), scenarioGroupId = groupId2.toId(), data = "DATA3",
        state = ScenarioVersionState.DRAFT, comment = "v1", creationDate = dateNow, updateDate = dateNow)
    private val scenarioVersion5 = ScenarioVersion(_id = versionId5.toId(), scenarioGroupId = groupId2.toId(), data = "DATA3",
        state = ScenarioVersionState.ARCHIVED, comment = "v1", creationDate = dateNow, updateDate = dateNow)

    private val scenarioGroup1 = ScenarioGroup(_id = groupId1.toId(), botId = botId1, name = "name1", creationDate = dateNow, updateDate = dateNow,
        versions = listOf(scenarioVersion1, scenarioVersion2))
    private val scenarioGroup1Copy = ScenarioGroup(_id = groupId1.toId(), botId = botId1, name = "name1", creationDate = dateNow, updateDate = dateNow,
        versions = listOf(scenarioVersion1, scenarioVersion2), description = "DESC-COPY")
    private val scenarioGroup2 = ScenarioGroup(_id = groupId2.toId(), botId = botId2, name = "name2", creationDate = dateNow, updateDate = dateNow,
        versions = listOf(scenarioVersion3))

    private val scenarioService: ScenarioService by injector.instance()

    companion object {
        private val scenarioGroupService: ScenarioGroupService = mockk(relaxed = true)
        private val scenarioVersionService: ScenarioVersionService = mockk(relaxed = true)
        private val storyService: StoryService = mockk(relaxed = true)

        init {
            tockInternalInjector = KodeinInjector()
            val module = Kodein.Module {
                bind<ScenarioGroupService>() with singleton { scenarioGroupService }
                bind<ScenarioVersionService>() with singleton { scenarioVersionService }
                bind<ScenarioService>() with singleton { ScenarioServiceImpl() }
                bind<StoryService>() with singleton { storyService }
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

    @Test fun `importOneScenarioGroup WHEN has no versions THEN throw exception`() {
        // GIVEN
        val scenarioGroupWithoutVersions = scenarioGroup1.copy(versions = emptyList())
        // WHEN // THEN
        assertThrows<ScenarioGroupWithoutVersionException> {
            scenarioService.importOneScenarioGroup(scenarioGroupWithoutVersions)
        }
        verify(exactly = 0) { scenarioGroupService.createOne(any()) }
        verify(exactly = 0) { scenarioVersionService.createMany(any())}
    }

    @Test fun `importOneScenarioGroup WHEN has versions with multiple scenario group THEN throw exception`() {
        // GIVEN
        val scenarioGroupWithIncorrecteVersions = scenarioGroup1.copy(versions = listOf(scenarioVersion1, scenarioVersion3))
        // WHEN // THEN
        assertThrows<ScenarioVersionsInconsistentException> {
            scenarioService.importOneScenarioGroup(scenarioGroupWithIncorrecteVersions)
        }
        verify(exactly = 0) { scenarioGroupService.createOne(any()) }
        verify(exactly = 0) { scenarioVersionService.createMany(any())}
    }

    @Test fun `importOneScenarioGroup WHEN has current version THEN throw exception`() {
        // GIVEN : scenarioGroup2 has a current version
        // WHEN // THEN
        assertThrows<ScenarioVersionBadStateException> {
            scenarioService.importOneScenarioGroup(scenarioGroup2)
        }
        verify(exactly = 0) { scenarioGroupService.createOne(any()) }
        verify(exactly = 0) { scenarioVersionService.createMany(any())}
    }

    @Test fun `importOneScenarioGroup WHEN scenario group name exist THEN throw exception`() {
        // GIVEN
        every { scenarioGroupService.createOne(scenarioGroup1) } throws ScenarioGroupDuplicatedException()
        // WHEN // THEN
        assertThrows<ScenarioGroupDuplicatedException> {
            scenarioService.importOneScenarioGroup(scenarioGroup1)
        }
        verify(exactly = 1) { scenarioGroupService.createOne(scenarioGroup1) }
        verify(exactly = 0) { scenarioVersionService.createMany(any())}
    }

    @Test fun `importOneScenarioGroup WHEN the name of the scenario group does not exist and has versions without current THEN creat the group and its versions`() {
        // GIVEN
        every { scenarioGroupService.createOne(scenarioGroup1) } returns scenarioGroup1
        every { scenarioVersionService.createMany(any()) } returnsArgument 0
        // WHEN
        val result = scenarioService.importOneScenarioGroup(scenarioGroup1)
        // THEN
        assertEquals(result, scenarioGroup1)
        verify(exactly = 1) { scenarioGroupService.createOne(scenarioGroup1) }
        verify(exactly = 1) { scenarioVersionService.createMany(scenarioGroup1.versions)}
    }

    @Test fun `importManyScenarioVersion WHEN scenario group not exists THEN throw exception`() {
        // GIVEN
        val versions = listOf(scenarioVersion1, scenarioVersion2)
        every { scenarioGroupService.findOneById(groupId1) } throws ScenarioGroupNotFoundException(groupId1)        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            scenarioService.importManyScenarioVersion(versions)
        }
        verify(exactly = 1) { scenarioGroupService.findOneById(groupId1) }
        verify(exactly = 0) { scenarioVersionService.createMany(any())}
    }

    @Test fun `importManyScenarioVersion WHEN scenario group exists and versions are valid (not empty, same scenario group and no current) THEN create and return created versions`() {
        // GIVEN
        val versions = listOf(scenarioVersion1, scenarioVersion2)
        every { scenarioGroupService.findOneById(groupId1) } returns scenarioGroup1
        every { scenarioVersionService.createMany(any()) } returnsArgument 0
        // WHEN
        val result = scenarioService.importManyScenarioVersion(versions)
        // THEN
        assertEquals(result, versions)
        verify(exactly = 1) { scenarioGroupService.findOneById(groupId1) }
        verify(exactly = 1) { scenarioVersionService.createMany(versions)}
    }

    @Test fun `createOneScenarioGroup WHEN the scenario group name exists THEN throws exception`() {
        // GIVEN
        every { scenarioGroupService.createOne(scenarioGroup1) } throws ScenarioGroupDuplicatedException()
        // WHEN // THEN
        assertThrows<ScenarioGroupDuplicatedException> {
            scenarioService.createOneScenarioGroup(scenarioGroup1)
        }
        verify(exactly = 1) { scenarioGroupService.createOne(scenarioGroup1) }
        verify(exactly = 0) { scenarioVersionService.createOne(any())}
    }

    @Test fun `createOneScenarioGroup WHEN the scenario group name not exists THEN create and return a scenario group created and its initial version`() {
        // GIVEN
        val scenarioGroupWithoutVersions = scenarioGroup1.copy(versions = emptyList())
        every { scenarioGroupService.createOne(scenarioGroupWithoutVersions) } returns scenarioGroupWithoutVersions
        every { scenarioVersionService.createOne(any()) } returnsArgument 0
        // WHEN
        val result = scenarioService.createOneScenarioGroup(scenarioGroupWithoutVersions)
        // THEN
        // assert not equals because result (created scenario group) has an initial version
        assertNotEquals(scenarioGroupWithoutVersions, result)
        // assert equals without comparing versions
        assertEquals(scenarioGroupWithoutVersions, result.copy(versions = emptyList()))
        assertEquals(1, result.versions.size)
        assertEquals(ScenarioVersionState.DRAFT, result.versions.first().state)
        verify(exactly = 1) { scenarioGroupService.createOne(scenarioGroupWithoutVersions) }
        verify(exactly = 1) { scenarioVersionService.createOne(any())}
    }

    @Test fun `createOneScenarioVersion WHEN the scenario group not exists THEN throws exception`() {
        // GIVEN
        every { scenarioGroupService.findOneById(groupId1) } throws ScenarioGroupNotFoundException(groupId1)
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            scenarioService.createOneScenarioVersion(scenarioVersion1)
        }
        verify(exactly = 1) { scenarioGroupService.findOneById(groupId1) }
        verify(exactly = 0) { scenarioVersionService.createOne(any()) }
    }

    @Test fun `createOneScenarioVersion WHEN the scenario group exists THEN create and return a created scenario version`() {
        // GIVEN
        every { scenarioGroupService.findOneById(groupId1) } returns scenarioGroup1
        every { scenarioVersionService.createOne(scenarioVersion1) } returns scenarioVersion1
        // WHEN
        val result = scenarioService.createOneScenarioVersion(scenarioVersion1)
        assertEquals(scenarioVersion1, result)
        // THEN
        verify(exactly = 1) { scenarioGroupService.findOneById(groupId1) }
        verify(exactly = 1) { scenarioVersionService.createOne(any()) }
    }

    @Test fun `findAllScenarioGroupWithVersionsByBotId WHEN no scenario group found THEN return empty list`() {
        // GIVEN
        every { scenarioGroupService.findAllByBotId(botId1) } returns emptyList()
        // WHEN
        val result = scenarioService.findAllScenarioGroupWithVersionsByBotId(botId1)
        assertTrue(result.isEmpty())
        // THEN
        verify(exactly = 1) { scenarioGroupService.findAllByBotId(botId1) }
    }

    @Test fun `findAllScenarioGroupWithVersionsByBotId WHEN 2 scenario groups was found THEN return the list of group and check presence of versions`() {
        // GIVEN
        every { scenarioGroupService.findAllByBotId(botId1) } returns listOf(scenarioGroup1, scenarioGroup2)
        // WHEN
        val result = scenarioService.findAllScenarioGroupWithVersionsByBotId(botId1)
        assertFalse(result.isEmpty())
        assertNull(result.firstOrNull { group -> group.versions.isEmpty() })
        // THEN
        verify(exactly = 1) { scenarioGroupService.findAllByBotId(botId1) }
    }

    @Test fun `findOneScenarioVersion WHEN the scenario version not exists THEN throws exception`() {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId1) } throws ScenarioVersionNotFoundException(versionId1)
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            scenarioService.findOneScenarioVersion(groupId1, versionId1)
        }
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
    }

    @Test fun `findOneScenarioVersion WHEN the scenario version exists but not match with group id THEN throws exception`() {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        // WHEN // THEN
        assertThrows<ScenarioGroupAndVersionMismatchException> {
            scenarioService.findOneScenarioVersion(groupId2, versionId1)
        }
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
    }

    @Test fun `findOneScenarioGroup WHEN the scenario group not exists THEN throws exception`() {
        // GIVEN
        every { scenarioGroupService.findOneById(groupId1) } throws ScenarioGroupNotFoundException(groupId1)
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            scenarioService.findOneScenarioGroup(groupId1)
        }
        verify(exactly = 1) { scenarioGroupService.findOneById(groupId1) }
    }

    @Test fun `findOneScenarioGroup WHEN the scenario group exists THEN return the scenario group found`() {
        // GIVEN
        every { scenarioGroupService.findOneById(groupId1) } returns scenarioGroup1
        // WHEN
        val result = scenarioService.findOneScenarioGroup(groupId1)
        // THEN
        assertEquals(scenarioGroup1, result)
        verify(exactly = 1) { scenarioGroupService.findOneById(groupId1) }
    }

    @Test fun `updateOneScenarioGroup WHEN the scenario group not exists THEN throws exception`() {
        // GIVEN
        every { scenarioGroupService.updateOne(scenarioGroup1Copy) } throws ScenarioGroupNotFoundException(groupId1)
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            scenarioService.updateOneScenarioGroup(scenarioGroup1Copy)
        }
        verify(exactly = 0) { scenarioGroupService.updateOne(scenarioGroup1) }
    }

    @Test fun `updateOneScenarioGroup WHEN the scenario group exists THEN update and return the scenario group updated`() {
        // GIVEN
        every { scenarioGroupService.updateOne(scenarioGroup1Copy) } returns scenarioGroup1Copy
        // WHEN
        val result = scenarioService.updateOneScenarioGroup(scenarioGroup1Copy)
        // THEN
        assertEquals(scenarioGroup1Copy, result)
        verify(exactly = 1) { scenarioGroupService.updateOne(scenarioGroup1Copy) }
    }

    @Test fun `updateOneScenarioVersion WHEN the scenario version not exists THEN throws exception`() {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId1) } throws ScenarioVersionNotFoundException(versionId1)
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            scenarioService.updateOneScenarioVersion(scenarioVersion1)
        }
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { scenarioVersionService.updateOne(any()) }
        verify(exactly = 0) { scenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
    }

    @Test fun `updateOneScenarioVersion WHEN the scenario version that is not part of the group THEN throws exception`() {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId1) } returns scenarioVersion1.copy(scenarioGroupId = groupId2.toId())
        // WHEN // THEN
        assertThrows<ScenarioGroupAndVersionMismatchException> {
            scenarioService.updateOneScenarioVersion(scenarioVersion1)
        }
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { scenarioVersionService.updateOne(any()) }
        verify(exactly = 0) { scenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
    }

    @Test fun `updateOneScenarioVersion WHEN the scenario version exists with CURRENT state THEN throws exception`() {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId1) } returns scenarioVersion1Copy2
        // WHEN // THEN
        assertThrows<ScenarioVersionBadStateException> {
            scenarioService.updateOneScenarioVersion(scenarioVersion1)
        }
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { scenarioVersionService.updateOne(any()) }
        verify(exactly = 0) { scenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
    }

    @Test fun `updateOneScenarioVersion WHEN the scenario version exists with ARCHIVED state THEN throws exception`() {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId5) } returns scenarioVersion5
        // WHEN // THEN
        assertThrows<ScenarioVersionBadStateException> {
            scenarioService.updateOneScenarioVersion(scenarioVersion5)
        }
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId5) }
        verify(exactly = 0) { scenarioVersionService.updateOne(any()) }
        verify(exactly = 0) { scenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
    }

    @Test fun `updateOneScenarioVersion to DRAFT WHEN the scenario version exists with DRAFT state and is part of its group and there is no current version for the group THEN update and return the scenario version updated`() {
        // GIVEN
        val scenarioVersion4CopyArchived = scenarioVersion4.copy(state = ScenarioVersionState.ARCHIVED)
        every { scenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        every { scenarioVersionService.updateOne(scenarioVersion1Copy1)  } returns scenarioVersion1Copy1
        // WHEN
        val result = scenarioService.updateOneScenarioVersion(scenarioVersion1Copy1)
        // THEN
        assertEquals(scenarioVersion1Copy1, result)
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { scenarioVersionService.updateOne(scenarioVersion1) }
        verify(exactly = 0) { scenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
        verify(exactly = 0) { scenarioVersionService.updateOne(scenarioVersion4CopyArchived) }
    }

    @Test fun `updateOneScenarioVersion to CURRENT WHEN the scenario version exists with DRAFT state and is part of its group and there is no current version for the group THEN update and return the scenario version updated`() {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        every { scenarioVersionService.findAllByScenarioGroupIdAndState(any(), any()) } returns emptyList()
        every { scenarioVersionService.updateOne(scenarioVersion1Copy2)  } returns scenarioVersion1Copy2
        // WHEN
        val result = scenarioService.updateOneScenarioVersion(scenarioVersion1Copy2)
        // THEN
        assertEquals(scenarioVersion1Copy2, result)
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
        verify(exactly = 1) { scenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
        verify(exactly = 1) { scenarioVersionService.updateOne(scenarioVersion1Copy2) }
    }

    @Test fun `updateOneScenarioVersion to CURRENT WHEN the scenario version exists with DRAFT state and is part of its group and there is a current version for the group THEN update and return the scenario version updated`() {
        // GIVEN
        val scenarioVersion4CopyArchived = scenarioVersion4.copy(state = ScenarioVersionState.ARCHIVED)
        every { scenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        every { scenarioVersionService.findAllByScenarioGroupIdAndState(groupId1,
            ScenarioVersionState.CURRENT) } returns listOf(scenarioVersion4)
        every { scenarioVersionService.updateOne(scenarioVersion1Copy2)  } returns scenarioVersion1Copy2
        every { scenarioVersionService.updateOne(scenarioVersion4CopyArchived)  } returns scenarioVersion4CopyArchived
        // WHEN
        val result = scenarioService.updateOneScenarioVersion(scenarioVersion1Copy2)
        // THEN
        assertEquals(scenarioVersion1Copy2, result)
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
        verify(exactly = 1) { scenarioVersionService.findAllByScenarioGroupIdAndState(groupId1,
            ScenarioVersionState.CURRENT)  }
        verify(exactly = 1) { scenarioVersionService.updateOne(scenarioVersion1Copy2) }
        verify(exactly = 1) { scenarioVersionService.updateOne(scenarioVersion4CopyArchived) }
    }

    @Test fun `deleteOneScenarioGroup WHEN the scenario group and its versions exist THEN delete the tick story, the group and its versions and return true`() {
        // GIVEN
        every { scenarioGroupService.findOneById(groupId1) } returns scenarioGroup1
        every { storyService.deleteStoryByStoryId(namespace, botId1, groupId2) } returns true
        justRun { scenarioVersionService.deleteAllByScenarioGroupId(groupId1) }
        justRun { scenarioGroupService.deleteOneById(groupId1) }
        // WHEN
        val result = scenarioService.deleteOneScenarioGroup(namespace, botId1, groupId1)
        // THEN
        assertTrue(result)
        verify(exactly = 1) { scenarioGroupService.findOneById(groupId1) }
        verify(exactly = 1)  { scenarioVersionService.deleteAllByScenarioGroupId(groupId1) }
        verify(exactly = 1) { storyService.deleteStoryByStoryId(namespace, botId1, groupId1) }
        verify(exactly = 1)  { scenarioGroupService.deleteOneById(groupId1) }
    }

    @Test fun `deleteOneScenarioGroup WHEN the scenario group does not exists THEN return false`() {
        // GIVEN
        every { scenarioGroupService.findOneById(groupId1) } throws ScenarioGroupNotFoundException(groupId1)
        every { storyService.deleteStoryByStoryId(namespace, botId1, groupId2) } returns true
        justRun { scenarioVersionService.deleteAllByScenarioGroupId(groupId1) }
        justRun { scenarioGroupService.deleteOneById(groupId1) }
        // WHEN
        val result = scenarioService.deleteOneScenarioGroup(namespace, botId1, groupId1)
        // THEN
        assertFalse(result)
        verify(exactly = 1) { scenarioGroupService.findOneById(groupId1) }
        verify(exactly = 0)  { scenarioVersionService.deleteAllByScenarioGroupId(groupId1) }
        verify(exactly = 0) { storyService.deleteStoryByStoryId(namespace, botId1, groupId1) }
        verify(exactly = 0)  { scenarioGroupService.deleteOneById(groupId1) }
    }

    @Test fun `deleteOneScenarioGroup WHEN the scenario group versions does not exists THEN return false`() {
        // GIVEN
        every { scenarioGroupService.findOneById(groupId1) } returns scenarioGroup1
        every { storyService.deleteStoryByStoryId(namespace, botId1, groupId2) } returns true
        every { scenarioVersionService.deleteAllByScenarioGroupId(groupId1) } throws ScenarioVersionNotFoundException()
        justRun { scenarioGroupService.deleteOneById(groupId1) }
        // WHEN
        val result = scenarioService.deleteOneScenarioGroup(namespace, botId1, groupId1)
        // THEN
        assertFalse(result)
        verify(exactly = 1) { scenarioGroupService.findOneById(groupId1) }
        verify(exactly = 1)  { scenarioVersionService.deleteAllByScenarioGroupId(groupId1) }
        verify(exactly = 0) { storyService.deleteStoryByStoryId(namespace, botId1, groupId1) }
        verify(exactly = 0)  { scenarioGroupService.deleteOneById(groupId1) }
    }



    @Test fun `deleteOneScenarioVersion a draft version WHEN it exists and is a part of scenario group but not the last one THEN delete the version but not its group nor the tick story and return true` () {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        justRun { scenarioVersionService.deleteOneById(versionId1) }
        every { scenarioVersionService.countAllByScenarioGroupId(groupId1) } returns 2
        // WHEN
        val result = scenarioService.deleteOneScenarioVersion(namespace, botId1, groupId1, versionId1)
        // THEN
        assertTrue(result)
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { storyService.deleteStoryByStoryId(namespace, botId1, groupId1) }
        verify(exactly = 1) { scenarioVersionService.deleteOneById(versionId1) }
        verify(exactly = 1) { scenarioVersionService.countAllByScenarioGroupId(groupId1) }
        verify(exactly = 0) { scenarioGroupService.deleteOneById(groupId1) }
    }

    @Test fun `deleteOneScenarioVersion a current version WHEN it exists and is a part of scenario group but not the last one THEN delete the tick story, the version but not its group and return true` () {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId3) } returns scenarioVersion3
        justRun { scenarioVersionService.deleteOneById(versionId3) }
        every { storyService.deleteStoryByStoryId(namespace, botId1, groupId2) } returns true
        every { scenarioVersionService.countAllByScenarioGroupId(groupId2) } returns 2
        // WHEN
        val result = scenarioService.deleteOneScenarioVersion(namespace, botId1, groupId2, versionId3)
        // THEN
        assertTrue(result)
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId3) }
        verify(exactly = 1) { storyService.deleteStoryByStoryId(namespace, botId1, groupId2) }
        verify(exactly = 1) { scenarioVersionService.deleteOneById(versionId3) }
        verify(exactly = 1) { scenarioVersionService.countAllByScenarioGroupId(groupId2) }
        verify(exactly = 0) { scenarioGroupService.deleteOneById(groupId2) }
    }

    @Test fun `deleteOneScenarioVersion a draft version WHEN it exists and is a part of scenario group and is the last one THEN delete the version and its group and return true` () {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        justRun { scenarioVersionService.deleteOneById(versionId1) }
        every { scenarioVersionService.countAllByScenarioGroupId(groupId1) } returns 0
        justRun { scenarioGroupService.deleteOneById(groupId1) }
        // WHEN
        val result = scenarioService.deleteOneScenarioVersion(namespace, botId1, groupId1, versionId1)
        // THEN
        assertTrue(result)
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { storyService.deleteStoryByStoryId(namespace, botId1, groupId1) }
        verify(exactly = 1) { scenarioVersionService.deleteOneById(versionId1) }
        verify(exactly = 1) { scenarioVersionService.countAllByScenarioGroupId(groupId1) }
        verify(exactly = 1) { scenarioGroupService.deleteOneById(groupId1) }
    }

    @Test fun `deleteOneScenarioVersion a draft version WHEN it dose not exists THEN return false` () {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId1) } throws ScenarioVersionNotFoundException(versionId1)
        // WHEN
        val result = scenarioService.deleteOneScenarioVersion(namespace, botId1, groupId1, versionId1)
        // THEN
        assertFalse(result)
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { storyService.deleteStoryByStoryId(namespace, botId1, groupId1) }
        verify(exactly = 0) { scenarioVersionService.deleteOneById(versionId1) }
        verify(exactly = 0) { scenarioVersionService.countAllByScenarioGroupId(groupId1) }
        verify(exactly = 0) { scenarioGroupService.deleteOneById(groupId1) }
    }

    @Test fun `deleteOneScenarioVersion a draft version WHEN it exists and is not a part of scenario group THEN return false` () {
        // GIVEN
        every { scenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        // WHEN
        val result = scenarioService.deleteOneScenarioVersion(namespace, botId1, groupId2, versionId1)
        // THEN
        assertFalse(result)
        verify(exactly = 1) { scenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { storyService.deleteStoryByStoryId(namespace, botId1, groupId2) }
        verify(exactly = 0) { scenarioVersionService.deleteOneById(versionId1) }
        verify(exactly = 0) { scenarioVersionService.countAllByScenarioGroupId(groupId1) }
        verify(exactly = 0) { scenarioGroupService.deleteOneById(groupId1) }
    }
}