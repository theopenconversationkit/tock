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

import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.EntityValue
import ai.tock.nlp.core.IntOpenRange
import ai.tock.nlp.front.shared.parser.ParsedEntityValue

/**
 * A classification for an entity.
 */
data class ClassifiedEntity(
    /**
     * The entity type.
     */
    val type: String,
    /**
     * The entity role.
     */
    val role: String,
    /**
     * Start index of the entity in the text.
     */
    override val start: Int,
    /**
     * End index (exclusive) of the entity in the text.
     */
    override val end: Int,
    /**
     * The sub entities of the entity.
     */
    val subEntities: List<ClassifiedEntity> = emptyList()
) : IntOpenRange {

    constructor(value: ParsedEntityValue) : this(
        value.entity.entityType.name,
        value.entity.role,
        value.start,
        value.end,
        value.subEntities.map { ClassifiedEntity(it) }
    )

    constructor(value: EntityValue) : this(
        value.entity.entityType.name,
        value.entity.role,
        value.start,
        value.end,
        value.subEntities.map { ClassifiedEntity(it.value) }
    )

    fun toEntityValue(entityProvider: (String, String) -> Entity?): EntityValue? =
        toEntity(entityProvider)
            ?.run {
                EntityValue(
                    start,
                    end,
                    this,
                    subEntities = subEntities.mapNotNull {
                        it.toEntityRecognition(entityProvider)
                    }
                )
            }

    fun toEntityRecognition(entityProvider: (String, String) -> Entity?): EntityRecognition? =
        toEntityValue(entityProvider)?.run { EntityRecognition(this, 1.0) }

    fun toEntity(entityProvider: (String, String) -> Entity?): Entity? = entityProvider.invoke(type, role)

    /**
     * Does this entity contains the specified entity type ?
     */
    fun containsEntityOrSubEntity(entityType: String): Boolean = type == entityType || subEntities.any { it.containsEntityOrSubEntity(entityType) }
}
