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

internal class DefaultDateTemplate(
    private val date: TemporalAccessor?,
    private val formatterProvider: DateTimeFormatterProvider,
) : DateTemplate {
    constructor(date: TemporalAccessor?, dateFormatter: DateTimeFormatter) :
        this(
            date,
            object : DateTimeFormatterProvider {
                override fun provide(locale: Locale): DateTimeFormatter = dateFormatter.withLocale(locale)
            },
        )

    override fun format(locale: Locale): String {
        return date?.let {
            formatterProvider.provide(locale).format(it)
        } ?: ""
    }

    /**
     * To immediately format this date with the given locale.
     */
    internal fun formatTo(locale: Locale): TranslatedSequence = format(locale).raw

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultDateTemplate

        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        return date?.hashCode() ?: 0
    }
}
