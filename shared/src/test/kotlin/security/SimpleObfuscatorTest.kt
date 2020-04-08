/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.shared.security

import ai.tock.shared.security.StringObfuscatorMode.display
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


/**
 *
 */
class SimpleObfuscatorTest {

    @Test
    fun obfuscate_shouldUpdateText_WhenPatternFound() {
        val obf = SimpleObfuscator("\\d{9}".toRegex(), "sososecret")
        assertEquals("aze sososecret 223 sososecret ds", obf.obfuscate("aze 222777777 223 199999999 ds"))
    }

    @Test
    fun obfuscate_shouldUseDisplayedText_WhenDisplayModeIsUsed() {
        val obf = SimpleObfuscator("\\d{9}".toRegex(), "sososecret", "?")
        val obfuscated = obf.obfuscate("aze 222777777 223 199999999 ds")
        assertEquals("aze sososecret 223 sososecret ds", obfuscated)
        assertEquals("aze ? 223 ? ds", obf.obfuscate(obfuscated, display))
    }
}