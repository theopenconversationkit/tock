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

import ai.tock.shared.security.decrypt
import ai.tock.shared.security.encrypt
import org.junit.jupiter.api.Test
import java.security.SecureRandom
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 *
 */
class EncryptorTest {
    @Test
    fun testEncryptAndDecrypt() {
        val s = String(SecureRandom().generateSeed(30))
        val encrypted1 = encrypt(s)
        assertEquals(s, decrypt(encrypted1))
        val encrypted2 = encrypt(s)
        assertEquals(s, decrypt(encrypted2))
        assertNotEquals(encrypted1, encrypted2)
    }

    @Test
    fun testEncryptAndDecryptForEmptyString() {
        val s = ""
        val encrypted1 = encrypt(s)
        assertEquals(s, decrypt(encrypted1))
        val encrypted2 = encrypt(s)
        assertEquals(s, decrypt(encrypted2))
        assertNotEquals(encrypted1, encrypted2)
    }
}
