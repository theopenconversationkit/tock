/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.duckling.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JSONValueTest {
    @Test
    fun `GIVEN an answer of Duckling WHEN it is parsed THEN its values are read as with Vert_x`() {
        val json =
            JSONValue.parseArray(
                """
                [{":dim":"number",":start":0,":end":4,":value":{":value":3.5,":type":"value"}},
                 {":dim":"time",":start":5,":end":10,":value":{":values":[{":value":"2026-11-24T00:00:00.000+01:00",":grain":"day"}]}}]
                """.trimIndent(),
            )

        assertEquals(2, json.iterable().count())
        val number = json[0]
        assertEquals("number", number[":dim"].string())
        assertEquals(4, number[":end"].int())
        assertEquals(3.5, number[":value"][":value"].number())
        val time = json[1][":value"][":values"]
        assertEquals("day", time[0][":grain"].string())
        assertTrue(json[1][":unknown"].isNull())
        assertTrue(JSONValue.parseArray("[]").isEmpty())
    }
}
