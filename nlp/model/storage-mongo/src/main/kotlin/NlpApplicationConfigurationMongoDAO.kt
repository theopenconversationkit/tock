/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

package ai.tock.nlp.model.service.storage.mongo

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.model.service.storage.NlpApplicationConfigurationDAO
import ai.tock.nlp.model.service.storage.mongo.MongoModelConfiguration.database
import ai.tock.nlp.model.service.storage.mongo.NlpApplicationConfigurationCol_.Companion.ApplicationName
import ai.tock.nlp.model.service.storage.mongo.NlpApplicationConfigurationCol_.Companion.Date
import ai.tock.nlp.model.service.storage.mongo.NlpApplicationConfigurationCol_.Companion.EngineType
import ai.tock.shared.ensureIndex
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.descending
import org.litote.kmongo.descendingSort
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.getCollection
import org.litote.kmongo.lt
import java.time.Instant
import java.time.Instant.now

/**
 *
 */
internal object NlpApplicationConfigurationMongoDAO : NlpApplicationConfigurationDAO {

    @JacksonData(internal = true)
    @Data(internal = true)
    data class NlpApplicationConfigurationCol(
        val applicationName: String,
        val engineType: NlpEngineType,
        val configuration: NlpApplicationConfiguration,
        val date: Instant = now()
    )

    private val col = database.getCollection<NlpApplicationConfigurationCol>("nlp_application_configuration")
        .apply {
            ensureIndex(descending(ApplicationName, EngineType, Date))
        }

    override fun saveNewConfiguration(
        applicationName: String,
        engineType: NlpEngineType,
        configuration: NlpApplicationConfiguration
    ) {
        col.insertOne(NlpApplicationConfigurationCol(applicationName, engineType, configuration))
    }

    override fun loadLastConfiguration(
        applicationName: String,
        engineType: NlpEngineType,
        updated: Instant
    ): NlpApplicationConfiguration? {
        return col
            .find(ApplicationName eq applicationName, EngineType eq engineType, Date lt updated)
            .descendingSort(Date)
            .limit(1)
            .firstOrNull()
            ?.configuration
    }
}
