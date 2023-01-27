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
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
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
import com.github.salomonbrys.kodein.instance
import io.mockk.*
import org.junit.Before
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.toId
import java.time.ZonedDateTime
import kotlin.test.*

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
        versions = listOf(scenarioVersion1, scenarioVersion2), enabled = false)
    private val scenarioGroup1Copy = ScenarioGroup(_id = groupId1.toId(), botId = botId1, name = "name1", creationDate = dateNow, updateDate = dateNow,
        versions = listOf(scenarioVersion1, scenarioVersion2), description = "DESC-COPY", enabled = false)
    private val scenarioGroup2 = ScenarioGroup(_id = groupId2.toId(), botId = botId2, name = "name2", creationDate = dateNow, updateDate = dateNow,
        versions = listOf(scenarioVersion3), enabled = false)

    @AfterEach
    fun clearMockk() {
        clearAllMocks()
    }

    @Test fun `importOneScenarioGroup WHEN has no versions THEN throw exception`() {
        // GIVEN
        val scenarioGroupWithoutVersions = scenarioGroup1.copy(versions = emptyList())
        // WHEN // THEN
        assertThrows<ScenarioGroupWithoutVersionException> {
            ScenarioService.importOneScenarioGroup(scenarioGroupWithoutVersions)
        }
        verify(exactly = 0) { ScenarioGroupService.createOne(any()) }
        verify(exactly = 0) { ScenarioVersionService.createMany(any())}
    }

    @Test fun `importOneScenarioGroup WHEN has versions with multiple scenario group THEN throw exception`() {
        // GIVEN
        val scenarioGroupWithIncorrecteVersions = scenarioGroup1.copy(versions = listOf(scenarioVersion1, scenarioVersion3))
        // WHEN // THEN
        assertThrows<ScenarioVersionsInconsistentException> {
            ScenarioService.importOneScenarioGroup(scenarioGroupWithIncorrecteVersions)
        }
        verify(exactly = 0) { ScenarioGroupService.createOne(any()) }
        verify(exactly = 0) { ScenarioVersionService.createMany(any())}
    }

    @Test fun `importOneScenarioGroup WHEN has current version THEN throw exception`() {
        // GIVEN : scenarioGroup2 has a current version
        // WHEN // THEN
        assertThrows<ScenarioVersionBadStateException> {
            ScenarioService.importOneScenarioGroup(scenarioGroup2)
        }
        verify(exactly = 0) { ScenarioGroupService.createOne(any()) }
        verify(exactly = 0) { ScenarioVersionService.createMany(any())}
    }

    @Test fun `importOneScenarioGroup WHEN scenario group name exist THEN throw exception`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        every { ScenarioGroupService.createOne(scenarioGroup1) } throws ScenarioGroupDuplicatedException()
        // WHEN // THEN
        assertThrows<ScenarioGroupDuplicatedException> {
            ScenarioService.importOneScenarioGroup(scenarioGroup1)
        }
        verify(exactly = 1) { ScenarioGroupService.createOne(scenarioGroup1) }
        verify(exactly = 0) { ScenarioVersionService.createMany(any())}
    }

    @Test fun `importOneScenarioGroup WHEN the name of the scenario group does not exist and has versions without current THEN creat the group and its versions`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        mockkObject(ScenarioVersionService)
        every { ScenarioGroupService.createOne(scenarioGroup1) } returns scenarioGroup1
        every { ScenarioVersionService.createMany(any()) } returnsArgument 0
        // WHEN
        val result = ScenarioService.importOneScenarioGroup(scenarioGroup1)
        // THEN
        assertEquals(result, scenarioGroup1)
        verify(exactly = 1) { ScenarioGroupService.createOne(scenarioGroup1) }
        verify(exactly = 1) { ScenarioVersionService.createMany(scenarioGroup1.versions)}
    }

    @Test fun `importManyScenarioVersion WHEN scenario group not exists THEN throw exception`() {
        // GIVEN
        val versions = listOf(scenarioVersion1, scenarioVersion2)
        mockkObject(ScenarioGroupService)
        every { ScenarioGroupService.findOneById(groupId1) } throws ScenarioGroupNotFoundException(groupId1)        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            ScenarioService.importManyScenarioVersion(namespace, versions)
        }
        verify(exactly = 1) { ScenarioGroupService.findOneById(groupId1) }
        verify(exactly = 0) { ScenarioVersionService.createMany(any())}
    }

    @Test fun `importManyScenarioVersion WHEN scenario group exists and versions are valid (not empty, same scenario group and no current) THEN create and return created versions`() {
        // GIVEN
        val versions = listOf(scenarioVersion1, scenarioVersion2)
        mockkObject(ScenarioGroupService)
        every { ScenarioGroupService.findOneById(groupId1) } returns scenarioGroup1
        every { ScenarioVersionService.createMany(any()) } returnsArgument 0
        // WHEN
        val result = ScenarioService.importManyScenarioVersion(namespace, versions)
        // THEN
        assertEquals(result, versions)
        verify(exactly = 1) { ScenarioGroupService.findOneById(groupId1) }
        verify(exactly = 1) { ScenarioVersionService.createMany(versions)}
    }

    @Test fun `createOneScenarioGroup WHEN the scenario group name exists THEN throws exception`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        every { ScenarioGroupService.createOne(scenarioGroup1) } throws ScenarioGroupDuplicatedException()
        // WHEN // THEN
        assertThrows<ScenarioGroupDuplicatedException> {
            ScenarioService.createOneScenarioGroup(scenarioGroup1)
        }
        verify(exactly = 1) { ScenarioGroupService.createOne(scenarioGroup1) }
        verify(exactly = 0) { ScenarioVersionService.createOne(any())}
    }

    @Test fun `createOneScenarioGroup WHEN the scenario group name not exists THEN create and return a scenario group created and its initial version`() {
        // GIVEN
        val scenarioGroupWithoutVersions = scenarioGroup1.copy(versions = emptyList())
        mockkObject(ScenarioGroupService)
        mockkObject(ScenarioVersionService)
        every { ScenarioGroupService.createOne(scenarioGroupWithoutVersions) } returns scenarioGroupWithoutVersions
        every { ScenarioVersionService.createOne(any()) } returnsArgument 0
        // WHEN
        val result = ScenarioService.createOneScenarioGroup(scenarioGroupWithoutVersions)
        // THEN
        // assert not equals because result (created scenario group) has an initial version
        assertNotEquals(scenarioGroupWithoutVersions, result)
        // assert equals without comparing versions
        assertEquals(scenarioGroupWithoutVersions, result.copy(versions = emptyList()))
        assertEquals(1, result.versions.size)
        assertEquals(ScenarioVersionState.DRAFT, result.versions.first().state)
        verify(exactly = 1) { ScenarioGroupService.createOne(scenarioGroupWithoutVersions) }
        verify(exactly = 1) { ScenarioVersionService.createOne(any())}
    }

    @Test fun `createOneScenarioVersion WHEN the scenario group not exists THEN throws exception`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        every { ScenarioGroupService.findOneById(groupId1) } throws ScenarioGroupNotFoundException(groupId1)
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            ScenarioService.createOneScenarioVersion(namespace, scenarioVersion1)
        }
        verify(exactly = 1) { ScenarioGroupService.findOneById(groupId1) }
        verify(exactly = 0) { ScenarioVersionService.createOne(any()) }
    }

    @Test fun `createOneScenarioVersion WHEN the scenario group exists THEN create and return a created scenario version`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        mockkObject(ScenarioVersionService)
        every { ScenarioGroupService.findOneById(groupId1) } returns scenarioGroup1
        every { ScenarioVersionService.createOne(scenarioVersion1) } returns scenarioVersion1
        // WHEN
        val result = ScenarioService.createOneScenarioVersion(namespace, scenarioVersion1)
        assertEquals(scenarioVersion1, result)
        // THEN
        verify(exactly = 1) { ScenarioGroupService.findOneById(groupId1) }
        verify(exactly = 1) { ScenarioVersionService.createOne(any()) }
    }

    @Test fun `findAllScenarioGroupWithVersionsByBotId WHEN no scenario group found THEN return empty list`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        every { ScenarioGroupService.findAllByBotId(botId1) } returns emptyList()
        // WHEN
        val result = ScenarioService.findAllScenarioGroupWithVersionsByBotId(namespace, botId1)
        assertTrue(result.isEmpty())
        // THEN
        verify(exactly = 1) { ScenarioGroupService.findAllByBotId(botId1) }
    }

    @Test fun `findAllScenarioGroupWithVersionsByBotId WHEN 2 scenario groups was found THEN return the list of group and check presence of versions`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        mockkObject(StoryService)
        every {
            StoryService.getStoryByNamespaceAndBotIdAndStoryId(any(), any(), any())
        } returns null

        every { ScenarioGroupService.findAllByBotId(botId1) } returns listOf(scenarioGroup1, scenarioGroup2)
        // WHEN
        val result = ScenarioService.findAllScenarioGroupWithVersionsByBotId(namespace, botId1)
        assertFalse(result.isEmpty())
        assertNull(result.firstOrNull { group -> group.versions.isEmpty() })
        // THEN
        verify(exactly = 1) { ScenarioGroupService.findAllByBotId(botId1) }
    }

    @Test fun `findOneScenarioVersion WHEN the scenario version not exists THEN throws exception`() {
        // GIVEN
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } throws ScenarioVersionNotFoundException(versionId1)
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            ScenarioService.findOneScenarioVersion(groupId1, versionId1)
        }
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
    }

    @Test fun `findOneScenarioVersion WHEN the scenario version exists but not match with group id THEN throws exception`() {
        // GIVEN
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        // WHEN // THEN
        assertThrows<ScenarioGroupAndVersionMismatchException> {
            ScenarioService.findOneScenarioVersion(groupId2, versionId1)
        }
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
    }

    @Test fun `findOneScenarioGroup WHEN the scenario group not exists THEN throws exception`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        every { ScenarioGroupService.findOneById(groupId1) } throws ScenarioGroupNotFoundException(groupId1)
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            ScenarioService.findOneScenarioGroup(namespace, groupId1)
        }
        verify(exactly = 1) { ScenarioGroupService.findOneById(groupId1) }
    }

    @Test fun `findOneScenarioGroup WHEN the scenario group exists THEN return the scenario group found`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        every { ScenarioGroupService.findOneById(groupId1) } returns scenarioGroup1
        // WHEN
        val result = ScenarioService.findOneScenarioGroup(namespace, groupId1)
        // THEN
        assertEquals(scenarioGroup1, result)
        verify(exactly = 1) { ScenarioGroupService.findOneById(groupId1) }
    }

    @Test fun `updateOneScenarioGroup WHEN the scenario group not exists THEN throws exception`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        every { ScenarioGroupService.updateOne(scenarioGroup1Copy) } throws ScenarioGroupNotFoundException(groupId1)
        // WHEN // THEN
        assertThrows<ScenarioGroupNotFoundException> {
            ScenarioService.updateOneScenarioGroup(namespace, scenarioGroup1Copy)
        }
        verify(exactly = 0) { ScenarioGroupService.updateOne(scenarioGroup1) }
    }

    @Test fun `updateOneScenarioGroup WHEN the scenario group exists THEN update and return the scenario group updated`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        mockkObject(StoryService)
        every { ScenarioGroupService.updateOne(scenarioGroup1Copy) } returns scenarioGroup1Copy
        every {
            StoryService.updateActivateStoryFeatureByNamespaceAndBotIdAndStoryId(
                any(),
                any(),
                any(),
                any(),
            )
        } returns false
        // WHEN
        val result = ScenarioService.updateOneScenarioGroup(namespace, scenarioGroup1Copy)
        // THEN
        assertEquals(scenarioGroup1Copy, result)
        verify(exactly = 1) { ScenarioGroupService.updateOne(scenarioGroup1Copy) }
    }

    @Test fun `updateOneScenarioVersion WHEN the scenario version not exists THEN throws exception`() {
        // GIVEN
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } throws ScenarioVersionNotFoundException(versionId1)
        // WHEN // THEN
        assertThrows<ScenarioVersionNotFoundException> {
            ScenarioService.updateOneScenarioVersion(scenarioVersion1)
        }
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { ScenarioVersionService.updateOne(any()) }
        verify(exactly = 0) { ScenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
    }

    @Test fun `updateOneScenarioVersion WHEN the scenario version that is not part of the group THEN throws exception`() {
        // GIVEN
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } returns scenarioVersion1.copy(scenarioGroupId = groupId2.toId())
        // WHEN // THEN
        assertThrows<ScenarioGroupAndVersionMismatchException> {
            ScenarioService.updateOneScenarioVersion(scenarioVersion1)
        }
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { ScenarioVersionService.updateOne(any()) }
        verify(exactly = 0) { ScenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
    }

    @Test fun `updateOneScenarioVersion WHEN the scenario version exists with CURRENT state THEN throws exception`() {
        // GIVEN
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } returns scenarioVersion1Copy2
        // WHEN // THEN
        assertThrows<ScenarioVersionBadStateException> {
            ScenarioService.updateOneScenarioVersion(scenarioVersion1)
        }
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { ScenarioVersionService.updateOne(any()) }
        verify(exactly = 0) { ScenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
    }

    @Test fun `updateOneScenarioVersion WHEN the scenario version exists with ARCHIVED state THEN throws exception`() {
        // GIVEN
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId5) } returns scenarioVersion5
        // WHEN // THEN
        assertThrows<ScenarioVersionBadStateException> {
            ScenarioService.updateOneScenarioVersion(scenarioVersion5)
        }
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId5) }
        verify(exactly = 0) { ScenarioVersionService.updateOne(any()) }
        verify(exactly = 0) { ScenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
    }

    @Test fun `updateOneScenarioVersion to DRAFT WHEN the scenario version exists with DRAFT state and is part of its group and there is no current version for the group THEN update and return the scenario version updated`() {
        // GIVEN
        val scenarioVersion4CopyArchived = scenarioVersion4.copy(state = ScenarioVersionState.ARCHIVED)
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        every { ScenarioVersionService.updateOne(scenarioVersion1Copy1)  } returns scenarioVersion1Copy1
        // WHEN
        val result = ScenarioService.updateOneScenarioVersion(scenarioVersion1Copy1)
        // THEN
        assertEquals(scenarioVersion1Copy1, result)
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { ScenarioVersionService.updateOne(scenarioVersion1) }
        verify(exactly = 0) { ScenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
        verify(exactly = 0) { ScenarioVersionService.updateOne(scenarioVersion4CopyArchived) }
    }

    @Test fun `updateOneScenarioVersion to CURRENT WHEN the scenario version exists with DRAFT state and is part of its group and there is no current version for the group THEN update and return the scenario version updated`() {
        // GIVEN
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        every { ScenarioVersionService.findAllByScenarioGroupIdAndState(any(), any()) } returns emptyList()
        every { ScenarioVersionService.updateOne(scenarioVersion1Copy2)  } returns scenarioVersion1Copy2
        // WHEN
        val result = ScenarioService.updateOneScenarioVersion(scenarioVersion1Copy2)
        // THEN
        assertEquals(scenarioVersion1Copy2, result)
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
        verify(exactly = 1) { ScenarioVersionService.findAllByScenarioGroupIdAndState(any(), any())  }
        verify(exactly = 1) { ScenarioVersionService.updateOne(scenarioVersion1Copy2) }
    }

    @Test fun `updateOneScenarioVersion to CURRENT WHEN the scenario version exists with DRAFT state and is part of its group and there is a current version for the group THEN update and return the scenario version updated`() {
        // GIVEN
        val scenarioVersion4CopyArchived = scenarioVersion4.copy(state = ScenarioVersionState.ARCHIVED)
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        every { ScenarioVersionService.findAllByScenarioGroupIdAndState(groupId1,
            ScenarioVersionState.CURRENT) } returns listOf(scenarioVersion4)
        every { ScenarioVersionService.updateOne(scenarioVersion1Copy2)  } returns scenarioVersion1Copy2
        every { ScenarioVersionService.updateOne(scenarioVersion4CopyArchived)  } returns scenarioVersion4CopyArchived
        // WHEN
        val result = ScenarioService.updateOneScenarioVersion(scenarioVersion1Copy2)
        // THEN
        assertEquals(scenarioVersion1Copy2, result)
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
        verify(exactly = 1) { ScenarioVersionService.findAllByScenarioGroupIdAndState(groupId1,
            ScenarioVersionState.CURRENT)  }
        verify(exactly = 1) { ScenarioVersionService.updateOne(scenarioVersion1Copy2) }
        verify(exactly = 1) { ScenarioVersionService.updateOne(scenarioVersion4CopyArchived) }
    }

    @Test fun `deleteOneScenarioGroup WHEN the scenario group and its versions exist THEN delete the tick story, the group and its versions and return true`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        mockkObject(ScenarioVersionService)
        mockkObject(StoryService)
        every { ScenarioGroupService.findOneById(groupId1) } returns scenarioGroup1
        every { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId1) } returns true
        justRun { ScenarioVersionService.deleteAllByScenarioGroupId(groupId1) }
        justRun { ScenarioGroupService.deleteOneById(groupId1) }
        every {
            StoryService.getStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId1)
        } returns null

        // WHEN
        val result = ScenarioService.deleteOneScenarioGroup(namespace, botId1, groupId1)
        // THEN
        assertTrue(result)
        verify(exactly = 1) { ScenarioGroupService.findOneById(groupId1) }
        verify(exactly = 1) { ScenarioVersionService.deleteAllByScenarioGroupId(groupId1) }
        verify(exactly = 1) { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId1) }
        verify(exactly = 1) { ScenarioGroupService.deleteOneById(groupId1) }
    }

    @Test fun `deleteOneScenarioGroup WHEN the scenario group does not exists THEN return false`() {
        // GIVEN
        mockkObject(ScenarioVersionService)
        mockkObject(ScenarioVersionService)
        mockkObject(StoryService)
        every { ScenarioGroupService.findOneById(groupId1) } throws ScenarioGroupNotFoundException(groupId1)
        every { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId2) } returns true
        justRun { ScenarioVersionService.deleteAllByScenarioGroupId(groupId1) }
        justRun { ScenarioGroupService.deleteOneById(groupId1) }
        // WHEN
        val result = ScenarioService.deleteOneScenarioGroup(namespace, botId1, groupId1)
        // THEN
        assertFalse(result)
        verify(exactly = 1) { ScenarioGroupService.findOneById(groupId1) }
        verify(exactly = 0) { ScenarioVersionService.deleteAllByScenarioGroupId(groupId1) }
        verify(exactly = 0) { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId1) }
        verify(exactly = 0) { ScenarioGroupService.deleteOneById(groupId1) }
    }

    @Test fun `deleteOneScenarioGroup WHEN the scenario group versions does not exists THEN return false`() {
        // GIVEN
        mockkObject(ScenarioGroupService)
        mockkObject(ScenarioVersionService)
        mockkObject(StoryService)
        every { ScenarioGroupService.findOneById(groupId1) } returns scenarioGroup1
        every { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId2) } returns true
        every { ScenarioVersionService.deleteAllByScenarioGroupId(groupId1) } throws ScenarioVersionNotFoundException()
        justRun { ScenarioGroupService.deleteOneById(groupId1) }
        // WHEN
        val result = ScenarioService.deleteOneScenarioGroup(namespace, botId1, groupId1)
        // THEN
        assertFalse(result)
        verify(exactly = 1) { ScenarioGroupService.findOneById(groupId1) }
        verify(exactly = 1) { ScenarioVersionService.deleteAllByScenarioGroupId(groupId1) }
        verify(exactly = 0) { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId1) }
        verify(exactly = 0) { ScenarioGroupService.deleteOneById(groupId1) }
    }



    @Test fun `deleteOneScenarioVersion a draft version WHEN it exists and is a part of scenario group but not the last one THEN delete the version but not its group nor the tick story and return true` () {
        // GIVEN
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        justRun { ScenarioVersionService.deleteOneById(versionId1) }
        every { ScenarioVersionService.countAllByScenarioGroupId(groupId1) } returns 2
        // WHEN
        val result = ScenarioService.deleteOneScenarioVersion(namespace, botId1, groupId1, versionId1)
        // THEN
        assertTrue(result)
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId1) }
        verify(exactly = 1) { ScenarioVersionService.deleteOneById(versionId1) }
        verify(exactly = 1) { ScenarioVersionService.countAllByScenarioGroupId(groupId1) }
        verify(exactly = 0) { ScenarioGroupService.deleteOneById(groupId1) }
    }

    @Test fun `deleteOneScenarioVersion a current version WHEN it exists and is a part of scenario group but not the last one THEN delete the tick story, the version but not its group and return true` () {
        // GIVEN
        mockkObject(ScenarioVersionService)
        mockkObject(StoryService)
        every { ScenarioVersionService.findOneById(versionId3) } returns scenarioVersion3
        justRun { ScenarioVersionService.deleteOneById(versionId3) }
        every { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId2) } returns true
        every { ScenarioVersionService.countAllByScenarioGroupId(groupId2) } returns 2
        // WHEN
        val result = ScenarioService.deleteOneScenarioVersion(namespace, botId1, groupId2, versionId3)
        // THEN
        assertTrue(result)
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId3) }
        verify(exactly = 1) { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId2) }
        verify(exactly = 1) { ScenarioVersionService.deleteOneById(versionId3) }
        verify(exactly = 1) { ScenarioVersionService.countAllByScenarioGroupId(groupId2) }
        verify(exactly = 0) { ScenarioGroupService.deleteOneById(groupId2) }
    }

    @Test fun `deleteOneScenarioVersion a draft version WHEN it exists and is a part of scenario group and is the last one THEN delete the version and its group and return true` () {
        // GIVEN
        mockkObject(ScenarioGroupService)
        mockkObject(ScenarioVersionService)
        mockkObject(StoryService)
        every { ScenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        justRun { ScenarioVersionService.deleteOneById(versionId1) }
        every { ScenarioVersionService.countAllByScenarioGroupId(groupId1) } returns 0
        justRun { ScenarioGroupService.deleteOneById(groupId1) }
        // WHEN
        val result = ScenarioService.deleteOneScenarioVersion(namespace, botId1, groupId1, versionId1)
        // THEN
        assertTrue(result)
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId1) }
        verify(exactly = 1) { ScenarioVersionService.deleteOneById(versionId1) }
        verify(exactly = 1) { ScenarioVersionService.countAllByScenarioGroupId(groupId1) }
        verify(exactly = 1) { ScenarioGroupService.deleteOneById(groupId1) }
    }

    @Test fun `deleteOneScenarioVersion a draft version WHEN it dose not exists THEN return false` () {
        // GIVEN
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } throws ScenarioVersionNotFoundException(versionId1)
        // WHEN
        val result = ScenarioService.deleteOneScenarioVersion(namespace, botId1, groupId1, versionId1)
        // THEN
        assertFalse(result)
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId1) }
        verify(exactly = 0) { ScenarioVersionService.deleteOneById(versionId1) }
        verify(exactly = 0) { ScenarioVersionService.countAllByScenarioGroupId(groupId1) }
        verify(exactly = 0) { ScenarioGroupService.deleteOneById(groupId1) }
    }

    @Test fun `deleteOneScenarioVersion a draft version WHEN it exists and is not a part of scenario group THEN return false` () {
        // GIVEN
        mockkObject(ScenarioVersionService)
        every { ScenarioVersionService.findOneById(versionId1) } returns scenarioVersion1
        // WHEN
        val result = ScenarioService.deleteOneScenarioVersion(namespace, botId1, groupId2, versionId1)
        // THEN
        assertFalse(result)
        verify(exactly = 1) { ScenarioVersionService.findOneById(versionId1) }
        verify(exactly = 0) { StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId1, groupId2) }
        verify(exactly = 0) { ScenarioVersionService.deleteOneById(versionId1) }
        verify(exactly = 0) { ScenarioVersionService.countAllByScenarioGroupId(groupId1) }
        verify(exactly = 0) { ScenarioGroupService.deleteOneById(groupId1) }
    }
}