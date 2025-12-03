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

package ai.tock.nlp.admin

import ai.tock.nlp.admin.model.ApplicationWithIntents
import ai.tock.nlp.admin.model.EntityTestErrorQueryResultReport
import ai.tock.nlp.admin.model.EntityTestErrorWithSentenceReport
import ai.tock.nlp.admin.model.IntentTestErrorQueryResultReport
import ai.tock.nlp.admin.model.IntentTestErrorWithSentenceReport
import ai.tock.nlp.admin.model.LogsQuery
import ai.tock.nlp.admin.model.LogsReport
import ai.tock.nlp.admin.model.ParseQuery
import ai.tock.nlp.admin.model.SearchQuery
import ai.tock.nlp.admin.model.SentenceReport
import ai.tock.nlp.admin.model.SentencesReport
import ai.tock.nlp.admin.model.TestBuildQuery
import ai.tock.nlp.admin.model.TestBuildStat
import ai.tock.nlp.admin.model.TranslateReport
import ai.tock.nlp.admin.model.TranslateSentencesQuery
import ai.tock.nlp.admin.model.UpdateSentencesQuery
import ai.tock.nlp.admin.model.UpdateSentencesReport
import ai.tock.nlp.core.Intent.Companion.RAG_EXCLUDED_INTENT_NAME
import ai.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedEntity
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.test.TestErrorQuery
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.UNKNOWN_USER_LOGIN
import ai.tock.shared.vertx.WebVerticle.Companion.badRequest
import ai.tock.shared.withNamespace
import ai.tock.translator.TranslatorEngine
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.Duration
import java.time.Instant.now
import java.time.temporal.ChronoUnit.MINUTES

/**
 *
 */
object AdminService {
    val front = FrontClient

    fun parseSentence(query: ParseQuery): SentenceReport {
        val result = front.parse(query.toQuery())
        val intentWithNamespace = result.intent.withNamespace(result.intentNamespace)
        val intentId =
            when (intentWithNamespace) {
                UNKNOWN_INTENT_NAME -> UNKNOWN_INTENT_NAME.toId()
                RAG_EXCLUDED_INTENT_NAME -> RAG_EXCLUDED_INTENT_NAME.toId()
                else -> front.getIntentIdByQualifiedName(intentWithNamespace)!!
            }
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
        return SentenceReport(result, query.currentLanguage, application._id, intentId)
    }

    fun searchSentences(query: SearchQuery): SentencesReport {
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
        val result = front.search(query.toSentencesQuery(application!!._id))
        return SentencesReport(query.start, result)
    }

    fun updateSentences(query: UpdateSentencesQuery): UpdateSentencesReport {
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
        val sentences =
            if (query.searchQuery == null) {
                query.selectedSentences.filter { it.applicationId == application._id }.map { it.toClassifiedSentence() }
            } else {
                front.search(query.searchQuery.toSentencesQuery(application._id)).sentences
            }
        return if (query.newIntentId != null &&
            (query.unknownNewIntent || query.ragExcludedNewIntent || application.intents.contains(query.newIntentId))
        ) {
            val nbUpdates = front.switchSentencesIntent(sentences, application, query.newIntentId)
            UpdateSentencesReport(nbUpdates)
        } else if (query.oldEntity != null && query.newEntity != null) {
            val nbUpdates = front.switchSentencesEntity(sentences, application, query.oldEntity, query.newEntity)
            UpdateSentencesReport(nbUpdates)
        } else if (query.newStatus != null) {
            front.switchSentencesStatus(sentences, query.newStatus)
            UpdateSentencesReport(sentences.size)
        } else {
            UpdateSentencesReport()
        }
    }

    fun translateSentences(query: TranslateSentencesQuery): TranslateReport {
        val application = front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
        val sentences =
            if (query.searchQuery == null) {
                query.selectedSentences.filter { it.applicationId == application._id }.map { it.toClassifiedSentence() }
            } else {
                front.search(query.searchQuery.toSentencesQuery(application._id)).sentences
            }
        val engine: TranslatorEngine = injector.provide()
        if (!engine.supportAdminTranslation) {
            badRequest("Translation is not activated for this account")
        }
        sentences.forEach { s ->
            val translation = engine.translate(s.text, s.language, query.targetLanguage)
            if (front.search(
                    SentencesQuery(
                        application._id,
                        query.targetLanguage,
                        search = translation,
                        status = setOf(validated, model),
                        onlyExactMatch = true,
                        normalizeText = application.normalizeText,
                    ),
                ).sentences.isEmpty()
            ) {
                val toLowerCaseTranslation = translation.lowercase(s.language)
                val newEntities = mutableListOf<ClassifiedEntity>()
                val entities =
                    s.classification.entities.mapNotNull { e ->
                        val originalEntityText = e.textValue(s.text)
                        val entityTranslation = engine.translate(originalEntityText, s.language, query.targetLanguage)
                        val index = toLowerCaseTranslation.indexOf(entityTranslation.lowercase(query.targetLanguage))
                        val start = if (index != -1) index else toLowerCaseTranslation.indexOf(originalEntityText.lowercase())
                        val end = start + if (index != -1) entityTranslation.length else originalEntityText.length
                        if (start == -1 || newEntities.any { it.overlap(start, end) }) {
                            null
                        } else {
                            e.copy(
                                start = start,
                                end = end,
                                // sub entities not yet supported
                                subEntities = emptyList(),
                            )
                                .apply { newEntities.add(this) }
                        }
                    }.sorted()
                val translatedSentence =
                    s.copy(
                        text = translation,
                        language = query.targetLanguage,
                        classification = s.classification.copy(entities = entities),
                        status = if (s.status == model) validated else s.status,
                        usageCount = 0,
                        unknownCount = 0,
                        creationDate = now(),
                        updateDate = now(),
                    )
                front.save(translatedSentence, s.qualifier ?: UNKNOWN_USER_LOGIN)
            }
        }
        return TranslateReport(sentences.size)
    }

