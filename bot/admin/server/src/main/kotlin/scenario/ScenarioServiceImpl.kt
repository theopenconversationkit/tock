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

import ai.tock.shared.exception.TockNotFound
import ai.tock.shared.injector
import ai.tock.shared.exception.rest.ConflictException
import ai.tock.shared.exception.rest.InternalServerException
import ai.tock.shared.exception.rest.NotFoundException
import com.github.salomonbrys.kodein.instance
import mu.KLogger
import mu.KotlinLogging

/**
 * Implementation of ScenarioService
 */
class ScenarioServiceImpl : ScenarioService {

    private val logger: KLogger = KotlinLogging.logger {}

    private val scenarioDAO: ScenarioDAO by injector.instance()

    /**
     * Returns all scenarios know
     */
    override fun findAll(): Collection<Scenario> {
        return scenarioDAO.findAll()
            .map(checkScenarioFromDatabase)
    }

    /**
     * Returns a specific scenario based on it's id
     * @property scenarioId id of scenario to find
     * @throws NotFoundException when no scenario found
     * @throws InternalServerException when scenario found is invalid
     */
    override fun findById(scenarioId: String): Scenario {
        return scenarioDAO.findById(scenarioId)
            .checkIsNotNullForId(scenarioId)
            .checkScenarioFromDatabase()
    }

    /**
     * Create a new scenario
     * @property scenario to create
     * @throws ConflictException when scenario id is not null
     * @throws InternalServerException when scenario created is invalid
     */
    override fun create(scenario: Scenario): Scenario {
        scenario.checkToCreate()
        return scenarioDAO.create(scenario)
            .checkScenarioFromDatabase()
    }

    /**
     * Update an existing scenario
     * @property scenarioId id of URI to update scenario
     * @property scenario to update
     * @throws NotFoundException when scenarioId don't exist
     * @throws ConflictException when scenario id is null
     * @throws ConflictException when scenario id is not the same as scenarioId
     * @throws InternalServerException when scenario updated is invalid
     */
    override fun update(scenarioId: String, scenario: Scenario): Scenario {
        scenario.mustExist(exist(scenarioId)).checkToUpdate(scenarioId)
        return scenarioDAO.update(scenario)
             .checkScenarioFromDatabase()
    }

    private fun exist(scenarioId: String): Boolean {
        return scenarioDAO.findById(scenarioId)?.let { true } ?: false
    }

    /**
     * Delete an existing scenario
     * If the scenario does not already exist, it just logs that it does not exist
     * @property scenarioId id of scenario to delete
     */
    override fun delete(scenarioId: String) {
        try {
            scenarioDAO.delete(scenarioId)
        } catch (notFoundException: TockNotFound) {
            logger.debug { "scenario id $scenarioId no longer exist and cannot be deleted" }
        }
    }
}