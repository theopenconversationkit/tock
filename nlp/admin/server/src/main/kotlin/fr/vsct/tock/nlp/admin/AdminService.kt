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
import fr.vsct.tock.nlp.admin.model.LogsQuery
import fr.vsct.tock.nlp.admin.model.LogsReport
import fr.vsct.tock.nlp.admin.model.ParseQuery
import fr.vsct.tock.nlp.admin.model.SearchQuery
import fr.vsct.tock.nlp.admin.model.SentenceReport
import fr.vsct.tock.nlp.admin.model.SentencesReport
import fr.vsct.tock.nlp.admin.model.TestBuildStat
import fr.vsct.tock.nlp.admin.model.TranslateReport
import fr.vsct.tock.nlp.admin.model.TranslateSentencesQuery
import fr.vsct.tock.nlp.admin.model.UpdateSentencesQuery
import fr.vsct.tock.nlp.admin.model.UpdateSentencesReport
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.test.TestErrorQuery
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.provide
import fr.vsct.tock.shared.security.UNKNOWN_USER_LOGIN
import fr.vsct.tock.shared.vertx.WebVerticle.Companion.badRequest
import fr.vsct.tock.shared.withNamespace
import fr.vsct.tock.translator.TranslatorEngine
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.Duration
import java.time.Instant.now

/**
 *
 */
object AdminService {

    val front = FrontClient

    fun parseSentence(query: ParseQuery): SentenceReport {
        val result = front.parse(query.toQuery())
        val intentId =
            if (result.intent.withNamespace(result.intentNamespace) == Intent.UNKNOWN_INTENT_NAME) Intent.UNKNOWN_INTENT_NAME.toId()
            else front.getIntentIdByQualifiedName(result.intent.withNamespace(result.intentNamespace))!!
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
        return SentenceReport(result, query.currentLanguage, application._id, intentId)
    }

    fun searchSentences(query: SearchQuery, encryptSentences: Boolean): SentencesReport {
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
        val result = front.search(query.toSentencesQuery(application!!._id))
        return SentencesReport(query.start, result, encryptSentences)
    }

    fun updateSentences(query: UpdateSentencesQuery): UpdateSentencesReport {
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
        val sentences = if (query.searchQuery == null) {
            query.selectedSentences.filter { it.applicationId == application._id }.map { it.toClassifiedSentence() }
        } else {
            front.search(query.searchQuery.toSentencesQuery(application._id)).sentences
        }
        return if (query.newIntentId != null && application.intents.contains(query.newIntentId)) {
            val nbUpdates = front.switchSentencesIntent(sentences, application, query.newIntentId)
            UpdateSentencesReport(nbUpdates)
        } else if (query.oldEntity != null && query.newEntity != null) {
            val nbUpdates = front.switchSentencesEntity(sentences, application, query.oldEntity, query.newEntity)
            UpdateSentencesReport(nbUpdates)
        } else {
            UpdateSentencesReport()
        }
    }

    fun translateSentences(query: TranslateSentencesQuery): TranslateReport {
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
        val sentences = if (query.searchQuery == null) {
            query.selectedSentences.filter { it.applicationId == application._id }.map { it.toClassifiedSentence() }
        } else {
            front.search(query.searchQuery.toSentencesQuery(application._id)).sentences
        }
        val engine: TranslatorEngine = injector.provide()
        if(!engine.supportAdminTranslation) {
            badRequest("Translation is not activated for this account")
        }
        sentences.forEach {
            val translation = engine.translate(it.text, it.language, query.targetLanguage)
            val translatedSentence = it.copy(
                text = translation,
                language = query.targetLanguage,
                //for now entities are not kept during translation
                classification = it.classification.copy(entities = emptyList()),
                status = if (it.status == model) validated else it.status,
                usageCount = 0,
                unknownCount = 0,
                creationDate = now(),
                updateDate = now()
            )
            //TODO not not override existing sentences
            front.save(translatedSentence, it.qualifier ?: UNKNOWN_USER_LOGIN)
        }
        return TranslateReport(sentences.size)
    }

    fun getApplicationWithIntents(applicationId: Id<ApplicationDefinition>): ApplicationWithIntents? {
        val application = front.getApplicationById(applicationId)
        return application?.let { getApplicationWithIntents(it) }
    }

    fun getApplicationWithIntents(application: ApplicationDefinition): ApplicationWithIntents {
        val intents = front.getIntentsByApplicationId(application._id).sortedBy { it.name }
        return ApplicationWithIntents(application, intents)
    }

    fun createOrGetIntent(namespace: String, intent: IntentDefinition): IntentDefinition? {
        return if (namespace == intent.namespace) {
            val intentId = front.getIntentIdByQualifiedName(intent.qualifiedName)
            if (intentId == null) {
                front.save(intent)
                intent.applications.forEach { appId ->
                    front.save(front.getApplicationById(appId)!!.let { it.copy(intents = it.intents + intent._id) })
                }
                intent
            } else {
                front.getIntentById(intentId)
            }
        } else {
            null
        }
    }

    fun createOrUpdateIntent(namespace: String, intent: IntentDefinition): IntentDefinition? {
        return if (namespace == intent.namespace) {
            val intentId = front.getIntentIdByQualifiedName(intent.qualifiedName)
            if (intentId == null) {
                front.save(intent)
                intent.applications.forEach { appId ->
                    front.save(front.getApplicationById(appId)!!.let { it.copy(intents = it.intents + intent._id) })
                }
                intent
            } else {
                val oldIntent = front.getIntentById(intentId)!!.run {
                    copy(
                        label = intent.label,
                        description = intent.description,
                        category = intent.category,
                        applications = applications + intent.applications,
                        entities = intent.entities + entities.filter { e -> intent.entities.none { it.role == e.role } },
                        entitiesRegexp = entitiesRegexp + intent.entitiesRegexp,
                        mandatoryStates = intent.mandatoryStates + mandatoryStates,
                        sharedIntents = intent.sharedIntents + sharedIntents
                    )
                }
                front.save(oldIntent)
                oldIntent
            }
        } else {
            null
        }
    }

    fun searchLogs(query: LogsQuery): LogsReport {
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
        val applicationId = application!!._id
        val result = front.search(query.toParseRequestLogQuery(applicationId))
        return LogsReport(
            query.start,
            result,
            applicationId
        ) { front.getIntentIdByQualifiedName(it.withNamespace(query.namespace)) }
    }

    fun searchTestIntentErrors(query: TestErrorQuery, encryptSentences: Boolean): IntentTestErrorQueryResultReport {
        return front.searchTestIntentErrors(query)
            .run {
                IntentTestErrorQueryResultReport(
                    total,
                    data.map {
                        IntentTestErrorWithSentenceReport(it, encryptSentences)
                    }
                )
            }
    }

    fun searchTestEntityErrors(query: TestErrorQuery, encryptSentences: Boolean): EntityTestErrorQueryResultReport {
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
                            )
                        )
                        if (s.total == 0L) {
                            null
                        } else {
                            EntityTestErrorWithSentenceReport(
                                SentenceReport(s.sentences.first()),
                                it,
                                encryptSentences
                            )
                        }
                    }
                )
            }
    }

    fun testBuildStats(query: ApplicationScopedQuery): List<TestBuildStat> {
        val app = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
        val stats = front
            .getTestBuilds(app._id, query.currentLanguage)
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