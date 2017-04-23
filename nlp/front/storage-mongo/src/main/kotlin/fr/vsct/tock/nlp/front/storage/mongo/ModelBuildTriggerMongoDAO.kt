/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.front.storage.mongo

import com.mongodb.client.MongoCollection
import fr.vsct.tock.nlp.front.service.storage.ModelBuildTriggerDAO
import fr.vsct.tock.nlp.front.shared.updater.ModelBuildTrigger
import org.litote.kmongo.createIndex
import org.litote.kmongo.deleteMany
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.save

/**
 *
 */
object ModelBuildTriggerMongoDAO : ModelBuildTriggerDAO {

    private val col: MongoCollection<ModelBuildTrigger> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<ModelBuildTrigger>()
        c.createIndex("{'applicationId':1}")
        c
    }

    override fun save(trigger: ModelBuildTrigger) {
        col.save(trigger)
    }

    override fun deleteTriggersForApplicationId(applicationId: String) {
        col.deleteMany("{'applicationId':${applicationId.json}}")
    }

    override fun getTriggers(): List<ModelBuildTrigger> {
        return col.find().toList()
    }
}