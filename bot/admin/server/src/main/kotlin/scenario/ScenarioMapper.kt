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

import ai.tock.bot.admin.model.scenario.ScenarioRequest
import ai.tock.bot.admin.model.scenario.ScenarioResult
import ai.tock.shared.exception.rest.InternalServerException

/**
 * Map a ScenarioRequest to a Scenario
 */
val mapToScenario: ScenarioRequest.() -> Scenario = {
    Scenario(
        id = id,
        name = name,
        category = category,
        tags = tags,
        applicationId = applicationId,
        createDate = createDate,
        updateDate = updateDate,
        description = description,
        data = data,
        state = state
    )
}

/**
 * Map a Scenario to a ScenarioRequest
 */
val mapToScenarioResult: Scenario.() -> ScenarioResult = {
    ScenarioResult(
        id = id ?: throw InternalServerException("cannot create scenarioResult with id null"),
        name = name,
        category = category,
        tags = tags,
        applicationId = applicationId,
        createDate = createDate,
        updateDate = updateDate,
        description = description,
        data = data,
        state = state
    )
}