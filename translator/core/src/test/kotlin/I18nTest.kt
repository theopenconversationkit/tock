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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class I18nTest {

    @Test
    fun formatWith_shouldWorks_forAllLocales() {
        val date = LocalDate.now().with(DayOfWeek.FRIDAY)
        val format = DateTimeFormatter.ofPattern("EEEE")
        assertEquals(
            "Friday",
            date.formatWith(format, Locale.ENGLISH).toString()
        )
        assertEquals(
            "vendredi",
            date.formatWith(format, Locale.FRENCH).toString()
        )
    }
}
