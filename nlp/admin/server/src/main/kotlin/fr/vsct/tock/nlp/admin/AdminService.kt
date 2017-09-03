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

package fr.vsct.tock.nlp.admin

import fr.vsct.tock.nlp.admin.model.ApplicationScopedQuery
import fr.vsct.tock.nlp.admin.model.ApplicationWithIntents
import fr.vsct.tock.nlp.admin.model.EntityTestErrorQueryResultReport
import fr.vsct.tock.nlp.admin.model.EntityTestErrorWithSentenceReport
import fr.vsct.tock.nlp.admin.model.IntentTestErrorQueryResultReport
import fr.vsct.tock.nlp.admin.model.IntentTestErrorWithSentenceReport
import fr.vsct.tock.nlp.admin.model.LogsReport
import fr.vsct.tock.nlp.admin.model.ParseQuery
import fr.vsct.tock.nlp.admin.model.SearchLogsQuery
import fr.vsct.tock.nlp.admin.model.SearchQuery
import fr.vsct.tock.nlp.admin.model.SentenceReport
import fr.vsct.tock.nlp.admin.model.SentencesReport
import fr.vsct.tock.nlp.admin.model.TestBuildStat
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.test.TestErrorQuery
import fr.vsct.tock.shared.withNamespace
import java.time.Duration

/**
 *
 */
object AdminService {

    val front = FrontClient

    fun parseSentence(query: ParseQuery): SentenceReport {
        val result = front.parse(query.toQuery())
        val intentId = if (result.intent.withNamespace(result.intentNamespace) == Intent.UNKNOWN_INTENT) Intent.UNKNOWN_INTENT
        else front.getIntentIdByQualifiedName(result.intent.withNamespace(query.namespace))!!
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
        return SentenceReport(result, query.language, application._id!!, intentId)
    }

    fun searchSentences(query: SearchQuery): SentencesReport {
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
        val result = front.search(query.toSentencesQuery(application!!._id!!))
        return SentencesReport(query.start, result)
    }

    fun getApplicationWithIntents(applicationId: String): ApplicationWithIntents? {
        val application = front.getApplicationById(applicationId)
        return application?.let { getApplicationWithIntents(it) }
    }

    fun getApplicationWithIntents(application: ApplicationDefinition): ApplicationWithIntents {
        val intents = front.getIntentsByApplicationId(application._id!!).sortedBy { it.name }
        return ApplicationWithIntents(application, intents)
    }

    fun createOrUpdateIntent(namespace: String, intent: IntentDefinition): IntentDefinition? {
        return if (namespace == intent.namespace
                && intent._id == front.getIntentIdByQualifiedName(intent.qualifiedName)) {
            front.save(intent)
            intent.applications.forEach {
                front.save(front.getApplicationById(it)!!.let { it.copy(intents = it.intents + intent._id!!) })
            }
            intent
        } else {
            null
        }
    }

    fun searchLogs(query: SearchLogsQuery): LogsReport {
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
        val applicationId = application!!._id!!
        val result = front.search(query.toParseRequestLogQuery(applicationId))
        return LogsReport(query.start, result, applicationId, { front.getIntentIdByQualifiedName(it.withNamespace(query.namespace)) })
    }

    fun searchTestIntentErrors(query: TestErrorQuery): IntentTestErrorQueryResultReport {
        return front.searchTestIntentErrors(query)
                .run {
                    IntentTestErrorQueryResultReport(
                            total,
                            data.map {
                                IntentTestErrorWithSentenceReport(it)
                            }
                    )
                }
    }

    fun searchTestEntityErrors(query: TestErrorQuery): EntityTestErrorQueryResultReport {
        return front.searchTestEntityErrors(query)
                .run {
                    EntityTestErrorQueryResultReport(
                            total,
                            data.mapNotNull {
                                val s = front.search(
                                        SentencesQuery(
                                                it.applicationId,
                                                it.language,
                                                search = it.text,
                                                onlyExactMatch = true
                                        ))
                                if (s.total == 0L) {
                                    null
                                } else {
                                    EntityTestErrorWithSentenceReport(
                                            SentenceReport(s.sentences.first()),
                                            it)
                                }
                            }
                    )
                }
    }

    fun testBuildStats(query: ApplicationScopedQuery): List<TestBuildStat> {
        val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
        val stats = front
                .getTestBuilds(app._id!!, query.language)
                .map {
                    TestBuildStat(
                            it.startDate,
                            it.nbErrors,
                            it.nbSentencesInModel,
                            it.nbSentencesTested,
                            it.buildModelDuration,
                            it.testSentencesDuration
                    )
                }
                .sortedBy { it.date }
        //only one point each 10 minutes
        return stats.filterIndexed { i, s ->
            i == 0 || Duration.between(stats[i - 1].date, s.date) >= Duration.ofMinutes(10)
        }
    }
}