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

package fr.vsct.tock.nlp.front.shared.parser

import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.EntityValue
import fr.vsct.tock.nlp.core.IntOpenRange
import fr.vsct.tock.nlp.entity.Value
import fr.vsct.tock.nlp.front.shared.value.ValueTransformer.wrapNullableValue

/**
 * This class is copied from [fr.vsct.tock.nlp.core.EntityValue], but
 * [value] is typed at [Value].
 * This is basically a hack to avoid including [Value] notion in the core.
 *
 * There is also an additional boolean [mergeSupport] to indicate if value merge
 * between two values or more of the same [Entity] is supported.
 *
 * A [probability] property is also added. It comes from [EntityRecognition]
 */
data class ParsedEntityValue(
    /**
     * Start (inclusive) text index of the entity.
     */
    override val start: Int,
    /**
     * End (exclusive) text index of the entity.
     */
    override val end: Int,
    /**
     * Entity definition.
     */
    val entity: Entity,
    /**
     * Current value if evaluated.
     */
    val value: Value? = null,
    /**
     * Is this entity has been evaluated ?
     */
    val evaluated: Boolean = false,
    /**
     * Sub entities if any.
     */
    val subEntities: List<ParsedEntityValue> = emptyList(),
    /**
     * Recognition probability.
     */
    val probability: Double = 1.0,
    /**
     * Does this entity value support merge with other values ?
     */
    val mergeSupport: Boolean = false
) : IntOpenRange {

    constructor(
        start: Int,
        end: Int,
        entity: Entity,
        value: Any? = null,
        evaluated: Boolean = false,
        subEntities: List<ParsedEntityValue>,
        probability: Double = 1.0,
        mergeSupport: Boolean = false
    ) :
            this(start, end, entity, wrapNullableValue(value), evaluated, subEntities, probability, mergeSupport)

    constructor(entityValue: EntityValue, probability: Double, mergeSupport: Boolean) : this(
        entityValue.start,
        entityValue.end,
        entityValue.entity,
        entityValue.value,
        entityValue.evaluated,
        entityValue.subEntities.map { ParsedEntityValue(it.value, it.probability, false) },
        probability,
        mergeSupport
    )
}