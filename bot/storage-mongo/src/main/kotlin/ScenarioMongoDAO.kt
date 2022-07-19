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
package ai.tock.bot.mongo

import ai.tock.bot.admin.scenario.Scenario
import ai.tock.bot.admin.scenario.ScenarioDAO
import ai.tock.bot.mongo.ScenarioCol_.Companion.Name
import ai.tock.shared.exception.TockIllegalArgumentException
import ai.tock.shared.exception.TockNotFound
import com.mongodb.client.result.DeleteResult
import org.litote.kmongo.*

internal object ScenarioMongoDAO : ScenarioDAO {

    internal val scenarioDatabase =
        MongoBotConfiguration.database.getCollection<ScenarioCol>("scenario")

    /**
     * Return a collection of all Scenario.
     */
    override fun findAll(): List<Scenario> {
        return scenarioDatabase.find()
            .ascendingSort(Name)
            .toList()
            .map(mapToScenario)
    }

    /**
     * Return Scenario find by id or null if not exist.
     * @property id of scenario to find.
     */
    override fun findById(id: String): Scenario? {
        return scenarioDatabase.findOneById(id.toId<Scenario>())?.mapToScenario()
    }

    /**
     * Create Scenario and return it.
     * @property scenario to create.
     * @throws TockIllegalArgumentException when scenario have id.
     */
    override fun create(scenario: Scenario): Scenario {
        if(isIdPresent(scenario)) {
            throw TockIllegalArgumentException("scenario musn't have id")
        }
        return save(scenario.mapToScenarioCol()).mapToScenario()
    }

    /**
     * Update Scenario and return it.
     * @property scenario to update.
     * @throws TockIllegalArgumentException when scenario have no id.
     */
    override fun update(scenario: Scenario): Scenario {
        if(!isIdPresent(scenario)) {
            throw TockIllegalArgumentException("scenario must have id")
        }
        return save(scenario.mapToScenarioCol()).mapToScenario()
    }

    private fun isIdPresent(scenario: Scenario): Boolean {
        return scenario.id?.isNotBlank() ?: false
    }

    private fun save(scenario: ScenarioCol): ScenarioCol {
        scenarioDatabase.save(scenario)
        return scenario
    }

    /**
     * Delete Scenario by id.
     * @property id of scenario to delete
     * @throws TockNotFound when no delete process of id
     */
    override fun delete(id: String) {
        val result: DeleteResult = scenarioDatabase.deleteOneById(id.toId<Scenario>())
        if(result.deletedCount == 0L) {
            throw TockNotFound("scenario $id not found in database")
        }
    }

    private val mapToScenario: ScenarioCol.() -> Scenario  = {
        Scenario(
            id = _id.toString(),
            name = name,
            category = category,
            tags = tags,
            applicationId = applicationId,
            createDate = createDate,
            updateDate = updateDate,
            description= description,
            data = data,
            state = state )
    }

    private val mapToScenarioCol: Scenario.() -> ScenarioCol  = {
        ScenarioCol(
            _id = id?.toId() ?: newId(),
            name = name,
            category = category,
            tags = tags,
            applicationId = applicationId,
            createDate = createDate,
            updateDate = updateDate,
            description= description,
            data = data,
            state = state )
    }
}