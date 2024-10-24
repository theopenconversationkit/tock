/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.jupiter.api.Test

class EncryptorsTest {
    @Test
    fun `sha256Uuid should produce valid UUIDs`() {
        val result = sha256Uuid("+33601234567")
        assertEquals(2, result.variant())
        assertEquals(5, result.version())
        assertEquals("a6db4f9b-e00f-5328-9e91-5b8d560cc4b9", result.toString(), "result should not change between versions")
    }

    @Test
    fun `sha256Uuid should preserve hash properties`() {
        val values = listOf("+33611111111", "+33611111112", "abcdef", "x", "y", "1", "11")
        val uuids = values.map { sha256Uuid(it) }
        assertEquals(values.size, uuids.distinct().size, "No collision")
        assertEquals(uuids, values.map { sha256Uuid(it) }, "Determinism")
        assertNotEquals(uuids, uuids.map { sha256Uuid(it.toString()) }, "No idempotence")
    }
}
