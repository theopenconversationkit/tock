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
import fr.vsct.tock.nlp.model.EntityCallContextForEntity

/**
 *
 */
internal object EntityEvaluatorService {

    private val providerByEntityType: Map<String, EntityEvaluatorProvider> = SupportedEntityEvaluatorsProvider
            .evaluators()
            .flatMap { provider ->
                provider.getSupportedEntityTypes().map { it to provider }
            }
            .toMap()

    fun getEvaluatedEntityTypes(): Set<String> = providerByEntityType.keys

    private fun getEntityEvaluatorProvider(entityType: EntityType): EntityEvaluatorProvider? {
        return providerByEntityType[entityType.name]
    }

    fun evaluateEntities(context: CallContext, text: String, entitiesRecognition: List<EntityRecognition>): List<EntityRecognition> {
        val newEvaluatedEntities: Map<EntityRecognition, Any?> =
                entitiesRecognition
                        .filterNot { it.value.evaluated }
                        .mapNotNull {
                            e ->
                            getEntityEvaluatorProvider(e.entityType)?.let {
                                it.getEntityEvaluator()?.let { evaluator ->
                                    e to evaluator.evaluate(EntityCallContextForEntity(context, e.entityType), e.value.textValue(text))
                                }
                            }
                        }
                        .toMap()

        return entitiesRecognition.map {
            if (newEvaluatedEntities.containsKey(it)) {
                it.copy(value = it.value.copy(value = newEvaluatedEntities[it], evaluated = true))
            } else {
                it
            }
        }
    }
}