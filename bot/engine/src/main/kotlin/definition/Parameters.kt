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

package ai.tock.bot.definition

/**
 * Use [ParameterKey] to create this class.
 *
 * For instance :
 *
 * <code> val params = KeyEnum.a["b"] + KeyEnum.c["d"]
 * </code>
 *
 * where KeyEnum implements [ParameterKey].
 */
data class Parameters(private val entries: Map<String, String>) {

    companion object {
        /**
         * An empty parameters.
         */
        val EMPTY = Parameters(emptyMap())
    }

    constructor(vararg entries: Pair<String, String>) : this(entries.toMap())

    operator fun plus(param: Parameters): Parameters = Parameters(entries + param.entries)

    fun toArray(): Array<Pair<String, String>> = entries.entries.map { it.key to it.value }.toTypedArray()

    fun toMap(): Map<String, String> = entries
}
