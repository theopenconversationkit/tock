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

package ai.tock.nlp.core.service

import ai.tock.nlp.core.Application
import ai.tock.nlp.core.BuildContext
import ai.tock.nlp.core.CallContext
import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.ModelCore
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.quality.EntityMatchError
import ai.tock.nlp.core.quality.IntentMatchError
import ai.tock.nlp.core.quality.TestContext
import ai.tock.nlp.core.quality.TestModelReport
import ai.tock.nlp.core.sample.SampleEntity
import ai.tock.nlp.core.sample.SampleExpression
import ai.tock.nlp.model.EntityBuildContext
import ai.tock.nlp.model.EntityBuildContextForIntent
import ai.tock.nlp.model.EntityBuildContextForSubEntities
import ai.tock.nlp.model.EntityCallContextForIntent
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.NlpClassifier
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.normalize
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant

/**
 *
 */
internal object ModelCoreService : ModelCore {
    private const val TEST_PROGRESS_BAR_WIDTH = 30
    private val WHITESPACE_REGEX = Regex("\\s+")

    private val logger = KotlinLogging.logger {}

    private val nlpClassifier: NlpClassifier by injector.instance()

    override fun warmupModels(context: BuildContext) {
        nlpClassifier.warmupIntentModel(IntentContext(context))
        context
            .application
            .intents
            .filter { it.entities.isNotEmpty() }
            .forEach {
                nlpClassifier.warmupEntityModel(
                    EntityCallContextForIntent(
                        CallContext(context.application, context.language, context.engineType, context.entityEngineType),
                        it,
                    ),
                )
            }
    }

    override fun updateIntentModel(
        context: BuildContext,
        expressions: List<SampleExpression>,
    ) {
        val nlpContext = IntentContext(context)
        if (!context.onlyIfNotExists || !nlpClassifier.isIntentModelExist(nlpContext)) {
            nlpClassifier.buildAndSaveIntentModel(nlpContext, context.formatExpressions(expressions))
        }
    }

    override fun updateEntityModelForIntent(
        context: BuildContext,
        intent: Intent,
        expressions: List<SampleExpression>,
    ) {
        val nlpContext = EntityBuildContextForIntent(context, intent)
        updateEntityModel(context, nlpContext, expressions)
    }

    override fun updateEntityModelForEntityType(
        context: BuildContext,
        entityType: EntityType,
        expressions: List<SampleExpression>,
    ) {
        val nlpContext = EntityBuildContextForSubEntities(context, entityType)
        updateEntityModel(context, nlpContext, expressions)
    }

    private fun updateEntityModel(
        context: BuildContext,
        nlpContext: EntityBuildContext,
        expressions: List<SampleExpression>,
    ) {
        if (!context.onlyIfNotExists ||
            !nlpClassifier.isEntityModelExist(nlpContext)
        ) {
            nlpClassifier.buildAndSaveEntityModel(nlpContext, context.formatExpressions(expressions))
        }
    }

    override fun deleteOrphans(
        applicationsAndIntents: Map<Application, Set<Intent>>,
        entityTypes: List<EntityType>,
    ) {
        nlpClassifier.deleteOrphans(applicationsAndIntents, entityTypes)
    }

