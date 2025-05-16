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

import ai.tock.nlp.front.service.storage.TestModelDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.test.EntityTestError
import ai.tock.nlp.front.shared.test.EntityTestErrorQueryResult
import ai.tock.nlp.front.shared.test.EntityTestError_
import ai.tock.nlp.front.shared.test.EntityTestError_.Companion.IntentId
import ai.tock.nlp.front.shared.test.IntentTestError
import ai.tock.nlp.front.shared.test.IntentTestErrorQueryResult
import ai.tock.nlp.front.shared.test.IntentTestError_
import ai.tock.nlp.front.shared.test.IntentTestError_.Companion.Count
import ai.tock.nlp.front.shared.test.IntentTestError_.Companion.CurrentIntent
import ai.tock.nlp.front.shared.test.IntentTestError_.Companion.Text
import ai.tock.nlp.front.shared.test.TestBuild
import ai.tock.nlp.front.shared.test.TestBuild_.Companion.ApplicationId
import ai.tock.nlp.front.shared.test.TestBuild_.Companion.Language
import ai.tock.nlp.front.shared.test.TestBuild_.Companion.StartDate
import ai.tock.nlp.front.shared.test.TestErrorQuery
import ai.tock.shared.ensureIndex
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.name
import ai.tock.shared.namespace
import com.mongodb.client.MongoCollection
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.descendingSort
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.gte
import org.litote.kmongo.save
import java.util.Locale

/**
 *
 */
internal object TestModelMongoDAO : TestModelDAO {

    private val buildCol: MongoCollection<TestBuild> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<TestBuild>()
        c.ensureIndex(ApplicationId, Language)
        c.ensureIndex(StartDate)
        c
    }

    private val intentErrorCol: MongoCollection<IntentTestError> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<IntentTestError>()
        c.ensureIndex(IntentTestError_.ApplicationId, IntentTestError_.Language, Count)
        c.ensureUniqueIndex(IntentTestError_.ApplicationId, IntentTestError_.Language, Text)
        c
    }

    private val entityErrorCol: MongoCollection<EntityTestError> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<EntityTestError>()
        c.ensureIndex(EntityTestError_.ApplicationId, EntityTestError_.Language, EntityTestError_.Count)
        c.ensureUniqueIndex(EntityTestError_.ApplicationId, EntityTestError_.Language, EntityTestError_.Text)
        c
    }

    override fun getTestBuilds(query: TestErrorQuery): List<TestBuild> {
        return with(query) {
            buildCol.find(
                and(
                    Language eq language,
                    ApplicationId eq applicationId,
                    if (after != null) StartDate gte after else null
                )
            )
                .descendingSort(StartDate)
                .mapNotNull {
                    val intent = intentName
                    if (intent == null) {
                        it
                    } else {
                        val intentErrors = it.intentErrorsByIntent[intent] ?: 0
                        val entityErrors = it.entityErrorsByIntent[intent] ?: 0
                        it.copy(
                            nbSentencesTested = it.nbSentencesTestedByIntent[intent] ?: 0,
                            nbErrors = intentErrors + entityErrors,
                            intentErrors = intentErrors,
                            entityErrors = entityErrors
                        )
                    }
                }
                .toList()
        }
    }

    override fun saveTestBuild(build: TestBuild) {
        buildCol.insertOne(build)
    }

    override fun searchTestIntentErrors(query: TestErrorQuery): IntentTestErrorQueryResult {
        val filter = and(
            Language eq query.language,
            ApplicationId eq query.applicationId,
            if (query.intentName == null) null else CurrentIntent eq query.intentName
        )
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
            ?: if (newError) intentErrorCol.save(intentError.copy(text = textKey(intentError.text))) else Unit
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
        val filter = and(
            Language eq query.language,
            ApplicationId eq query.applicationId,
            query.intentName?.let { intentName ->
                IntentDefinitionMongoDAO.getIntentByNamespaceAndName(intentName.namespace(), intentName.name())
                    ?.let { IntentId eq it._id }
            }
        )
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
            ?: if (newError) entityErrorCol.save(entityError.copy(text = textKey(entityError.text))) else Unit
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
