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
import fr.vsct.tock.nlp.front.service.storage.TestModelDAO
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.test.EntityTestError
import fr.vsct.tock.nlp.front.shared.test.EntityTestErrorQueryResult
import fr.vsct.tock.nlp.front.shared.test.EntityTestError_
import fr.vsct.tock.nlp.front.shared.test.IntentTestError
import fr.vsct.tock.nlp.front.shared.test.IntentTestErrorQueryResult
import fr.vsct.tock.nlp.front.shared.test.IntentTestError_
import fr.vsct.tock.nlp.front.shared.test.IntentTestError_.Companion.Count
import fr.vsct.tock.nlp.front.shared.test.IntentTestError_.Companion.Text
import fr.vsct.tock.nlp.front.shared.test.TestBuild
import fr.vsct.tock.nlp.front.shared.test.TestBuild_.Companion.ApplicationId
import fr.vsct.tock.nlp.front.shared.test.TestBuild_.Companion.Language
import fr.vsct.tock.nlp.front.shared.test.TestBuild_.Companion.StartDate
import fr.vsct.tock.nlp.front.shared.test.TestErrorQuery
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.descendingSort
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import java.util.Locale

/**
 *
 */
internal object TestModelMongoDAO : TestModelDAO {

    private val buildCol: MongoCollection<TestBuild> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<TestBuild>()
        c.ensureIndex(ApplicationId, Language)
        c
    }

    private val intentErrorCol: MongoCollection<IntentTestError> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<IntentTestError>()
        c.ensureIndex(IntentTestError_.ApplicationId, IntentTestError_.Language, IntentTestError_.Count)
        c.ensureUniqueIndex(IntentTestError_.ApplicationId, IntentTestError_.Language, IntentTestError_.Text)
        c
    }

    private val entityErrorCol: MongoCollection<EntityTestError> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<EntityTestError>()
        c.ensureIndex(EntityTestError_.ApplicationId, EntityTestError_.Language, EntityTestError_.Count)
        c.ensureUniqueIndex(EntityTestError_.ApplicationId, EntityTestError_.Language, EntityTestError_.Text)
        c
    }

    override fun getTestBuilds(applicationId: Id<ApplicationDefinition>, language: Locale): List<TestBuild> {
        return buildCol.find(Language eq language, ApplicationId eq applicationId)
            .descendingSort(StartDate).toList()
    }

    override fun saveTestBuild(build: TestBuild) {
        buildCol.insertOne(build)
    }

    override fun searchTestIntentErrors(query: TestErrorQuery): IntentTestErrorQueryResult {
        val filter = and(Language eq query.language, ApplicationId eq query.applicationId)
        val count = intentErrorCol.countDocuments(filter).toInt()
        return if (count == 0) {
            IntentTestErrorQueryResult(0, emptyList())
        } else {
            IntentTestErrorQueryResult(
                count,
                intentErrorCol
                    .find(filter)
                    .descendingSort(Count)
                    .skip(query.start.toInt())
                    .limit(query.size)
                    .toList()
            )
        }
    }

    override fun addTestIntentError(intentError: IntentTestError) {
        val filter = and(
            Text eq textKey(intentError.text),
            Language eq intentError.language,
            ApplicationId eq intentError.applicationId
        )
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

    override fun deleteTestIntentError(applicationId: Id<ApplicationDefinition>, language: Locale, text: String) {
        intentErrorCol.deleteOne(
            and(
                Text eq textKey(text),
                Language eq language,
                ApplicationId eq applicationId
            )
        )
    }

    override fun searchTestEntityErrors(query: TestErrorQuery): EntityTestErrorQueryResult {
        val filter = and(Language eq query.language, ApplicationId eq query.applicationId)
        val count = entityErrorCol.countDocuments(filter).toInt()
        return if (count == 0) {
            EntityTestErrorQueryResult(0, emptyList())
        } else {
            EntityTestErrorQueryResult(
                count,
                entityErrorCol
                    .find(filter)
                    .descendingSort(Count)
                    .skip(query.start.toInt())
                    .limit(query.size)
                    .toList()
            )
        }
    }

    override fun addTestEntityError(entityError: EntityTestError) {
        val filter = and(
            Text eq textKey(entityError.text),
            Language eq entityError.language,
            ApplicationId eq entityError.applicationId
        )
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

    override fun deleteTestEntityError(applicationId: Id<ApplicationDefinition>, language: Locale, text: String) {
        entityErrorCol.deleteOne(
            and(
                Text eq textKey(text),
                Language eq language,
                ApplicationId eq applicationId
            )
        )
    }
}