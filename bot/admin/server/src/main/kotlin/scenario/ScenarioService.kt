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

interface ScenarioService {

    /**
     * Returns all scenarios on all versions
     */
    fun findAll(): Collection<Scenario>

    /**
     * Returns all scenarios that are active and not in the state ARCHIVE {@see ScenarioState}
     */
    fun findAllActive(): Collection<Scenario>

    /**
     * Returns a scenario with only the version requested
     * @param version of scenario to find
     */
    fun findOnlyVersion(version: String): Scenario

    /**
     * Returns a scenario with all version based on its id
     * @param id of scenario to find
     */
    fun findById(id: String): Scenario

    /**
     * Returns the current version of a scenario based on its id
     * @param id of scenario to find
     */
    fun findCurrentById(id: String): Scenario

    /**
     * Returns scenario with versions not archive based on its id
     * @param id of scenario to find
     */
    fun findActiveById(id: String): Scenario

    /**
     * Create a new version on a new scenario or on an existing scenario if id is set
     * @param scenario to create
     */
    fun create(scenario: Scenario): Scenario

    /**
     * Update an existing version on an existing scenario
     * @param scenarioId id of URI to update scenario
     * @param scenario to update
     */
    fun update(version: String, scenario: Scenario): Scenario

    /**
     * Delete an existing version of a scenario
     * @param version of scenario to delete
     */
    fun deleteByVersion(version: String)

    /**
     * Delete all versions of an existing scenario
     * @param id of scenario to delete
     */
    fun deleteById(id: String)
}