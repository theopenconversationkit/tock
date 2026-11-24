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

package ai.tock.duckling.client

import com.fasterxml.jackson.databind.ObjectMapper

/**
 * A value of the JSON answered by Duckling: the lists, maps, strings, numbers and booleans decoded by Jackson,
 * the types Vert.x decoded before.
 */
internal class JSONValue(
    val value: Any?,
) {
    companion object {
        val NULL_VALUE = JSONValue(null)

        private val mapper = ObjectMapper()

        /** Decodes a JSON array. */
        fun parseArray(json: String): JSONValue = JSONValue(mapper.readValue(json, List::class.java))
    }

    operator fun get(i: Int): JSONValue =
        if (isNull()) {
            NULL_VALUE
        } else if (value is List<*>) {
            JSONValue(value[i])
        } else {
            throw IllegalArgumentException("supported only for array but is $value")
        }

    operator fun get(name: String): JSONValue =
        if (isNull()) {
            NULL_VALUE
        } else if (value is Map<*, *>) {
            JSONValue(value.get(name))
        } else {
            throw IllegalArgumentException("supported only for map but is $value")
        }

    fun string(): String = value.toString()

    fun int(): Int = value as Int

    fun number(): Number = value as Number

    fun array(): List<*> = value as List<*>

    fun isEmpty(): Boolean = (value as List<*>).isEmpty()

    fun isNull(): Boolean = value == null

    fun isNotNull(): Boolean = value != null

    fun iterable(): Iterable<JSONValue> = (value as List<*>).map { JSONValue(it) }

    override fun toString(): String = string()
}
