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
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Sorts
import fr.vsct.tock.nlp.front.service.storage.TestModelDAO
import fr.vsct.tock.nlp.front.shared.test.EntityTestError
import fr.vsct.tock.nlp.front.shared.test.EntityTestErrorQueryResult
import fr.vsct.tock.nlp.front.shared.test.IntentTestError
import fr.vsct.tock.nlp.front.shared.test.IntentTestErrorQueryResult
import fr.vsct.tock.nlp.front.shared.test.TestBuild
import fr.vsct.tock.nlp.front.shared.test.TestErrorQuery
import org.litote.kmongo.count
import org.litote.kmongo.deleteOne
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.replaceOne
import org.litote.kmongo.save
import java.util.Locale

/**
 *
 */
object TestModelMongoDAO : TestModelDAO {

    private val buildCol: MongoCollection<TestBuild> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<TestBuild>()
        c.ensureIndex("{'applicationId':1,'language':1}")
        c
    }

    private val intentErrorCol: MongoCollection<IntentTestError> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<IntentTestError>()
        c.ensureIndex("{'applicationId':1,'language':1,'count':1}")
        c.ensureIndex("{'applicationId':1,'language':1,'text':1}", IndexOptions().unique(true))
        c
    }

    private val entityErrorCol: MongoCollection<EntityTestError> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<EntityTestError>()
        c.ensureIndex("{'applicationId':1,'language':1,'count':1}")
        c.ensureIndex("{'applicationId':1,'language':1,'text':1}", IndexOptions().unique(true))
        c
    }

    override fun getTestBuilds(applicationId: String, language: Locale): List<TestBuild> {
        return buildCol.find("{'language':${language.json},'applicationId':${applicationId.json}}").sort(Sorts.descending("startDate")).toList()
    }

    //"{'text':${textKey(sentence.text).json},'language':${sentence.language.json},'applicationId':${sentence.applicationId.json}}
    override fun saveTestBuild(build: TestBuild) {
        buildCol.insertOne(build)
    }

    override fun searchTestIntentErrors(query: TestErrorQuery): IntentTestErrorQueryResult {
        val filter = "{'language':${query.language.json},'applicationId':${query.applicationId.json}}"
        val count = intentErrorCol.count(filter).toInt()
        return if (count == 0) {
            IntentTestErrorQueryResult(0, emptyList())
        } else {
            IntentTestErrorQueryResult(
                    count,
                    intentErrorCol
                            .find(filter)
                            .sort(Sorts.descending("count"))
                            .skip(query.start.toInt())
                            .limit(query.size)
                            .toList()
            )
        }
    }

    override fun addTestIntentError(intentError: IntentTestError) {
        val filter = "{'text':${textKey(intentError.text).json},'language':${intentError.language.json},'applicationId':${intentError.applicationId.json}}"
        val newError = intentError.count != 0
        intentErrorCol.findOne(filter)
                ?.apply {
                    intentErrorCol.replaceOne(
                            filter,
                            copy(
                                    count = count + intentError.count,
                                    currentIntent = if (newError) intentError.currentIntent else currentIntent,
                                    wrongIntent = if (newError) intentError.wrongIntent else wrongIntent,
                                    averageErrorProbability = if (newError) (averageErrorProbability * total + intentError.averageErrorProbability) / (total + 1) else averageErrorProbability,
                                    total = total + 1
                            )
                    )
                }
                ?: if (newError) intentErrorCol.save(intentError.copy(text = textKey(intentError.text)))
    }

    override fun deleteTestIntentError(applicationId: String, language: Locale, text: String) {
        val filter = "{'text':${textKey(text).json},'language':${language.json},'applicationId':${applicationId.json}}"
        intentErrorCol.deleteOne(filter)
    }

    override fun searchTestEntityErrors(query: TestErrorQuery): EntityTestErrorQueryResult {
        val filter = "{'language':${query.language.json},'applicationId':${query.applicationId.json}}"
        val count = entityErrorCol.count(filter).toInt()
        return if (count == 0) {
            EntityTestErrorQueryResult(0, emptyList())
        } else {
            EntityTestErrorQueryResult(
                    count,
                    entityErrorCol
                            .find(filter)
                            .sort(Sorts.descending("count"))
                            .skip(query.start.toInt())
                            .limit(query.size)
                            .toList()
            )
        }
    }

    override fun addTestEntityError(entityError: EntityTestError) {
        val filter = "{'text':${textKey(entityError.text).json},'language':${entityError.language.json},'applicationId':${entityError.applicationId.json}}"
        val newError = entityError.count != 0
        entityErrorCol.findOne(filter)
                ?.apply {
                    entityErrorCol.replaceOne(
                            filter,
                            copy(
                                    count = count + entityError.count,
                                    intentId = entityError.intentId,
                                    lastAnalyse = if (newError) entityError.lastAnalyse else lastAnalyse,
                                    averageErrorProbability = if (newError) (averageErrorProbability * total + entityError.averageErrorProbability) / (total + 1) else averageErrorProbability,
                                    total = total + 1
                            )
                    )
                }
                ?: if (newError) entityErrorCol.save(entityError.copy(text = textKey(entityError.text)))
    }

    override fun deleteTestEntityError(applicationId: String, language: Locale, text: String) {
        val filter = "{'text':${textKey(text).json},'language':${language.json},'applicationId':${applicationId.json}}"
        entityErrorCol.deleteOne(filter)
    }
}