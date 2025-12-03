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

package ai.tock.bot.engine.dialog

import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.NlpEntityValue
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.nlp.entity.StringValue
import ai.tock.nlp.entity.Value

/**
 * Takes a [Value] applied to the current [Entity] and returns a [EntityValue].
 */
infix fun Entity.setTo(value: Value?): EntityValue = EntityValue(this, value)

/**
 * Takes a [String] applied to the current [Entity] and returns a not yet evaluated [EntityValue].
 */
infix fun Entity.setTo(text: String): EntityValue = EntityValue(null, null, this, text, null, false)

/**
 * Does this event contains specified role entity?
 */
internal fun List<EntityValue>.hasEntity(role: String): Boolean {
    return hasSubEntity(this, role)
}

internal fun hasEntityPredefinedValue(
    entities: List<EntityValue>,
    role: String,
    value: String,
): Boolean {
    return entities.filter { it.entity.role == role || hasEntityPredefinedValue(it.subEntities, role, value) }
        .firstOrNull {
            (it.value as? StringValue)?.value == value ||
                hasEntityPredefinedValue(it.subEntities, role, value)
        } != null
}

internal fun hasSubEntity(
    entities: List<EntityValue>,
    role: String,
): Boolean {
    return entities.any { it.entity.role == role } || entities.any { hasSubEntity(it.subEntities, role) }
}

/**
 * A (may be not yet evaluated) value linked to an entity stored in the context.
 */
data class EntityValue(
    /**
     * If extracted from a sentence, start position of the text content in this sentence.
     */
    val start: Int?,
    /**
     * If extracted from a sentence, end position of the text content in this sentence.
     */
    val end: Int?,
    /**
     * The linked [Entity].
     */
    val entity: Entity,
    /**
     * Text content if any.
     */
    val content: String?,
    /**
     * Value if any.
     */
    val value: Value? = null,
    /**
     * Is the value has been evaluated?
     */
    val evaluated: Boolean = false,
    /**
     * Sub entity values if any.
     */
    val subEntities: List<EntityValue> = emptyList(),
    /**
     * The probability of the value.
     */
    val probability: Double = 1.0,
    /**
     * Does this value support merge?
     */
    val mergeSupport: Boolean = false,
) {
    constructor(nlpResult: NlpResult, value: NlpEntityValue) : this(nlpResult.retainedQuery, value)

    constructor(sentence: String, value: NlpEntityValue) :
        this(
            value.start,
            value.end,
            value.entity,
            sentence.substring(value.start, value.end),
            value.value,
            value.evaluated,
            value.subEntities.map { EntityValue(sentence.substring(value.start, value.end), it) },
            value.probability,
            value.mergeSupport,
        )

    constructor(entity: Entity, value: Value?, content: String? = null) :
        this(
            null,
            null,
            entity,
            content,
            value,
            true,
        )

    override fun toString(): String {
        return if (evaluated) value?.toString() ?: "null" else content ?: "no content"
    }

    internal fun toClosedRange(): IntRange? = if (start != null && end != null && start < end) IntRange(start, end - 1) else null
}
