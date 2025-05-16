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

package ai.tock.nlp.front.shared.config

import ai.tock.nlp.front.shared.parser.ParseResult
import org.litote.kmongo.Id

/**
 * A classification for a sentence.
 */
data class Classification(
    /**
     * The intent id.
     */
    val intentId: Id<IntentDefinition>,
    /**
     * The entities.
     */
    val entities: List<ClassifiedEntity>
) {

    constructor(query: ParseResult, intentId: Id<IntentDefinition>) : this(
        intentId,
        query.entities.map { ClassifiedEntity(it) }
    )

    /**
     * Does this classification contains the specified entity type ?
     */
    fun containsEntityOrSubEntity(entityType: String): Boolean = entities.any { it.containsEntityOrSubEntity(entityType) }
}
