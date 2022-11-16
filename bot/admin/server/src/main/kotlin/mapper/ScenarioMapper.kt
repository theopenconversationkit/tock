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

package ai.tock.bot.admin.mapper

import ai.tock.bot.admin.model.scenario.ScenarioActionHandlerResponse
import ai.tock.bot.admin.model.scenario.ScenarioGroupRequest
import ai.tock.bot.admin.model.scenario.ScenarioGroupResponse
import ai.tock.bot.admin.model.scenario.ScenarioGroupWithVersionsRequest
import ai.tock.bot.admin.model.scenario.ScenarioVersionRequest
import ai.tock.bot.admin.model.scenario.ScenarioVersionResponse
import ai.tock.bot.admin.model.scenario.ScenarioVersionWithoutData
import ai.tock.bot.admin.scenario.ScenarioGroup
import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.bot.handler.ActionHandler
import org.litote.kmongo.newId
import org.litote.kmongo.toId

/**
 * Mapping object between scenario classes
 */
object ScenarioMapper {
    /**
     * Lambda to map an [ScenarioGroup] to a [ScenarioGroupResponse]
     */
    fun ScenarioGroup.toScenarioGroupResponse(): ScenarioGroupResponse {
        return ScenarioGroupResponse(
            id = _id.toString(),
            name = name,
            category = category,
            tags = tags,
            description = description,
            creationDate = creationDate,
            updateDate = updateDate,
            versions = versions.map {
                it.toScenarioVersionWithoutData()
            },
            enabled = enabled
        )
    }

    /**
     * Lambda to map an [ScenarioVersion] to a [ScenarioVersionResponse]
     */
    fun ScenarioVersion.toScenarioVersionResponse(): ScenarioVersionResponse {
        return ScenarioVersionResponse(
            id = _id.toString(),
            creationDate = creationDate,
            updateDate = updateDate,
            comment = comment,
            data = data,
            state = state.value.uppercase()
        )
    }

    /**
     * Lambda to map an [ScenarioVersion] to a [ScenarioVersionWithoutData]
     */
    fun ScenarioVersion.toScenarioVersionWithoutData(): ScenarioVersionWithoutData {
        return ScenarioVersionWithoutData(
            id = _id.toString(),
            creationDate = creationDate,
            updateDate = updateDate,
            comment = comment,
            state = state.value.uppercase()
        )
    }

    /**
     * Lambda to map an [ScenarioGroupRequest] to a [ScenarioGroup]
     * @param botId: id of the bot
     */
    fun ScenarioGroupRequest.toScenarioGroup(botId: String): ScenarioGroup {
        return toScenarioGroup(botId, newId<ScenarioGroup>().toString())
    }

    /**
     * Lambda to map an [ScenarioGroupRequest] to a [ScenarioGroup]
     * @param botId: id of the bot
     * @param scenarioGroupId: id of the scenario group
     */
    fun ScenarioGroupRequest.toScenarioGroup(botId: String, scenarioGroupId: String): ScenarioGroup {
        return ScenarioGroup(
            _id = scenarioGroupId.toId(),
            name = name,
            botId = botId,
            category = category,
            tags = tags,
            description = description,
            versions = emptyList(),
            enabled = enabled
        )
    }

    /**
     * Lambda to map an [ScenarioGroupWithVersionsRequest] to a [ScenarioGroup]
     * @param botId: id of the bot
     */
    fun ScenarioGroupWithVersionsRequest.toScenarioGroup(botId: String): ScenarioGroup {
        return toScenarioGroup(botId, newId<ScenarioGroup>().toString())
    }

    /**
     * Lambda to map an [ScenarioGroupWithVersionsRequest] to a [ScenarioGroup]
     * @param botId: id of the bot
     * @param scenarioGroupId: id of the scenario group
     */
    fun ScenarioGroupWithVersionsRequest.toScenarioGroup(botId: String, scenarioGroupId: String): ScenarioGroup {
        return ScenarioGroup(
            _id = scenarioGroupId.toId(),
            name = name,
            botId = botId,
            category = category,
            tags = tags,
            description = description,
            versions = versions.map { it.toScenarioVersion(scenarioGroupId) },
            enabled = enabled
        )
    }

    /**
     * Lambda to map an [ScenarioVersionRequest] to a [ScenarioVersion]
     * @param scenarioGroupId: id of the scenario group
     */
    fun ScenarioVersionRequest.toScenarioVersion(scenarioGroupId: String): ScenarioVersion {
        return toScenarioVersion(scenarioGroupId, newId<ScenarioVersion>().toString())
    }

    /**
     * Lambda to map an [ScenarioVersionRequest] to a [ScenarioVersion]
     * @param scenarioGroupId: id of the scenario group
     * @param scenarioVersionId: id of the scenario version
     */
    fun ScenarioVersionRequest.toScenarioVersion(scenarioGroupId: String, scenarioVersionId: String): ScenarioVersion {
        return ScenarioVersion(
            _id = scenarioVersionId.toId(),
            scenarioGroupId = scenarioGroupId.toId(),
            data = data,
            state = state,
            comment = comment
        )
    }

    /**
     * Lambda to map an [ActionHandler] to a [ScenarioActionHandlerResponse]
     */
    fun ActionHandler.toScenarioActionHandlerResponse(): ScenarioActionHandlerResponse {
        return ScenarioActionHandlerResponse(
            name = name,
            description = description,
            inputContexts = inputContexts,
            outputContexts = outputContexts
        )
    }

}
