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

package ai.tock.bot.admin.mapper

import ai.tock.bot.admin.mapper.ScenarioMapper.toScenarioGroup
import ai.tock.bot.admin.mapper.ScenarioMapper.toScenarioGroupResponse
import ai.tock.bot.admin.mapper.ScenarioMapper.toScenarioVersion
import ai.tock.bot.admin.mapper.ScenarioMapper.toScenarioVersionResponse
import ai.tock.bot.admin.mapper.ScenarioMapper.toScenarioVersionWithoutData
import ai.tock.bot.admin.model.scenario.ScenarioGroupRequest
import ai.tock.bot.admin.model.scenario.ScenarioGroupResponse
import ai.tock.bot.admin.model.scenario.ScenarioGroupWithVersionsRequest
import ai.tock.bot.admin.model.scenario.ScenarioVersionRequest
import ai.tock.bot.admin.model.scenario.ScenarioVersionResponse
import ai.tock.bot.admin.model.scenario.ScenarioVersionWithoutData
import ai.tock.bot.admin.scenario.ScenarioGroup
import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.bot.admin.scenario.ScenarioVersionState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ScenarioMapperTest {

    private val botId = "botId"
    private val scenarioGroupId   = "01234".toId<ScenarioGroup>()
    private val scenarioVersionId1 = "1-56789".toId<ScenarioVersion>()
    private val scenarioVersionId2 = "2-56789".toId<ScenarioVersion>()
    private val creationDate = ZonedDateTime.now()
    private val updateDate = ZonedDateTime.now().plusHours(2L)

    @Nested
    inner class ScenarioMapperResponse {
        private val scenarioVersion1 = ScenarioVersion(
            _id = scenarioVersionId1,
            scenarioGroupId = scenarioGroupId,
            data = "DATA-1",
            comment = "v1",
            state = ScenarioVersionState.DRAFT,
            creationDate = creationDate,
            updateDate = updateDate
        )

        private val scenarioVersion2 = ScenarioVersion(
            _id = scenarioVersionId2,
            scenarioGroupId = scenarioGroupId,
            data = "DATA-2",
            comment = "v2",
            state = ScenarioVersionState.ARCHIVED,
            creationDate = creationDate,
            updateDate = updateDate
        )

        private val scenarioGroup1 = ScenarioGroup(
            _id = scenarioGroupId,
            botId = botId,
            name = "NAME",
            category = "CATEGORY",
            tags = listOf("TAG1"),
            description = "DESCRIPTION",
            versions = listOf(scenarioVersion1, scenarioVersion2),
            creationDate = creationDate,
            updateDate = updateDate
        )

        /**
         * mapping an [ScenarioVersion] to a [ScenarioVersionResponse]
         */
        @Test
        fun scenarioVersionToResponse() {
            val response = scenarioVersion1.toScenarioVersionResponse()

            assertEquals(scenarioVersion1.data, response.data)
            assertEquals(scenarioVersion1.state.value.uppercase(), response.state)
            assertEquals(scenarioVersion1.comment, response.comment)
            assertEquals(scenarioVersion1.creationDate, response.creationDate)
            assertEquals(scenarioVersion1.updateDate, response.updateDate)
        }

        /**
         * mapping an [ScenarioVersion] to a [ScenarioVersionWithoutData]
         */
        @Test
        fun scenarioVersionToWithoutDataResponse() {
            val response = scenarioVersion1.toScenarioVersionWithoutData()

            assertEquals(scenarioVersion1.state.value.uppercase(), response.state)
            assertEquals(scenarioVersion1.comment, response.comment)
            assertEquals(scenarioVersion1.creationDate, response.creationDate)
            assertEquals(scenarioVersion1.updateDate, response.updateDate)
        }

        /**
         * mapping an [ScenarioGroup] to a [ScenarioGroupResponse]
         */
        @Test
        fun scenarioGroupToResponse() {
            val response = scenarioGroup1.toScenarioGroupResponse()

            assertEquals(scenarioGroup1.name, response.name)
            assertEquals(scenarioGroup1.category, response.category)
            assertEquals(scenarioGroup1.tags, response.tags)
            assertEquals(scenarioGroup1.description, response.description)
            assertEquals(scenarioGroup1.creationDate, response.creationDate)
            assertEquals(scenarioGroup1.updateDate, response.updateDate)
            assertEquals(scenarioGroup1.versions.map { it.toScenarioVersionWithoutData() }, response.versions)
        }
    }

    @Nested
    inner class ScenarioMapperRequest {
        private val scenarioVersionRequest1 = ScenarioVersionRequest(
            data = "DATA-1",
            comment = "v1",
            state = ScenarioVersionState.DRAFT
        )

        private val scenarioVersionRequest2 = ScenarioVersionRequest(
            data = "DATA-2",
            comment = "v2",
            state = ScenarioVersionState.ARCHIVED
        )

        private val scenarioGroupRequest = ScenarioGroupRequest(
            name = "NAME",
            category = "CATEGORY",
            tags = listOf("TAG1"),
            description = "DESCRIPTION"
        )

        private val scenarioGroupWithVersionsRequest = ScenarioGroupWithVersionsRequest(
            name = "NAME",
            category = "CATEGORY",
            tags = listOf("TAG1"),
            description = "DESCRIPTION",
            versions = listOf(scenarioVersionRequest1, scenarioVersionRequest2)
        )

        /**
         * mapping an [ScenarioVersionRequest] to a [ScenarioVersion]
         */
        @Test
        fun requestToScenarioVersion() {
            val scenarioVersion = scenarioVersionRequest1.toScenarioVersion(scenarioGroupId.toString(), scenarioVersionId1.toString())

            assertEquals(scenarioVersionRequest1.data, scenarioVersion.data)
            assertEquals(scenarioVersionRequest1.state, scenarioVersion.state)
            assertEquals(scenarioVersionRequest1.comment, scenarioVersion.comment)
            assertNotEquals(creationDate, scenarioVersion.creationDate)
            assertNotEquals(updateDate, scenarioVersion.updateDate)
        }

        /**
         * mapping an [ScenarioGroupWithVersionsRequest] to a [ScenarioGroup]
         */
        @Test
        fun requestToScenarioGroupWithVersions(){
            val scenarioGroup = scenarioGroupWithVersionsRequest.toScenarioGroup(botId, scenarioGroupId.toString())

            assertEquals(scenarioGroupWithVersionsRequest.name, scenarioGroup.name)
            assertEquals(scenarioGroupWithVersionsRequest.category, scenarioGroup.category)
            assertEquals(scenarioGroupWithVersionsRequest.tags, scenarioGroup.tags)
            assertEquals(scenarioGroupWithVersionsRequest.description, scenarioGroup.description)
        }

        /**
         * mapping an [ScenarioGroupRequest] to a [ScenarioGroup]
         */
        @Test
        fun requestToScenarioGroup() {
            val scenarioGroup = scenarioGroupRequest.toScenarioGroup(botId)

            assertEquals(scenarioGroupRequest.name, scenarioGroup.name)
            assertEquals(scenarioGroupRequest.category, scenarioGroup.category)
            assertEquals(scenarioGroupRequest.tags, scenarioGroup.tags)
            assertEquals(scenarioGroupRequest.description, scenarioGroup.description)
        }
    }


}