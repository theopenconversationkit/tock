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

package ai.tock.nlp.core

/**
 *
 */
interface IntOpenRange : Comparable<IntOpenRange> {

    val start: Int
    val end: Int

    override fun compareTo(other: IntOpenRange): Int {
        val c = start.compareTo(other.start)
        return if (c == 0) other.end.compareTo(end) else c
    }

    fun overlap(range: IntOpenRange): Boolean {
        return overlap(range.start, range.end)
    }

    fun overlap(start: Int, end: Int): Boolean {
        return this.end > start && this.start < end
    }

    fun isSameRange(range: IntOpenRange): Boolean {
        return start == range.start && end == range.end
    }

    /**
     * Transforms this range into an [IntRange].
     */
    fun toClosedRange(): IntRange = IntRange(start, end - 1)

    fun size(): Int = end - start

    fun textValue(originalText: String): String = originalText.substring(start, end)
}