    fun getApplicationWithIntents(applicationId: Id<ApplicationDefinition>): ApplicationWithIntents? {
        val application = front.getApplicationById(applicationId)
        return application?.let { getApplicationWithIntents(it) }
    }

    fun getApplicationWithIntents(application: ApplicationDefinition): ApplicationWithIntents {
        val intents = front.getIntentsByApplicationId(application._id).sortedBy { it.name }
        return ApplicationWithIntents(application, intents, front.getModelSharedIntents(application.namespace))
    }

    fun createOrGetIntent(
        namespace: String,
        intent: IntentDefinition,
    ): IntentDefinition? {
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

    /**
     * Create or Update Intent and search the existing one by the intent qualifiedName
     */
    fun createOrUpdateIntent(
        namespace: String,
        intent: IntentDefinition,
    ): IntentDefinition? {
        return if (namespace == intent.namespace) {
            val intentId = front.getIntentIdByQualifiedName(intent.qualifiedName)
            (
                if (intentId == null) {
                    intent
                } else {
                    front.getIntentById(intentId)!!.run {
                        copy(
                            label = intent.label,
                            description = intent.description,
                            category = intent.category,
                            applications = applications + intent.applications,
                            entities = intent.entities + entities.filter { e -> intent.entities.none { it.role == e.role } },
                            entitiesRegexp = entitiesRegexp + intent.entitiesRegexp,
                            mandatoryStates = intent.mandatoryStates + mandatoryStates,
                            sharedIntents = intent.sharedIntents + sharedIntents,
                        )
                    }
                }
            ).apply {
                front.save(this)
                applications.forEach { appId ->
                    front.getApplicationById(appId)?.also {
                        front.save(it.copy(intents = it.intents + _id))
                    }
                }
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
            applicationId,
        ) { front.getIntentIdByQualifiedName(it.withNamespace(query.namespace)) }
    }

    fun searchTestIntentErrors(query: TestErrorQuery): IntentTestErrorQueryResultReport {
        return front.searchTestIntentErrors(query)
            .run {
                IntentTestErrorQueryResultReport(
                    total,
                    data.mapNotNull {
                        val s =
                            front.search(
                                SentencesQuery(
                                    it.applicationId,
                                    it.language,
                                    search = it.text,
                                    onlyExactMatch = true,
                                ),
                            )
                        if (s.total == 0L) {
                            null
                        } else {
                            IntentTestErrorWithSentenceReport(
                                s.sentences.first(),
                                it,
                            )
                        }
                    },
                )
            }
    }

    internal fun ClassifiedSentence.obfuscatedEntityRanges(): List<IntRange> = classification.entities.filter { front.isEntityTypeObfuscated(it.type) }.map { it.toClosedRange() }

    fun searchTestEntityErrors(query: TestErrorQuery): EntityTestErrorQueryResultReport {
        return front.searchTestEntityErrors(query)
            .run {
                val results =
                    data.mapNotNull {
                        val s =
                            front.search(
                                SentencesQuery(
                                    it.applicationId,
                                    it.language,
                                    search = it.text,
                                    onlyExactMatch = true,
                                ),
                            )
                        if (s.total == 0L) {
                            null
                        } else {
                            EntityTestErrorWithSentenceReport(
                                s.sentences.first(),
                                it,
                            )
                        }
                    }
                EntityTestErrorQueryResultReport(total, results)
            }
    }

    fun testBuildStats(
        query: TestBuildQuery,
        app: ApplicationDefinition,
    ): List<TestBuildStat> {
        val stats =
            front
                .getTestBuilds(query.toTestErrorQuery(app))
                .map {
                    TestBuildStat(
                        it.startDate.truncatedTo(MINUTES),
                        it.nbErrors,
                        it.intentErrors,
                        it.entityErrors,
                        it.nbSentencesInModel,
                        it.nbSentencesTested,
                        it.buildModelDuration,
                        it.testSentencesDuration,
                    )
                }
                .sortedBy { it.date }
        // only one point each 1 minutes
        return stats.filterIndexed { i, s ->
            i == 0 || Duration.between(stats[i - 1].date, s.date) >= Duration.ofMinutes(1)
        }
    }

    fun deleteNamespaceIfEmpty(namespace: String) {
        // Check that no application is linked to namespace
        if (FrontClient.getApplications().any { it.namespace == namespace }) {
            throw IllegalStateException("Namespace '$namespace' still contains applications.")
        }
        // Delete all user/namespace associations
        FrontClient.getUsers(namespace).forEach { userNamespace ->
            FrontClient.deleteNamespace(userNamespace.login, namespace)
        }
    }
}
