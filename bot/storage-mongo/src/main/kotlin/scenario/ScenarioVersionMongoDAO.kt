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
package scenario

import ai.tock.bot.admin.scenario.ScenarioGroup
import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.bot.admin.scenario.ScenarioVersionDAO
import ai.tock.bot.admin.scenario.ScenarioVersionState
import ai.tock.bot.mongo.MongoBotConfiguration
import ai.tock.shared.exception.scenario.version.ScenarioVersionNotFoundException
import com.mongodb.client.result.DeleteResult
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollectionOfName
import org.litote.kmongo.updateOne
import java.time.ZonedDateTime

internal object ScenarioVersionMongoDAO : ScenarioVersionDAO {

    private val logger = KotlinLogging.logger {}

    internal val collection =
        MongoBotConfiguration.database
            .getCollectionOfName<ScenarioVersion>("scenario_version")

    override fun findOneById(id: Id<ScenarioVersion>): ScenarioVersion? {
        return collection.findOneById(id)
    }

    override fun findAll(): List<ScenarioVersion> {
        return collection.find().toList()
    }

    override fun findAllByScenarioGroupIdAndState(
        scenarioGroupId: Id<ScenarioGroup>,
        state: ScenarioVersionState
    ): List<ScenarioVersion> {
        return collection.find(and(
            ScenarioVersion::scenarioGroupId eq scenarioGroupId,
            ScenarioVersion::state eq state)
        ).toList()
    }

    override fun countAllByScenarioGroupId(scenarioGroupId: Id<ScenarioGroup>): Long {
        return collection.countDocuments(and(
            ScenarioVersion::scenarioGroupId eq scenarioGroupId))
    }

    override fun createOne(scenarioVersion: ScenarioVersion): ScenarioVersion {
        collection.insertOne(scenarioVersion)
        return scenarioVersion
    }

    override fun createMany(scenarioVersions: List<ScenarioVersion>): List<ScenarioVersion> {
        collection.insertMany(scenarioVersions)
        return scenarioVersions
    }

    override fun updateOne(scenarioVersion: ScenarioVersion): ScenarioVersion {
        val scenarioVersionDB = collection.findOneById(scenarioVersion._id)
        scenarioVersionDB ?: throw ScenarioVersionNotFoundException(scenarioVersion._id.toString())

        val scenarioVersionUpdated = with(scenarioVersion) {
            scenarioVersionDB.copy(
                data = data,
                state = state,
                comment = comment,
                updateDate = ZonedDateTime.now()
            )
        }

        collection.updateOne(scenarioVersionUpdated)
        return scenarioVersionUpdated
    }

    override fun deleteOneById(id: Id<ScenarioVersion>) {
        val result: DeleteResult = collection.deleteOneById(id)
        if(result.deletedCount == 0L) {
            throw ScenarioVersionNotFoundException(id.toString())
        }
    }

    override fun deleteAllByScenarioGroupId(
        scenarioGroupId: Id<ScenarioGroup>
    ) {
        val result: DeleteResult = collection.deleteMany(and(
            ScenarioVersion::scenarioGroupId eq scenarioGroupId))
        if(result.deletedCount == 0L) {
            throw ScenarioVersionNotFoundException(scenarioGroupId.toString())
        }
    }

}