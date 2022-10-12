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
import ai.tock.bot.admin.scenario.ScenarioGroupDAO
import ai.tock.bot.mongo.MongoBotConfiguration
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.exception.scenario.group.ScenarioGroupDuplicatedException
import ai.tock.shared.exception.scenario.group.ScenarioGroupNotFoundException
import ai.tock.shared.warn
import com.mongodb.MongoWriteException
import com.mongodb.client.result.DeleteResult
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.aggregate
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.eq
import org.litote.kmongo.getCollectionOfName
import org.litote.kmongo.lookup
import org.litote.kmongo.match
import org.litote.kmongo.updateOne
import java.time.ZonedDateTime

internal object ScenarioGroupMongoDAO : ScenarioGroupDAO {

    private val logger = KotlinLogging.logger {}

    internal val collection =
        MongoBotConfiguration.database.
        getCollectionOfName<ScenarioGroup>("scenario_group")

    init {
        try {
            collection.ensureUniqueIndex(
                ScenarioGroup::botId, ScenarioGroup::name)
        } catch (e: Exception) {
            logger.warn(e)
        }
    }

    override fun findOneById(id: Id<ScenarioGroup>): ScenarioGroup? {
        return collection.aggregate<ScenarioGroup>(lookup(
                from = "scenario_version",
                localField = "_id",
                foreignField = "scenarioGroupId",
                newAs = "versions"
            ),
            match(ScenarioGroup::_id eq id),
        ).first()
    }

    override fun findAllByBotId(botId: String): List<ScenarioGroup> {
        return collection.aggregate<ScenarioGroup>(lookup(
                from = "scenario_version",
                localField = "_id",
                foreignField = "scenarioGroupId",
                newAs = "versions"
            ),
            match(ScenarioGroup::botId eq botId),
        ).toList()
    }

    override fun findAll(): List<ScenarioGroup> {
        return collection.find().toList()
    }


    override fun createOne(scenarioGroup: ScenarioGroup): ScenarioGroup {
        return try {
            collection.insertOne(scenarioGroup)
            scenarioGroup
        } catch (ex: MongoWriteException){
            if(ex.code == 11000) {
                logger.error { ex.message }
                throw ScenarioGroupDuplicatedException()
            } else {
                throw ex
            }
        }
    }


    override fun updateOne(scenarioGroup: ScenarioGroup): ScenarioGroup {
        val scenarioGroupDB = collection.aggregate<ScenarioGroup>(
            match(ScenarioGroup::_id eq scenarioGroup._id),
            lookup(
                from = "scenario_version",
                localField = "_id",
                foreignField = "scenarioGroupId",
                newAs = "versions"
            )
        ).firstOrNull()

        scenarioGroupDB ?: throw ScenarioGroupNotFoundException(scenarioGroup._id.toString())

        val scenarioGroupUpdated = scenarioGroupDB.copy(
            name = scenarioGroup.name,
            category = scenarioGroup.category,
            tags = scenarioGroup.tags,
            description = scenarioGroup.description,
            updateDate = ZonedDateTime.now()
        )

        collection.updateOne(scenarioGroupUpdated)
        return scenarioGroupUpdated
    }

    override fun deleteOneById(scenarioGroupId: Id<ScenarioGroup>) {
        val result: DeleteResult = collection.deleteOneById(scenarioGroupId)
        if(result.deletedCount == 0L) {
            throw ScenarioGroupNotFoundException(scenarioGroupId.toString())
        }
    }

}