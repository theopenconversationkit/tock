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

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.Locale

val defaultUserInterface: UserInterfaceType = UserInterfaceType.textChat

/**
 * Format a [TemporalAccessor] this the specified [dateFormatPattern] used to create a [DateTimeFormatter].
 */
infix fun TemporalAccessor?.by(dateFormatPattern: String): DateTemplate = by(DateTimeFormatter.ofPattern(dateFormatPattern))

/**
 * Format a [TemporalAccessor] this the specified [DateTimeFormatter].
 */
infix fun TemporalAccessor?.by(formatter: DateTimeFormatter): DateTemplate = DefaultDateTemplate(this, formatter)

/**
 * Format a [TemporalAccessor] this the specified [DateTimeFormatterProvider].
 */
infix fun TemporalAccessor?.by(formatterProvider: DateTimeFormatterProvider): DateTemplate = DefaultDateTemplate(this, formatterProvider)

/**
 * To immediately format this date with the given locale.
 */
fun TemporalAccessor?.formatWith(
    formatter: DateTimeFormatter,
    locale: Locale,
): CharSequence? = if (this == null) null else (by(formatter) as DefaultDateTemplate).formatTo(locale)

/**
 * To immediately format this date with the given locale.
 */
fun TemporalAccessor?.formatWith(
    formatterProvider: DateTimeFormatterProvider,
    locale: Locale,
): CharSequence? = if (this == null) null else (by(formatterProvider) as DefaultDateTemplate).formatTo(locale)

/**
 * Transforms this char sequence in a not-to-translate [TranslatedSequence] - ie a "raw" String.
 */
val CharSequence.raw: TranslatedSequence
    get() = if (this is TranslatedSequence) this else RawString(this)

/**
 * Is this char sequence containing SSML?
 */
fun CharSequence.isSSML(): Boolean = contains("<speak>")

/**
 * Split a char sequence to a list of char sequence.
 */
fun CharSequence.splitToCharSequence(
    vararg delimiters: String,
    ignoreCase: Boolean = false,
    limit: Int = 0,
): List<CharSequence> {
    return if (this is TextAndVoiceTranslatedString) {
        splitToCharSequence(*delimiters, ignoreCase = ignoreCase, limit = limit)
    } else {
        rangesDelimitedBy(delimiters, ignoreCase = ignoreCase, limit = limit).asIterable().map { subSequence(it) }
    }
}

// copied from Strings.kt ->

private class DelimitedRangesSequence(
    private val input: CharSequence,
    private val startIndex: Int,
    private val limit: Int,
    private val getNextMatch: CharSequence.(Int) -> Pair<Int, Int>?,
) : Sequence<IntRange> {
    override fun iterator(): Iterator<IntRange> =
        object : Iterator<IntRange> {
            var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue
            var currentStartIndex: Int = startIndex.coerceIn(0, input.length)
            var nextSearchIndex: Int = currentStartIndex
            var nextItem: IntRange? = null
            var counter: Int = 0

            private fun calcNext() {
                if (nextSearchIndex < 0) {
                    nextState = 0
                    nextItem = null
                } else {
                    if (limit > 0 && ++counter >= limit || nextSearchIndex > input.length) {
                        nextItem = currentStartIndex..input.lastIndex
                        nextSearchIndex = -1
                    } else {
                        val match = input.getNextMatch(nextSearchIndex)
                        if (match == null) {
                            nextItem = currentStartIndex..input.lastIndex
                            nextSearchIndex = -1
                        } else {
                            val (index, length) = match
                            nextItem = currentStartIndex..index - 1
                            currentStartIndex = index + length
                            nextSearchIndex = currentStartIndex + if (length == 0) 1 else 0
                        }
                    }
                    nextState = 1
                }
            }

            override fun next(): IntRange {
                if (nextState == -1) {
                    calcNext()
                }
                if (nextState == 0) {
                    throw NoSuchElementException()
                }
                val result = nextItem as IntRange
                // Clean next to avoid keeping reference on yielded instance
                nextItem = null
                nextState = -1
                return result
            }

            override fun hasNext(): Boolean {
                if (nextState == -1) {
                    calcNext()
                }
                return nextState == 1
            }
        }
}

private fun CharSequence.rangesDelimitedBy(
    delimiters: Array<out String>,
    startIndex: Int = 0,
    ignoreCase: Boolean = false,
    limit: Int = 0,
): Sequence<IntRange> {
    require(limit >= 0, { "Limit must be non-negative, but was $limit." })
    val delimitersList = delimiters.asList()

    return DelimitedRangesSequence(this, startIndex, limit, { start -> findAnyOf(delimitersList, start, ignoreCase = ignoreCase, last = false)?.let { it.first to it.second.length } })
}

private fun CharSequence.findAnyOf(
    strings: Collection<String>,
    startIndex: Int,
    ignoreCase: Boolean,
    last: Boolean,
): Pair<Int, String>? {
    if (!ignoreCase && strings.size == 1) {
        val string = strings.single()
        val index = if (!last) indexOf(string, startIndex) else lastIndexOf(string, startIndex)
        return if (index < 0) null else index to string
    }

    val indices = if (!last) startIndex.coerceAtLeast(0)..length else startIndex.coerceAtMost(lastIndex) downTo 0

    if (this is String) {
        for (index in indices) {
            val matchingString = strings.firstOrNull { it.regionMatches(0, this, index, it.length, ignoreCase) }
            if (matchingString != null) {
                return index to matchingString
            }
        }
    } else {
        for (index in indices) {
            val matchingString = strings.firstOrNull { it.regionMatchesImpl(0, this, index, it.length, ignoreCase) }
            if (matchingString != null) {
                return index to matchingString
            }
        }
    }

    return null
}

internal fun CharSequence.regionMatchesImpl(
    thisOffset: Int,
    other: CharSequence,
    otherOffset: Int,
    length: Int,
    ignoreCase: Boolean,
): Boolean {
    if ((otherOffset < 0) || (thisOffset < 0) || (thisOffset > this.length - length) ||
        (otherOffset > other.length - length)
    ) {
        return false
    }

    for (index in 0..length - 1) {
        if (!this[thisOffset + index].equals(other[otherOffset + index], ignoreCase)) {
            return false
        }
    }
    return true
}

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal fun String.nativeIndexOf(
    ch: Char,
    fromIndex: Int,
): Int = (this as java.lang.String).indexOf(ch.code, fromIndex)

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal fun String.nativeLastIndexOf(
    ch: Char,
    fromIndex: Int,
): Int = (this as java.lang.String).lastIndexOf(ch.code, fromIndex)
