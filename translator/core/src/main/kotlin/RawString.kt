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

package ai.tock.translator

import mu.KotlinLogging

/**
 * An empty string marked as [TranslatedSequence].
 */
val EMPTY_TRANSLATED_STRING: TranslatedSequence = RawString("")

/**
 * A raw string is a string that should not be translated.
 *
 * @see CharSequence.raw
 */
data class RawString(private val wrapped: CharSequence) :
    CharSequence by wrapped, TranslatedSequence {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun toString(): String {
        return wrapped.toString()
    }

    override fun subSequence(
        startIndex: Int,
        endIndex: Int,
    ): TranslatedSequence {
        return RawString(wrapped.subSequence(startIndex, endIndex))
    }

    override fun plus(other: Any?): TranslatedSequence {
        logger.warn { "adding a String to a TranslatedSequence is not recommended - please use message format pattern" }
        return RawString(toString() + other.toString())
    }
}
