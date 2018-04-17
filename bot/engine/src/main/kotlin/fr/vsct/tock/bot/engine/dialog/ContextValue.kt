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

package fr.vsct.tock.bot.engine.dialog

import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.api.client.model.EntityValue
import fr.vsct.tock.nlp.api.client.model.NlpResult
import fr.vsct.tock.nlp.entity.Value

/**
 * Takes a [Value] applied to the current [Entity] and returns a [ContextValue].
 */
infix fun Entity.set(value: Value?): ContextValue =
    ContextValue(this, value)

/**
 * Takes a [String] applied to the current [Entity] and returns a not yet evaluated [ContextValue].
 */
infix fun Entity.set(text: String): ContextValue =
    ContextValue(null, null, this, text, null, false)

/**
 * A (may be not yet evaluated) value linked to an entity stored in the context.
 */
data class ContextValue(
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
    val subEntities: List<ContextValue> = emptyList(),
    /**
     * The probability of the value.
     */
    val probability: Double = 1.0,
    /**
     * Does this value support merge?
     */
    val mergeSupport: Boolean = false
) {

    constructor(nlpResult: NlpResult, value: EntityValue) : this(nlpResult.retainedQuery, value)

    constructor(sentence: String, value: EntityValue)
            : this(
        value.start,
        value.end,
        value.entity,
        sentence.substring(value.start, value.end),
        value.value,
        value.evaluated,
        value.subEntities.map { ContextValue(sentence.substring(value.start, value.end), it) },
        value.probability,
        value.mergeSupport
    )

    constructor(entity: Entity, value: Value?, content: String? = null)
            : this(
        null,
        null,
        entity,
        content,
        value,
        true
    )

    override fun toString(): String {
        return if (evaluated) value?.toString() ?: "null" else content ?: "no content"
    }

}