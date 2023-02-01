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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.service.storage.ScenarioSettingsDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ScenarioSettings
import ai.tock.shared.watch
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.replaceOneWithFilter



object ScenarioSettingsMongoDAO : ScenarioSettingsDAO {

    internal val col: MongoCollection<ScenarioSettings> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<ScenarioSettings>().apply {
            ensureUniqueIndex(
                ScenarioSettings::applicationId
            )
        }
        c
    }

    private val asyncCol = MongoFrontConfiguration.asyncDatabase.getCollection<ScenarioSettings>()

    override fun save(scenarioSettings: ScenarioSettings) {
        col.replaceOneWithFilter(
            and(
                ScenarioSettings::applicationId eq scenarioSettings.applicationId,
            ),
            scenarioSettings,
            ReplaceOptions().upsert(true)
        )
    }

    override fun getScenarioSettingsByApplicationId(id: Id<ApplicationDefinition>): ScenarioSettings? {
        return col.findOne(ScenarioSettings::applicationId eq id)
    }

    override fun listenChanges(listener: (ScenarioSettings) -> Unit) {
        asyncCol.watch {
            it.fullDocument?.let {  doc -> listener.invoke(doc) }
        }
    }

}