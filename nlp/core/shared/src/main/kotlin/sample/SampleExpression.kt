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

package ai.tock.nlp.core.sample

import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.Intent

/**
 *
 */
data class SampleExpression(
    val text: String,
    val intent: Intent,
    val entities: List<SampleEntity> = emptyList(),
    val context: SampleContext = SampleContext()
) {

    fun entityValues(def: Entity): List<String> {
        return entities
            .filter { it.definition == def }
            .map { text.substring(it.start, it.end) }
    }

    fun containsEntityType(entityType: EntityType): Boolean {
        return entities.any { it.isType(entityType) }
    }
}
