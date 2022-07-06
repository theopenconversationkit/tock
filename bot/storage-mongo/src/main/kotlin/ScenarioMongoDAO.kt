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
package ai.tock.bot.mongo

import ai.tock.bot.admin.scenario.Scenario
import ai.tock.bot.admin.scenario.ScenarioDAO
import ai.tock.bot.mongo.ScenarioCol_.Companion.Name
import ai.tock.shared.exception.TockIllegaleArgumentException
import ai.tock.shared.exception.TockNotFound
import com.mongodb.client.result.DeleteResult
import org.litote.kmongo.*

internal object ScenarioMongoDAO : ScenarioDAO {

    internal val scenarioDatabase =
        MongoBotConfiguration.database.getCollection<ScenarioCol>("scenario")

    override fun findAll(): List<Scenario> {
        return scenarioDatabase.find()
            .ascendingSort(Name)
            .toList()
            .map(mapToScenario)
    }

    override fun findById(id: String): Scenario? {
        return scenarioDatabase.findOneById(id.toId<Scenario>())?.mapToScenario()
    }

    override fun create(scenario: Scenario): Scenario {
        if(isIdPresent(scenario)) {
            throw TockIllegaleArgumentException("scenario musn't have id")
        }
        return save(scenario.mapToScenarioCol()).mapToScenario()
    }

    override fun update(scenario: Scenario): Scenario {
        if(!isIdPresent(scenario)) {
            throw TockIllegaleArgumentException("scenario must have id")
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