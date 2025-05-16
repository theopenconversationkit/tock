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

package ai.tock.shared.security

import ai.tock.shared.security.TockObfuscatorService.obfuscate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
class TockObfuscatorServiceTest {

    @BeforeEach
    fun before() {
        TockObfuscatorService.registerStringObfuscator(SimpleObfuscator("\\d{9}".toRegex(), "sososecret"))
    }

    @AfterEach
    fun after() {
        TockObfuscatorService.deregisterObfuscators()
    }

    @Test
    fun obfuscate_shouldUpdateText_WhenPatternFound() {
        assertEquals("aze sososecret 223 sososecret ds", obfuscate("aze 222777777 223 199999999 ds"))
    }

    @Test
    fun `obfuscate has to obfuscate obfuscated ranges`() {
        val obfuscated = obfuscate("abcdefghijklm", obfuscatedRanges = listOf(1..3, 5..6))
        assertEquals("a***e**hijklm", obfuscated)
    }
}
