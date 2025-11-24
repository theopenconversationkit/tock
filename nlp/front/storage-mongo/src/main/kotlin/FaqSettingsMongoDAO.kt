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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.service.storage.FaqSettingsDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.FaqSettings
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.replaceOneWithFilter

object FaqSettingsMongoDAO : FaqSettingsDAO {
    internal val col: MongoCollection<FaqSettings> by lazy {
        val c =
            MongoFrontConfiguration.database.getCollection<FaqSettings>().apply {
                ensureUniqueIndex(
                    FaqSettings::applicationId,
                )
            }
        c
    }

    override fun getFaqSettingsById(id: Id<FaqSettings>): FaqSettings? {
        return col.findOneById(id)
    }

    override fun deleteFaqSettingsById(id: Id<FaqSettings>) {
        col.deleteOneById(id)
    }

    override fun save(faqSettings: FaqSettings) {
        col.replaceOneWithFilter(
            and(
                FaqSettings::applicationId eq faqSettings.applicationId,
            ),
            faqSettings,
            ReplaceOptions().upsert(true),
        )
    }

    override fun getFaqSettingsByApplicationId(id: Id<ApplicationDefinition>): FaqSettings? {
        return col.findOne(FaqSettings::applicationId eq id)
    }
}
