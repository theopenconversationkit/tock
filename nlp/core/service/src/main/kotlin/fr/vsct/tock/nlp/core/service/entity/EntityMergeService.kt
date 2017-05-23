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

import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.IntOpenRange
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.service.entity.EntityMergeService.Weighted.WeightedEntity
import fr.vsct.tock.nlp.core.service.entity.EntityMergeService.Weighted.WeightedEntityType
import mu.KotlinLogging

/**
 * Merge intent entity model results & dedicated entity models results.
 */
internal object EntityMergeService : EntityMerge {

    sealed class Weighted(val weight: Double, val range: IntOpenRange) : Comparable<Weighted> {
        override fun compareTo(other: Weighted): Int {
            return weight.compareTo(other.weight)
        }

        fun overlap(w: Weighted): Boolean {
            return range.overlap(w.range)
        }

        class WeightedEntity(val entity: EntityRecognition) : Weighted(calculateWeight(entity), entity) {
            companion object {
                fun calculateWeight(entity: EntityRecognition): Double {
                    return entity.probability - if (entity.value.evaluated && entity.value.value == null) 0.5 else 0.0
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

    override fun mergeEntityTypes(
            intent: Intent,
            entities: List<EntityRecognition>,
            entityTypes: List<EntityTypeRecognition>): List<EntityRecognition> {
        return if (entityTypes.isEmpty()) {
            entities
        } else {
            //introduce weight
            val all = entities.map { WeightedEntity(it) } + entityTypes.map { WeightedEntityType(it) }

            //need to trace those already viewed
            val viewed = mutableSetOf<Weighted>()

            all.map { e -> all.filter { e.overlap(it) }.sortedDescending() }
                    //sorted by the most weighted
                    .sortedBy { it.first() }
                    .mapNotNull { group ->
                        val stillAvailable = group - viewed
                        viewed.addAll(group)
                        val first = stillAvailable.firstOrNull()
                        when (first) {
                            null -> null
                            is WeightedEntity ->
                                with(first.entity.value) { if (evaluated && value == null) null else first.entity }
                            is WeightedEntityType -> {
                                if (group.any { it is WeightedEntity }) {
                                    first.entityType.toEntityRecognition(
                                            (group.first { it is WeightedEntity } as WeightedEntity).entity.role)
                                } else {
                                    createOrphanEntity(first.entityType, intent)
                                }
                            }
                        }
                    }
                    //sorted by range
                    .sorted()
        }
    }

    private fun createOrphanEntity(
            entityType: EntityTypeRecognition,
            intent: Intent): EntityRecognition? {
        val intentEntities = intent.entities.filter { it.entityType == entityType.entityType }
        return if (intentEntities.size == 1) {
            logger.debug { "create orphan : $entityType" }
            entityType.toEntityRecognition(intentEntities.first().role)
        } else {
            //TODO take the more frequently viewed
            intentEntities.let {
                val e = it.first()
                logger.warn { "create orphan with first role found  : $e" }
                entityType.toEntityRecognition(e.role)
            }
        }
    }

}