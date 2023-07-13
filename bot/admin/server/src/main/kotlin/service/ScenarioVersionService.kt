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

import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.bot.admin.scenario.ScenarioVersionDAO
import ai.tock.bot.admin.scenario.ScenarioVersionState
import ai.tock.shared.injector
import ai.tock.shared.exception.scenario.version.ScenarioVersionNotFoundException
import ai.tock.shared.provide
import org.litote.kmongo.toId

/**
 * Service that manage the scenario versions
 */
object ScenarioVersionService {
    private val scenarioVersionDAO: ScenarioVersionDAO get() = injector.provide()

    /**
     * Returns one scenario version
     * @param id: id of the scenario version
     * @throws [ScenarioVersionNotFoundException] if the scenario version was not found.
     */
    fun findOneById(id: String): ScenarioVersion =
        scenarioVersionDAO.findOneById(id.toId()) ?: throw ScenarioVersionNotFoundException(id)

    /**
     * Returns all scenario versions that matches with scenario group and state
     * @param scenarioGroupId: id of the scenario group
     * @param state: state of the scenario version
     */
    fun findAllByScenarioGroupIdAndState(scenarioGroupId: String, state: ScenarioVersionState): List<ScenarioVersion> {
        return scenarioVersionDAO.findAllByScenarioGroupIdAndState(scenarioGroupId.toId(), state)
    }

    /**
     * Create a new scenario version and returns the created scenario version
     * @param scenarioVersion: the scenario version to create
     */
    fun createOne(scenarioVersion: ScenarioVersion): ScenarioVersion {
        return scenarioVersionDAO.createOne(scenarioVersion)
    }

    /**
     * Create multiple scenario versions and returns the created scenario versions
     * @param scenarioVersions: the scenario versions to create
     */
    fun createMany(scenarioVersions: List<ScenarioVersion>): List<ScenarioVersion> {
        return scenarioVersionDAO.createMany(scenarioVersions)
    }

    /**
     * Update a given scenario version and returns the updated scenario version
     * @param scenarioVersion: the scenario version to update.
     * @throws [ScenarioVersionNotFoundException] if [ScenarioVersion] was not found.
     */
    fun updateOne(scenarioVersion: ScenarioVersion): ScenarioVersion {
        return scenarioVersionDAO.updateOne(scenarioVersion)
    }

    /**
     * Delete an existing scenario version.
     * @param id: id of the scenario version
     * @throws [ScenarioVersionNotFoundException] if the scenario version `id` is not found
     */
    fun deleteOneById(id: String) {
        scenarioVersionDAO.deleteOneById(id.toId())
    }

    /**
     * Delete all existing scenario versions by their scenario group id.
     * @param scenarioGroupId: id of the scenario group
     */
    fun deleteAllByScenarioGroupId(scenarioGroupId: String) {
        scenarioVersionDAO.deleteAllByScenarioGroupId(scenarioGroupId.toId())
    }

    /**
     * Count the number of scenario versions for the scenario group.
     * @param scenarioGroupId: id of the scenario group
     */
    fun countAllByScenarioGroupId(scenarioGroupId: String): Long {
        return scenarioVersionDAO.countAllByScenarioGroupId(scenarioGroupId.toId())
    }

}