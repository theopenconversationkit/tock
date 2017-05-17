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
import fr.vsct.tock.nlp.core.EntityValue
import fr.vsct.tock.nlp.core.IntOpenRange
import fr.vsct.tock.nlp.entity.CustomValueWrapper
import fr.vsct.tock.nlp.entity.Value

/**
 * The difference between this class and [fr.vsct.tock.nlp.core.EntityValue]
 * is that value is typed at [Value].
 * This is basically a hack to avoid including [Value] notion in the core.
 */
data class ParsedEntityValue(override val start: Int,
                             override val end: Int,
                             val entity: Entity,
                             val value: Value? = null,
                             val evaluated: Boolean = false) : IntOpenRange {

    companion object {
        private fun wrapWalue(value: Any?): Value? {
            return when (value) {
                null -> null
                is Value -> value
                else -> CustomValueWrapper(value)
            }
        }
    }

    constructor(start: Int,
                end: Int,
                entity: Entity,
                value: Any? = null,
                evaluated: Boolean = false) :
            this(start, end, entity, wrapWalue(value), evaluated)

    constructor(entityValue: EntityValue) : this(
            entityValue.start,
            entityValue.end,
            entityValue.entity,
            entityValue.value,
            entityValue.evaluated)
}