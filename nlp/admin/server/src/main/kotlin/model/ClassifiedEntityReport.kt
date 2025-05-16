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

package ai.tock.nlp.admin.model

import ai.tock.nlp.entity.Value
import ai.tock.nlp.front.shared.config.ClassifiedEntity
import ai.tock.nlp.front.shared.parser.ParsedEntityValue

/**
 *
 */
data class ClassifiedEntityReport(
    val type: String,
    val role: String,
    val start: Int,
    val end: Int,
    val value: Value?,
    val probability: Double?,
    val subEntities: List<ClassifiedEntityReport>
) {

    constructor(value: ParsedEntityValue) : this(
        value.entity.entityType.name,
        value.entity.role,
        value.start,
        value.end,
        value.value,
        value.probability,
        value.subEntities.map { ClassifiedEntityReport(it) }
    )

    constructor(entity: ClassifiedEntity) : this(
        entity.type,
        entity.role,
        entity.start,
        entity.end,
        null,
        null,
        entity.subEntities.map { ClassifiedEntityReport(it) }
    )

    fun toClassifiedEntity(): ClassifiedEntity {
        return ClassifiedEntity(type, role, start, end, subEntities.map { it.toClassifiedEntity() })
    }
}
