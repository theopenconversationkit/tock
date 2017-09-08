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
import fr.vsct.tock.nlp.front.shared.build.ModelBuild
import fr.vsct.tock.nlp.front.shared.build.ModelBuildQueryResult
import fr.vsct.tock.nlp.front.shared.build.ModelBuildTrigger
import org.litote.kmongo.deleteMany
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.find
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.save
import org.litote.kmongo.sort
import java.util.Locale

/**
 *
 */
object ModelBuildTriggerMongoDAO : ModelBuildTriggerDAO {

    private val col: MongoCollection<ModelBuildTrigger> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<ModelBuildTrigger>()
        c.ensureIndex("{'applicationId':1}")
        c
    }

    private val modelCol: MongoCollection<ModelBuild> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<ModelBuild>()
        c.ensureIndex("{'applicationId':1,'language':1}")
        c.ensureIndex("{'applicationId':1,'language':1,'date':1}")
        c
    }

    override fun save(trigger: ModelBuildTrigger) {
        col.save(trigger)
    }

    override fun deleteTrigger(trigger: ModelBuildTrigger) {
        col.deleteMany("{'applicationId':${trigger.applicationId.json},'onlyIfModelNotExists':${trigger.onlyIfModelNotExists}}")
    }

    override fun getTriggers(): List<ModelBuildTrigger> {
        return col.find().toList()
    }

    override fun save(build: ModelBuild) {
        modelCol.save(build)
    }

    override fun builds(applicationId: String, language: Locale, start: Int, size: Int): ModelBuildQueryResult {
        val filter = "{'applicationId':${applicationId.json},'language':${language.json}}"

        return ModelBuildQueryResult(
                modelCol.count(),
                modelCol
                        .find(filter)
                        .sort("{date:1}")
                        .skip(start)
                        .limit(size)
                        .toList()
        )
    }
}