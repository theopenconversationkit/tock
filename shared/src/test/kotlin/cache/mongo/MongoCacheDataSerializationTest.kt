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

package ai.tock.shared.cache.mongo

import ai.tock.shared.jackson.AnyValueWrapper
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import java.util.Arrays
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class MongoCacheDataSerializationTest {

    @Test
    fun `test serialize and deserialize`() {
        val data =
            MongoCacheData(type = "type", s = "s", b = ByteArray(19) { it.toByte() }, a = AnyValueWrapper("test"))
        val json = mapper.writeValueAsString(data)
        val newData: MongoCacheData = mapper.readValue(json)
        assertTrue(Arrays.equals(data.b, newData.b))
        assertEquals(data.a, newData.a)
        assertEquals(data.date, newData.date)
        assertEquals(data.id, newData.id)
        assertEquals(data.s, newData.s)
        assertEquals(data.type, newData.type)
    }
}
