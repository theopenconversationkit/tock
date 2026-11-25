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

package ai.tock.bot.connector.slack

import io.mockk.every
import io.mockk.mockk
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SlackEncoderTest {
    private val context: RoutingContext = mockk(relaxed = true)
    private val urlEncodedString = "arg1=val1&arg2=val2&arg3=val3"

    @Test
    fun testConvertUrlEncodedStringToJson() {
        every { context.body().asString() } returns urlEncodedString

        val expectedJson =
            JsonObject()
                .put("arg1", "val1")
                .put("arg2", "val2")
                .put("arg3", "val3")
                .toString()
        assertEquals(expectedJson, context.convertUrlEncodedStringToJson())
    }

    @Test
    fun `GIVEN an outgoing webhook body whose text contains encoded separators WHEN it is converted THEN each value is kept whole`() {
        val json = JsonObject(parseJson("user_id=U1&text=a%3Db+%26+c%25&trigger_word=bot&empty"))

        assertEquals("U1", json.getString("user_id"))
        assertEquals("a=b & c%", json.getString("text"))
        assertEquals("bot", json.getString("trigger_word"))
        assertEquals("", json.getString("empty"))
    }
}
