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

package ai.tock.shared

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class StringsTest {
    @Test
    fun `WHEN string ends with dot THEN endWithPunctuation returns true`() {
        assertTrue("hkh.".endWithPunctuation())
    }

    @Test
    fun `Test Regex for replace accent`() {
        assertEquals(allowDiacriticsInRegexp("demo"), "d[eéèêë]m[oòóôõöø]")
        assertEquals(
            allowDiacriticsInRegexp("c est une mise a jour"),
            "[cç]['-_ ][eéèêë]st['-_ ][uùúûü][nñ][eéèêë]['-_ ]m[iìíîï]s[eéèêë]['-_ ][aàáâãä]['-_ ]j[oòóôõöø][uùúûü]r",
        )
    }
}
