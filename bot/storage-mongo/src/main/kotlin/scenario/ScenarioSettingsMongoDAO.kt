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


import ai.tock.bot.admin.scenario.ScenarioSettings
import ai.tock.bot.admin.scenario.ScenarioSettingsDAO
import ai.tock.bot.mongo.MongoBotConfiguration
import ai.tock.shared.watch
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.replaceOneWithFilter
import org.litote.kmongo.and
import org.litote.kmongo.reactivestreams.getCollection


object ScenarioSettingsMongoDAO : ScenarioSettingsDAO{

    private val col: MongoCollection<ScenarioSettings> by lazy {
        val c = MongoBotConfiguration.database.getCollection<ScenarioSettings>().apply {
            ensureUniqueIndex(
                ScenarioSettings::botId
            )
        }
        c
    }

    private val asyncCol = MongoBotConfiguration.asyncDatabase.getCollection<ScenarioSettings>()
    override fun save(scenarioSettings: ScenarioSettings) {
        col.replaceOneWithFilter(
            and(
                ScenarioSettings::botId eq scenarioSettings.botId,
            ),
            scenarioSettings,
            ReplaceOptions().upsert(true)
        )
    }

    override fun getScenarioSettingsByBotId(id: String): ScenarioSettings? {
        return col.findOne(ScenarioSettings::botId eq id)
    }

    override fun listenChanges(listener: (ScenarioSettings) -> Unit) {
        asyncCol.watch {
            it.fullDocument?.let {  doc -> listener.invoke(doc) }
        }
    }
}