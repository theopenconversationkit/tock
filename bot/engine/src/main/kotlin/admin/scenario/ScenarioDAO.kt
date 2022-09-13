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

/**
 *
 */
interface ScenarioDAO {

    /**
     * Return a collection of all Scenario.
     */
    fun findAll(): Collection<Scenario>

    /**
     * Return Scenario find by version id or null if not exist.
     * @param version of scenario history to find.
     */
    fun findByVersion(version: String): Scenario?

    /**
     * Return a Scenario find by id or null if not exist.
     * @param id of scenarios to find.
     */
    fun findById(id: String): Scenario?

    /**
     * Create Scenario and return it.
     * @param scenario to create.
     */
    fun create(scenario: Scenario): Scenario?

    /**
     * Patch Scenario and return it.
     * (to create new version on existing scenario)
     * @param scenario to create.
     */
    fun patch(scenario: Scenario): Scenario?

    /**
     * Update Scenario and return it.
     * @param scenario to update.
     */
    fun update(scenario: Scenario): Scenario?

    /**
     * Delete Scenario by id.
     * @param id of scenario to delete.
     */
    fun delete(id: String)
}