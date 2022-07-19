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

import ai.tock.shared.exception.rest.InternalServerException
import ai.tock.shared.exception.rest.NotFoundException

interface ScenarioService {

    /**
     * Returns all scenarios know
     */
    fun findAll(): Collection<Scenario>

    /**
     * Returns a specific scenario based on its id
     * @property scenarioId id of scenario to find
     * @throws NotFoundException when no scenario found
     * @throws InternalServerException when scenario found is invalid
     */
    fun findById(scenarioId: String): Scenario

    /**
     * Create a new scenario
     * @property scenario to create
     */
    fun create(scenario: Scenario): Scenario

    /**
     * Update an existing scenario
     * @property scenarioId id of URI to update scenario
     * @property scenario to update
     */
    fun update(scenarioId: String, scenario: Scenario): Scenario

    /**
     * Delete an existing scenario
     * @property scenarioId id of scenario to delete
     */
    fun delete(scenarioId: String)
}