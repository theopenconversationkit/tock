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

package ai.tock.nlp.core

import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

/**
 * Configure entity evaluation.
 */
data class EntityEvaluationContext(
    /**
     * The reference date for dates.
     */
    val referenceDate: ZonedDateTime = now(UTC),
    /**
     * If merging entity type values is requested.
     */
    val mergeEntityTypes: Boolean = true,
    /**
     * If using entity type models is requested (entity disambiguation case).
     */
    val classifyEntityTypes: Boolean = mergeEntityTypes,
    /**
     * To manage a different date for each specified entity.
     */
    val referenceDateByEntityMap: Map<Entity, ZonedDateTime>? = null,
) {
    fun referenceDateForEntity(entity: Entity): ZonedDateTime = referenceDateByEntityMap?.get(entity) ?: referenceDate
}
