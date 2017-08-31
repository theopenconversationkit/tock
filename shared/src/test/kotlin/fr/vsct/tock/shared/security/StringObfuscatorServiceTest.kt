/*
 * Copyright (C) 2017 VSCT
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

package fr.vsct.tock.shared.security

import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


/**
 *
 */
class StringObfuscatorServiceTest {

    @Before
    fun before() {
        StringObfuscatorService.registerObfuscator(SimpleObfuscator("\\d{9}".toRegex(), "sososecret"))
    }

    @After
    fun after() {
        StringObfuscatorService.deregisterObfuscators()
    }

    @Test
    fun obfuscate_shouldUpdateText_WhenPatternFound() {
        assertEquals("aze sososecret 223 sososecret ds", StringObfuscatorService.obfuscate("aze 222777777 223 199999999 ds"))
    }
}