    override fun testModel(
        context: TestContext,
        expressions: List<SampleExpression>,
    ): TestModelReport {
        if (expressions.size < 100) {
            error("at least 100 expressions needed")
        }
        val shuffle = expressions.toMutableList()
        shuffle.shuffle()
        val limit = (expressions.size * context.threshold).toInt()
        val modelExpressions = shuffle.subList(0, limit)
        val testedExpressions = shuffle.subList(limit, shuffle.size)

        val startDate = Instant.now()

        val intentContext = IntentContext(context)
        val intentModel = nlpClassifier.buildIntentModel(intentContext, context.formatExpressions(modelExpressions))
        val entityModels =
            modelExpressions
                .groupBy { it.intent }
                .mapNotNull { (intent, expressions) ->
                    try {
                        intent to
                            nlpClassifier.buildEntityModel(
                                EntityBuildContextForIntent(context, intent),
                                context.formatExpressions(expressions),
                            )
                    } catch (e: Exception) {
                        logger.error { "entity model build fail for $intent " }
                        logger.error(e)
                        null
                    }
                }.toMap()

        val buildDuration = Duration.between(startDate, Instant.now())

        val intentErrors = mutableListOf<IntentMatchError>()
        val entityErrors = mutableListOf<EntityMatchError>()

        var successes = 0
        var failures = 0

        testedExpressions.forEachIndexed { index, expression ->
            val parseResult =
                NlpCoreService.parse(
                    context,
                    expression.text,
                    intentModel,
                    entityModels,
                )
            val intentMatches = parseResult.intent == expression.intent.name
            val entitiesMatch = intentMatches && !hasNotSameEntities(expression.entities, parseResult.entities)
            if (!intentMatches) {
                intentErrors.add(IntentMatchError(expression, parseResult.intent, parseResult.intentProbability))
            } else if (!entitiesMatch) {
                entityErrors.add(EntityMatchError(expression, parseResult.entities))
            }

            if (intentMatches && entitiesMatch) {
                successes++
            } else {
                failures++
            }

            logger.debug {
                """[${expression.context.language.toLanguageTag()}] ${expression.text.replace(WHITESPACE_REGEX, " ")}
${if (intentMatches) "✅" else "❌"} Expected ${expression.intent.name}, got ${parseResult.intent}
${formatSentenceTestProgress(index + 1, testedExpressions.size, successes, failures)}
"""
            }
        }

        val testDuration = Duration.between(startDate.plus(buildDuration), Instant.now())

        return TestModelReport(
            expressions,
            testedExpressions,
            intentErrors,
            entityErrors,
            buildDuration,
            testDuration,
            startDate,
        )
    }

    internal fun formatSentenceTestProgress(
        completed: Int,
        total: Int,
        successes: Int,
        failures: Int,
    ): String {
        require(total > 0) { "total must be greater than zero" }
        val boundedCompleted = completed.coerceIn(0, total)
        val percentage = boundedCompleted * 100 / total
        val filledWidth = percentage * TEST_PROGRESS_BAR_WIDTH / 100
        val bar = "#".repeat(filledWidth) + "-".repeat(TEST_PROGRESS_BAR_WIDTH - filledWidth)
        return "Testing sentences [$bar] $percentage% ($completed/$total; success: $successes, fail: $failures) "
    }

    private fun hasNotSameEntities(
        expectedEntities: List<SampleEntity>,
        entities: List<EntityRecognition>,
    ): Boolean =
        expectedEntities.any { e ->
            entities.none {
                it.role == e.definition.role && it.entityType == e.definition.entityType &&
                    it.isSameRange(
                        e,
                    )
            }
        } ||
            entities.any {
                expectedEntities.none { e ->
                    it.role == e.definition.role && it.entityType == e.definition.entityType &&
                        it.isSameRange(
                            e,
                        )
                }
            }

    override fun getCurrentModelConfiguration(
        applicationName: String,
        nlpEngineType: NlpEngineType,
    ): NlpApplicationConfiguration = nlpClassifier.getCurrentModelConfiguration(applicationName, nlpEngineType)

    override fun updateModelConfiguration(
        applicationName: String,
        engineType: NlpEngineType,
        configuration: NlpApplicationConfiguration,
    ) = nlpClassifier.updateModelConfiguration(applicationName, engineType, configuration)

    private fun BuildContext.formatExpressions(expressions: List<SampleExpression>): List<SampleExpression> =
        if (application.normalizeText) {
            expressions.map { e -> e.copy(text = e.text.normalize(language)) }
        } else {
            expressions
        }

    private fun TestContext.formatExpressions(expressions: List<SampleExpression>): List<SampleExpression> =
        if (callContext.application.normalizeText) {
            expressions.map { e -> e.copy(text = e.text.normalize(callContext.language)) }
        } else {
            expressions
        }
}
