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

package ai.tock.nlp.core.service.entity

import ai.tock.nlp.core.CallContext
import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.merge.ValueDescriptor
import ai.tock.nlp.model.EntityCallContext
import ai.tock.nlp.model.EntityCallContextForEntity
import ai.tock.nlp.model.EntityCallContextForIntent
import ai.tock.nlp.model.EntityCallContextForSubEntities
import ai.tock.shared.error
import ai.tock.shared.namespaceAndName
import mu.KotlinLogging
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 *
 */
internal object EntityCoreService : EntityCore {
    private val logger = KotlinLogging.logger {}

    private val entityTypeProviders: List<EntityTypeProvider> by lazy(PUBLICATION) { SupportedEntityTypeProviders.providers() }

    val knownEntityTypes: Set<String> get() = entityTypeProviders.flatMap { it.supportedEntityTypes() }.toSet()

    private fun getEntityEvaluator(entityType: EntityType): EntityTypeEvaluator? =
        entityType.name.namespaceAndName().let { (namespace, name) ->
            entityTypeProviders.firstOrNull { it.supportEvaluation(namespace, name) }?.getEntityTypeEvaluator()
        }

    override fun supportValuesMerge(entityType: EntityType) =
        entityType.name.namespaceAndName().let { (namespace, name) ->
            entityTypeProviders.any { it.supportValuesMerge(namespace, name) }
        }

    override fun classifyEntityTypes(
        context: EntityCallContext,
        text: String,
    ): List<EntityTypeRecognition> {
        return when (context) {
            is EntityCallContextForIntent -> classifyEntityTypesForIntent(context, text)
            is EntityCallContextForEntity -> emptyList() // TODO
            is EntityCallContextForSubEntities -> emptyList() // TODO
        }
    }

    private fun classifyEntityTypesForIntent(
        context: EntityCallContextForIntent,
        text: String,
    ): List<EntityTypeRecognition> {
        return context.intent
            .entities
            .mapNotNull { e ->
                e.entityType.name.namespaceAndName().let { (namespace, name) ->
                    entityTypeProviders.firstOrNull { it.supportClassification(namespace, name) }?.getEntityTypeClassifier()
                }
            }
            .distinct()
            .flatMap { classifyEntities(it, context, text) }
    }

    private fun classifyEntities(
        classifier: EntityTypeClassifier,
        context: EntityCallContext,
        text: String,
    ): List<EntityTypeRecognition> {
        return try {
            classifier.classifyEntities(context, text)
        } catch (e: Throwable) {
            logger.error(e)
            emptyList()
        }
    }

    override fun evaluateEntities(
        context: CallContext,
        text: String,
        entitiesRecognition: List<EntityRecognition>,
    ): List<EntityRecognition> = entitiesRecognition.map { e -> evaluate(context, text, e) }

    private fun evaluate(
        context: CallContext,
        text: String,
        e: EntityRecognition,
    ): EntityRecognition {
        if (e.value.evaluated) {
            return e
        }
        val root =
            getEntityEvaluator(e.entityType)?.let { evaluator ->
                evaluate(
                    evaluator,
                    EntityCallContextForEntity(context, e.value.entity),
                    e.value.textValue(text),
                )
            }

        return if (e.value.subEntities.isNotEmpty()) {
            val t = e.textValue(text)
            e.copy(
                probability = if (root == null) 1.0 else e.probability,
                value =
                    e.value.copy(
                        value = root?.value,
                        evaluated = root?.evaluated == true,
                        subEntities = e.value.subEntities.map { evaluate(context, t, it) },
                    ),
            )
        } else {
            root?.let { e.copy(probability = it.probability, value = e.value.copy(value = it.value, evaluated = it.evaluated)) }
                ?: e
        }
    }

    private fun evaluate(
        evaluator: EntityTypeEvaluator,
        context: EntityCallContextForEntity,
        text: String,
    ): EvaluationResult {
        return try {
            evaluator.evaluate(context, text)
        } catch (e: Throwable) {
            logger.error(e)
            EvaluationResult(false)
        }
    }

    override fun mergeValues(
        context: EntityCallContextForEntity,
        values: List<ValueDescriptor>,
    ): ValueDescriptor? {
        return getEntityEvaluator(context.entityType)?.merge(context, values)
    }

    override fun healthcheck(): Boolean {
        return entityTypeProviders.all { it.healthcheck() }
    }
}
