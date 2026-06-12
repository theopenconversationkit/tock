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

import kotlin.reflect.KClass

/**
 * Represents a key for a value in a [DialogContext]
 *
 * New instances should be constructed using the [reified factory][Companion.invoke] and stored in a constant:
 * ```
 * object MyKeys {
 *     val myDataKey = DialogContextKey<Boolean>("my_data")
 * }
 * ```
 *
 * @param <T> the type of the value
 */
class DialogContextKey<T : Any>(val id: String, val type: KClass<T>) {
    companion object {
        /**
         * Factory method for [DialogContextKey]
         */
        inline operator fun <reified T : Any> invoke(name: String) = DialogContextKey(name, T::class)
    }

    override fun toString(): String = id

    /**
     * Two keys are considered equal if they have the same identifier
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DialogContextKey<*>

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
