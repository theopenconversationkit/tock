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

package ai.tock.nlp.front.service

import ai.tock.nlp.core.CallContext
import ai.tock.nlp.core.EntityEvaluationContext
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.ModelCore
import ai.tock.nlp.core.quality.TestContext
import ai.tock.nlp.front.service.ConfigurationRepository.entityTypeByName
import ai.tock.nlp.front.service.ConfigurationRepository.toApplication
import ai.tock.nlp.front.service.storage.TestModelDAO
import ai.tock.nlp.front.shared.ModelTester
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedEntity
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.test.EntityTestError
import ai.tock.nlp.front.shared.test.EntityTestErrorQueryResult
import ai.tock.nlp.front.shared.test.IntentTestError
import ai.tock.nlp.front.shared.test.IntentTestErrorQueryResult
import ai.tock.nlp.front.shared.test.TestBuild
import ai.tock.nlp.front.shared.test.TestErrorQuery
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.provide
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.util.Locale

/**
 *
 */
object ModelTesterService : ModelTester {

    private val logger = KotlinLogging.logger {}

    private val config = ApplicationConfigurationService
    val model: ModelCore get() = injector.provide()
    val modelDAO: TestModelDAO = injector.provide()

    override fun testModels() {
        config.getApplications().forEach { app ->
            app.supportedLocales.forEach { locale ->
                try {
                    testApplicationModel(app, locale)
                } catch (t: Throwable) {
                    logger.error(t)
                }
            }
        }
    }

    private fun testApplicationModel(application: ApplicationDefinition, locale: Locale) {
        val sentences = config.getSentences(application.intents, locale, ClassifiedSentenceStatus.model)
        // at least 100 validated sentences to test the model
        if (sentences.size > 100) {
            logger.info { "Start testing model for $application and locale $locale" }
            val intentCache = mutableMapOf<Id<IntentDefinition>, Intent>()
            val report = model.testModel(
                TestContext(
                    CallContext(
                        toApplication(application),
                        locale,
                        application.nlpEngineType,
                        EntityEvaluationContext(mergeEntityTypes = application.mergeEngineTypes)
                    ),
                    0.9F
                ),
                sentences.map { it.toSampleExpression({ config.toIntent(it, intentCache) }, { entityTypeByName(it) }) }
            )
            modelDAO.saveTestBuild(
                TestBuild(
                    application._id,
                    locale,
                    report.startDate,
                    report.buildModelDuration,
                    report.testSentencesDuration,
                    report.expressionsInModel.size,
                    report.expressionsTested.size,
                    report.intentErrors.size + report.entityErrors.size,
                    report.intentErrors.size,
                    report.entityErrors.size,
                    report.expressionsTested.groupBy { it.intent.name }.mapValues { it.value.size },
                    report.intentErrors.groupBy { it.expression.intent.name }.mapValues { it.value.size },
                    report.entityErrors.groupBy { it.expression.intent.name }.mapValues { it.value.size }
                )
            )

            val sentencesMap = sentences.map { it.text to it }.toMap()
            val intentErrorsMap = report.intentErrors.groupBy { it.expression.text }
            val entityErrorsMap = report.entityErrors.groupBy { it.expression.text }

            report.intentErrors.forEach {
                modelDAO.addTestIntentError(
                    IntentTestError(
                        application._id,
                        locale,
                        it.expression.text,
                        it.expression.intent.name,
                        it.intent,
                        it.intentProbability,
                        1
                    )
                )
            }
            report.expressionsTested.forEach {
                if (!intentErrorsMap.containsKey(it.text)) {
                    modelDAO.addTestIntentError(
                        IntentTestError(
                            application._id,
                            locale,
                            it.text,
                            "",
                            "",
                            0.0,
                            0
                        )
                    )
                }
            }

            report.entityErrors.forEach {
                modelDAO.addTestEntityError(
                    EntityTestError(
                        application._id,
                        locale,
                        it.expression.text,
                        sentencesMap[it.expression.text]!!.classification.intentId,
                        it.entities.map { ClassifiedEntity(it.value) },
                        if (it.entities.isEmpty()) 1.0 else it.entities.map { it.probability }.average(),
                        1
                    )
                )
            }

            report.expressionsTested.forEach {
                if (!intentErrorsMap.containsKey(it.text) && !entityErrorsMap.containsKey(it.text)) {
                    modelDAO.addTestEntityError(
                        EntityTestError(
                            application._id,
                            locale,
                            it.text,
                            null,
                            emptyList(),
                            0.0,
                            0
                        )
                    )
                }
            }

            logger.info { "End testing model for $application and locale $locale" }
        }
    }

    override fun searchTestIntentErrors(query: TestErrorQuery): IntentTestErrorQueryResult {
        return modelDAO.searchTestIntentErrors(query)
    }

    override fun searchTestEntityErrors(query: TestErrorQuery): EntityTestErrorQueryResult {
        return modelDAO.searchTestEntityErrors(query)
    }

    override fun deleteTestIntentError(applicationId: Id<ApplicationDefinition>, language: Locale, text: String) {
        modelDAO.deleteTestIntentError(applicationId, language, text)
    }

    override fun deleteTestEntityError(applicationId: Id<ApplicationDefinition>, language: Locale, text: String) {
        modelDAO.deleteTestEntityError(applicationId, language, text)
    }

    override fun getTestBuilds(query: TestErrorQuery): List<TestBuild> {
        return modelDAO.getTestBuilds(query)
    }
}
