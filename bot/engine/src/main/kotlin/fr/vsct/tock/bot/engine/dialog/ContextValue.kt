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

import fr.vsct.tock.nlp.entity.Value
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.api.client.model.EntityValue

/**
 *
 */
data class ContextValue(
        val start: Int?,
        val end: Int?,
        val entity: Entity,
        val content: String?,
        var value: Value? = null,
        var evaluated: Boolean = false
) {

    constructor(sentence: String, value: EntityValue)
            : this(
            value.start,
            value.end,
            value.entity,
            sentence.substring(value.start, value.end),
            value.value,
            value.evaluated)

    constructor(entity: Entity, value: Value?, content: String? = null)
            : this(
            null,
            null,
            entity,
            content,
            value,
            true)

    override fun toString(): String {
        return if (evaluated) value?.toString() ?: "null" else content ?: "no content"
    }

    fun changeValue(newValue: Value?): ContextValue {
        value = newValue
        evaluated = true
        return this
    }


}