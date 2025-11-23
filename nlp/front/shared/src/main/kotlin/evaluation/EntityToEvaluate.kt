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

package ai.tock.nlp.front.shared.evaluation

import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.EntityValue
import ai.tock.nlp.core.IntOpenRange

/**
 *
 */
data class EntityToEvaluate(
    override val start: Int,
    override val end: Int,
    val entity: Entity,
    val subEntities: List<EntityToEvaluate> = emptyList(),
) : IntOpenRange {
    fun toEntityRecognition(): EntityRecognition = EntityRecognition(EntityValue(start, end, entity, null, subEntities.map { it.toEntityRecognition() }), 1.0)
}
