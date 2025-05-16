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

import ai.tock.nlp.front.service.storage.ModelBuildTriggerDAO
import ai.tock.nlp.front.shared.build.ModelBuild
import ai.tock.nlp.front.shared.build.ModelBuildQueryResult
import ai.tock.nlp.front.shared.build.ModelBuildTrigger
import ai.tock.nlp.front.shared.build.ModelBuildTrigger_.Companion.ApplicationId
import ai.tock.nlp.front.shared.build.ModelBuildTrigger_.Companion.OnlyIfModelNotExists
import ai.tock.nlp.front.shared.build.ModelBuild_
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.ensureIndex
import com.mongodb.client.MongoCollection
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.deleteMany
import org.litote.kmongo.descendingSort
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import java.util.Locale

/**
 *
 */
internal object ModelBuildTriggerMongoDAO : ModelBuildTriggerDAO {

    private val col: MongoCollection<ModelBuildTrigger> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<ModelBuildTrigger>()
        c.ensureIndex(ApplicationId)
        c
    }

    private val modelCol: MongoCollection<ModelBuild> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<ModelBuild>()
        c.ensureIndex(ModelBuild_.ApplicationId, ModelBuild_.Language)
        c.ensureIndex(ModelBuild_.ApplicationId, ModelBuild_.Language, ModelBuild_.Date)
        c
    }

    override fun save(trigger: ModelBuildTrigger) {
        col.save(trigger)
    }

    override fun deleteTrigger(trigger: ModelBuildTrigger) {
        col.deleteMany(
            ApplicationId eq trigger.applicationId,
            OnlyIfModelNotExists eq trigger.onlyIfModelNotExists
        )
    }

    override fun getTriggers(): List<ModelBuildTrigger> {
        return col.find().toList()
    }

    override fun save(build: ModelBuild) {
        modelCol.save(build)
    }

    override fun builds(
        applicationId: Id<ApplicationDefinition>,
        language: Locale,
        start: Int,
        size: Int
    ): ModelBuildQueryResult {
        val filter = and(
            ModelBuild_.ApplicationId eq applicationId,
            ModelBuild_.Language eq language
        )

        return ModelBuildQueryResult(
            modelCol.countDocuments(filter),
            modelCol
                .find(filter)
                .descendingSort(ModelBuild_.Date)
                .skip(start)
                .limit(size)
                .toList()
        )
    }
}
