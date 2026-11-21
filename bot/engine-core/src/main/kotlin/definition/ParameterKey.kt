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

import ai.tock.shared.jackson.mapper

/**
 * A parameter key - the implementation is usually an enum.
 */
interface ParameterKey {
    /**
     * Overridden by enum implementation.
     */
    val name: String

    /**
     * The key of the parameter.
     */
    val key: String get() = name

    /**
     * Create a [Parameters] with this as key and the [value].toString() value.
     */
    operator fun get(value: Any): Parameters = Parameters(key to ((value as? ParameterKey)?.key ?: value.toString()))

    /**
     * Create a [Parameters] with this as key and the [value] json serialized string.
     */
    operator fun invoke(value: Any): Parameters = Parameters(key to mapper.writeValueAsString(value))
}
