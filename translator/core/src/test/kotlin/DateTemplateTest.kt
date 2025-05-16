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

import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate.now
import java.util.Formatter
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class DateTemplateTest {

    @Test
    fun formatTo_shouldWorks_forAllLocales() {
        val date = now().with(DayOfWeek.FRIDAY)
        val dateTemplate = date by "EEEE"
        var formatter = Formatter(Locale.ENGLISH)
        dateTemplate.formatTo(formatter, 0, 0, 0)
        assertEquals(
            "Friday",
            formatter.toString()
        )
        formatter = Formatter(Locale.FRENCH)
        dateTemplate.formatTo(formatter, 0, 0, 0)
        assertEquals(
            "vendredi",
            formatter.toString()
        )
    }
}
