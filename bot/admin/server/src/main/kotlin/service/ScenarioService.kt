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
package ai.tock.bot.admin.service

import ai.tock.bot.admin.scenario.ScenarioGroup
import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.shared.exception.scenario.group.ScenarioGroupNotFoundException

/**
 * Service that manage the scenario functionality
 */
interface ScenarioService {
    /**
     * Returns all scenario groups with their scenario versions
     */
    fun findAllScenarioGroupWithVersionsByBotId(namespace: String, botId: String): List<ScenarioGroup>

    /**
     * Returns one scenario group with its scenario versions
     * @param scenarioGroupId: id of the scenario group
     * @throws [ScenarioGroupNotFoundException] if the scenario group was not found
     */
    fun findOneScenarioGroup(namespace: String, scenarioGroupId: String): ScenarioGroup

    /**
     * Returns one scenario version
     * @param scenarioGroupId: id of the scenario group
     * @param scenarioVersionId: id of the scenario version
     * @throws [ScenarioVersionNotFoundException] if the scenario version was not found
     */
    fun findOneScenarioVersion(scenarioGroupId: String, scenarioVersionId: String): ScenarioVersion

    /**
     * Create a new scenario group
     * Returns the created scenario group
     * @param scenarioGroup: the scenario group to create
     */
    fun createOneScenarioGroup(scenarioGroup: ScenarioGroup): ScenarioGroup

    /**
     * Import a scenario group with its version and returns the created scenario group
     * @param scenarioGroup: the scenario group to import
     */
    fun importOneScenarioGroup(scenarioGroup: ScenarioGroup): ScenarioGroup

    /**
     * Import many scenario versions and returns the created scenario versions
     * @param scenarioVersions: a list of scenario versions to import
     */
    fun importManyScenarioVersion(namespace: String, scenarioVersions: List<ScenarioVersion>): List<ScenarioVersion>

    /**
     * Create a new scenario version
     * Returns the created scenario version
     * @param scenarioVersion: the scenario version to create
     * @throws [ScenarioGroupNotFoundException] if the scenario group of the [scenarioVersion] was not found
     */
    fun createOneScenarioVersion(namespace: String, scenarioVersion: ScenarioVersion): ScenarioVersion

    /**
     * Update a given scenario group
     * Returns the updated scenario group
     * @param namespace: the namespace
     * @param scenarioGroup: the scenario group to update
     * @throws [ScenarioGroupNotFoundException] if the [scenarioGroup] was not found
     */
    fun updateOneScenarioGroup(namespace: String, scenarioGroup: ScenarioGroup): ScenarioGroup

    /**
     * Update a given scenario version
     * Returns the updated scenario version
     * @param scenarioVersion: the scenario version to update.
     * @throws [ScenarioVersionNotFoundException] if the [scenarioVersion] was not found.
     * @throws [ScenarioGroupNotFoundException] if the scenario group of the [scenarioVersion] was not found
     * @throws [MismatchedScenarioException] if the [scenarioVersion] is not part of its scenario group
     * @throws [UnauthorizedUpdateScenarioVersionException] if the [scenarioVersion] cannot be updated
     */
    fun updateOneScenarioVersion(scenarioVersion: ScenarioVersion): ScenarioVersion

    /**
     * Delete an existing scenario group and its versions as well as its tick story
     * @param namespace: the namespace
     * @param botId : id of the bot
     * @param scenarioGroupId: id of the scenario group
     * @throws [ScenarioGroupNotFoundException] if the scenario group was not found
     */
    fun deleteOneScenarioGroup(namespace: String, botId: String, scenarioGroupId: String): Boolean

    /**
     * Delete an existing scenario version.
     * If the scenario version given is the last one, then delete the scenario group
     * @param namespace: the namespace
     * @param botId : id of the bot
     * @param scenarioGroupId: id of the scenario group
     * @param scenarioVersionId: id of the scenario version
     * @throws [ScenarioGroupNotFoundException] if the scenario group was not found
     * @throws [ScenarioVersionNotFoundException] if the scenario version was not found
     * @throws [MismatchedScenarioException] if the scenario version is not part of its scenario group
     */
    fun deleteOneScenarioVersion(namespace: String, botId: String, scenarioGroupId: String, scenarioVersionId: String): Boolean

}