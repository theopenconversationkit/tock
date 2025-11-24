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

package ai.tock.nlp.entity.date

import ai.tock.nlp.entity.StringValue
import ai.tock.nlp.entity.UnknownValue
import ai.tock.nlp.entity.Value
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class UnknownValueDeserializationTest {
    @Test
    fun testSerializeAndDeserialize() {
        val v = UnknownValue()
        val s = mapper.writeValueAsString(v)
        assertTrue(mapper.readValue(s, Value::class.java) is UnknownValue)
    }

    @Test
    fun testDeserializeUnknown() {
        assertTrue(mapper.readValue("""{"@type":"a","b":"c"}""", Value::class.java) is UnknownValue)
    }

    data class V(val a: Value, val b: Value, val c: Value)

    @Test
    fun testDeserializeListOfUnknown() {
        val r =
            mapper.readValue(
                """{"a":{"@type":"a","k":"c"},"b":{"@type":"a","h":"c"},"c":{"@type":"ai.tock.nlp.entity.StringValue","value":"v"}}""",
                V::class.java,
            )
        assertTrue(r.a is UnknownValue)
        assertTrue(r.b is UnknownValue)
        assertEquals(StringValue("v"), r.c)
    }
}
