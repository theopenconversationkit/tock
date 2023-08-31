/*
 * Copyright (C) 2017/2023 e-voyageurs technologies
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

package indicator

import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.IndicatorDAO
import ai.tock.bot.mongo.MongoBotConfiguration
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.deleteOne
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollectionOfName
import org.litote.kmongo.save

object IndicatorMongoDAO : IndicatorDAO {

    internal val col =
        MongoBotConfiguration.database.getCollectionOfName<Indicator>("indicator").also {
            it.ensureUniqueIndex(
                Indicator::name,
                Indicator::botId
            )
        }

    override fun save(indicator: Indicator) = col.save(indicator)

    override fun existByNameAndBotId(name: String, botId: String): Boolean {
        return (col.countDocuments(
            and(
                Indicator::botId eq botId,
                Indicator::name eq name,
            )
        ) > 0)
    }

    override fun findByNameAndBotId(name: String, botId: String): Indicator? =
        col.findOne(
            and(
                Indicator::name eq name,
                Indicator::botId eq botId
            )
        )

    override fun findAllByBotId(botId: String): List<Indicator> = col.find(Indicator::botId eq botId).toList()

    override fun findAll(): List<Indicator> = col.find().toList()

    override fun delete(id: Id<Indicator>) = col.deleteOne(Indicator::_id eq id).deletedCount == 1L

    override fun deleteByNameAndApplicationName(name: String, applicationName: String): Boolean =
        col.deleteOne(Indicator::name eq name, Indicator::botId eq applicationName).deletedCount == 1L

}