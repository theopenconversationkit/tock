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
import com.github.salomonbrys.kodein.instance
import mu.KLogger
import mu.KotlinLogging

class ScenarioServiceImpl : ScenarioService {

    private val logger: KLogger = KotlinLogging.logger {}

    private val scenarioDAO: ScenarioDAO by injector.instance()

    override fun findAll(): Collection<Scenario> {
        return scenarioDAO.findAll()
            .map(checkScenarioFromDatabase)
    }

    override fun findById(scenarioId: String): Scenario {
        return scenarioDAO.findById(scenarioId)
            .checkIsNotNullForId(scenarioId)
            .checkScenarioFromDatabase()
    }

    override fun create(scenario: Scenario): Scenario {
        scenario.checkToCreate()
        return scenarioDAO.create(scenario)
            .checkScenarioFromDatabase()
    }

    override fun update(scenarioId: String, scenario: Scenario): Scenario {
        scenario.mustExist(existe(scenarioId)).checkToUpdate(scenarioId)
        return scenarioDAO.update(scenario)
                .checkScenarioFromDatabase()
    }

    private fun existe(scenarioId: String): Boolean {
        return scenarioDAO.findById(scenarioId)?.let { true } ?: false
    }

    override fun delete(scenarioId: String) {
        try {
            scenarioDAO.delete(scenarioId)
        } catch (notFoundException: TockNotFound) {
            logger.debug { "scenario id $scenarioId already don't exist" }
        }
    }
}