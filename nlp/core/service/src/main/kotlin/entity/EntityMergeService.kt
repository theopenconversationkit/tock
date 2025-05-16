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
import ai.tock.nlp.core.IntOpenRange
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.service.entity.EntityMergeService.Weighted.WeightedEntity
import ai.tock.nlp.core.service.entity.EntityMergeService.Weighted.WeightedEntityType
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging

/**
 * To merge intent entity model results & dedicated entity models results.
 */
internal object EntityMergeService : EntityMerge {

    sealed class Weighted(var weight: Double, val range: IntOpenRange) : Comparable<Weighted> {
        override fun compareTo(other: Weighted): Int {
            return weight.compareTo(other.weight)
        }

        fun overlap(w: Weighted): Boolean {
            return range.overlap(w.range)
        }

        class WeightedEntity(val entity: EntityRecognition) : Weighted(calculateWeight(entity), entity) {
            companion object {
                fun calculateWeight(entity: EntityRecognition): Double {
                    // if it's 100%, it's 100%
                    return if (entity.probability == 1.0) 1.0
                    else entity.probability - if (entity.value.evaluated && entity.value.value == null) 0.5 else 0.0
                }
            }
        }

        class WeightedEntityType(val entityType: EntityTypeRecognition) : Weighted(calculateWeight(entityType), entityType) {
            companion object {
                fun calculateWeight(entity: EntityTypeRecognition): Double {
                    return entity.probability - if (entity.value.evaluated && entity.value.value == null) 0.5 else 0.0
                }
            }
        }
    }

    private val logger = KotlinLogging.logger {}

    private val entityCore: EntityCore by injector.instance()

    override fun mergeEntityTypes(
        callContext: CallContext,
        text: String,
        intent: Intent,
        entities: List<EntityRecognition>,
        entityTypes: List<EntityTypeRecognition>
    ): List<EntityRecognition> {
        return if (entityTypes.isEmpty()) {
            entities
        } else {
            // introduce weight and start by the highest value
            val all = (entities.map { WeightedEntity(it) } + entityTypes.map { WeightedEntityType(it) })
                .sortedDescending()

            // need to trace those already viewed
            val viewed = mutableSetOf<Weighted>()

            all.map { e -> all.filter { e.overlap(it) }.sortedDescending() }
                // groups are sorted by the highest value of the group
                .sortedBy { it.first() }
                .mapNotNull { group ->
                    val stillAvailable = group - viewed
                    viewed.addAll(group)
                    // if one contains all the others, he has a bonus
                    if (stillAvailable.size > 1) {
                        var better = stillAvailable.first()
                        var min = better.range.start
                        var max = better.range.end
                        stillAvailable.forEach {
                            min = Math.min(min, it.range.start)
                            max = Math.max(max, it.range.end)
                            if (min == it.range.start && max == it.range.end) {
                                better = it
                            }
                        }
                        if (min == better.range.start && max == better.range.end) {
                            better.weight += 0.2
                        }
                    }
                    val first = stillAvailable.sortedDescending().firstOrNull()
                    // need to recheck overlap
                    if (first != null) {
                        viewed.addAll(all.filter { first.overlap(it) })
                    }
                    when (first) {
                        null -> null
                        is WeightedEntity ->
                            with(first.entity.value) { if (evaluated && value == null) null else first.entity }
                        is WeightedEntityType -> {
                            group.firstOrNull { it is WeightedEntity && intent.hasEntity(first.entityType.entityType, it.entity.role) }
                                ?.let {
                                    first.entityType.toResult(callContext, text, (it as WeightedEntity).entity.role)
                                }
                                ?: createOrphanEntity(callContext, text, first.entityType, intent)
                        }
                    }
                }
                // sorted by range
                .sorted()
        }
    }

    private fun createOrphanEntity(
        callContext: CallContext,
        text: String,
        entityType: EntityTypeRecognition,
        intent: Intent
    ): EntityRecognition? {
        val intentEntities = intent.entities.filter { it.entityType == entityType.entityType }
        return if (intentEntities.size == 1) {
            logger.debug { "create orphan : $entityType" }
            entityType.toResult(callContext, text, intentEntities.first().role)
        } else {
            // TODO take the more frequently viewed
            intentEntities.let {
                val e = it.first()
                logger.warn { "create orphan with first role found  : $e" }
                entityType.toResult(callContext, text, e.role)
            }
        }
    }

    private fun EntityTypeRecognition.toResult(
        callContext: CallContext,
        text: String,
        role: String
    ): EntityRecognition {
        return toEntityRecognition(role)
            .run {
                // need to reevaluate
                if (callContext.evaluationContext.referenceDateByEntityMap?.containsKey(value.entity) == true) {
                    entityCore.evaluateEntities(
                        callContext,
                        text,
                        listOf(
                            copy(
                                value = value.copy(
                                    evaluated = false
                                )
                            )
                        )
                    ).first()
                } else {
                    this
                }
            }
    }
}
