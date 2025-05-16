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

import java.time.temporal.TemporalAccessor
import java.util.Formattable
import java.util.Formatter
import java.util.Locale

/**
 * A date template is used to format a date (or a [TemporalAccessor]) for all supported [Locale] in the i18n process.
 */
interface DateTemplate : Formattable {

    /**
     * Formats the date from the provided [locale].
     */
    fun format(locale: Locale): String

    override fun formatTo(formatter: Formatter, flags: Int, width: Int, precision: Int) {
        formatter.format(format(formatter.locale()))
    }
}
