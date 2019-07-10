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

package fr.vsct.tock.nlp.core.service.entity

import fr.vsct.tock.nlp.core.CallContext
import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.merge.ValueDescriptor
import fr.vsct.tock.nlp.model.EntityCallContext
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import fr.vsct.tock.nlp.model.EntityCallContextForIntent
import fr.vsct.tock.nlp.model.EntityCallContextForSubEntities
import fr.vsct.tock.shared.error
import mu.KotlinLogging

/**
 *
 */
internal object EntityCoreService : EntityCore {

    private val logger = KotlinLogging.logger {}

    private val evaluatorProviders: List<EntityEvaluatorProvider> = SupportedEntityEvaluatorsProvider.evaluators()

    private val entityTypeProviderMap: Map<String, EntityEvaluatorProvider> = evaluatorProviders
        .flatMap { provider ->
            provider.getSupportedEntityTypes().map { it to provider }
        }
        .toMap()

    private val entityTypeWithValuesMergeSupport: Set<String> = evaluatorProviders
        .flatMap { provider ->
            provider.getEntityTypesWithValuesMergeSupport()
        }
        .toSet()

    override fun getEvaluableEntityTypes(): Set<String> = entityTypeProviderMap.keys

    override fun supportValuesMerge(entityType: EntityType) = entityTypeWithValuesMergeSupport.contains(entityType.name)

    private fun getEntityEvaluatorProvider(entityType: EntityType): EntityEvaluatorProvider? {
        if (entityType.predefinedValues.isNotEmpty()) {
            return PredefinedValueEntityEvaluatorProvider()
        }
        return entityTypeProviderMap[entityType.name]
    }

    private fun getEntityEvaluator(entityType: EntityType): EntityEvaluator? =
        if (entityType.predefinedValues.isNotEmpty()) {
            PredefinedValuesEntityEvaluator
        } else {
            getEntityEvaluatorProvider(entityType)?.getEntityEvaluator()
        }

    override fun classifyEntityTypes(
        context: EntityCallContext,
        text: String
    ): List<EntityTypeRecognition> {
        return when (context) {
            is EntityCallContextForIntent -> classifyEntityTypesForIntent(context, text)
            is EntityCallContextForEntity -> TODO()
            is EntityCallContextForSubEntities -> TODO()
        }
    }

    private fun classifyEntityTypesForIntent(
        context: EntityCallContextForIntent,
        text: String
    ): List<EntityTypeRecognition> {
        return context.intent
            .entities
            .mapNotNull { getEntityEvaluatorProvider(it.entityType) }
            .distinct()
            .mapNotNull { it.getEntityTypeClassifier() }
            .flatMap { classifyEntities(it, context, text) }
    }

    private fun classifyEntities(
        classifier: EntityTypeClassifier,
        context: EntityCallContext,
        text: String
    ): List<EntityTypeRecognition> {
        return try {
            classifier.classifyEntities(context, text)
        } catch (e: Exception) {
            logger.error(e)
            emptyList()
        }
    }

    override fun evaluateEntities(
        context: CallContext,
        text: String,
        entitiesRecognition: List<EntityRecognition>
    ): List<EntityRecognition> {
        val newEvaluatedEntities: Map<EntityRecognition, EvaluationResult> =
            entitiesRecognition
                .filterNot { it.value.evaluated }
                .mapNotNull { e ->
                    getEntityEvaluator(e.entityType)?.let { evaluator ->
                        e to evaluate(
                            evaluator,
                            EntityCallContextForEntity(context, e.value.entity),
                            e.value.textValue(text)
                        )
                    }
                }
                .toMap()

        return entitiesRecognition.map {
            if (newEvaluatedEntities.containsKey(it)) {
                val evaluation = newEvaluatedEntities[it]!!
                it.copy(
                    probability = if (evaluation.evaluated) (it.probability + evaluation.probability) / 2 else it.probability,
                    value = it.value.copy(value = evaluation.value, evaluated = true)
                )
            } else {
                it
            }
        }
    }

    fun evaluate(
        evaluator: EntityEvaluator,
        context: EntityCallContextForEntity,
        text: String
    ): EvaluationResult {
        return try {
            evaluator.evaluate(context, text)
        } catch (e: Throwable) {
            logger.error(e)
            EvaluationResult(false)
        }
    }

    override fun mergeValues(context: EntityCallContextForEntity, values: List<ValueDescriptor>): ValueDescriptor? {
        return getEntityEvaluator(context.entityType)?.merge(context, values)
    }

    override fun healthcheck(): Boolean {
        return evaluatorProviders.all { it.healthcheck() }
    }
}