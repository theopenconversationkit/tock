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

package ai.tock.duckling.client

import ai.tock.nlp.entity.DurationValue
import ai.tock.nlp.entity.NumberValue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.test.assertEquals

/**
 *
 */
class DucklingParserIntegrationTest {

    @Test
    fun testParseNumber() {
        val v = DucklingParser.parse("en", "number", ZonedDateTime.now(), "12")
        assertEquals(12, (v.first().value as NumberValue).value)
    }

    @Test
    fun testDuration() {
        val result =
            DucklingParser.parse("fr", "duration", ZonedDateTime.now(), "deux heures et 1 min")
        assertEquals(DurationValue(Duration.parse("PT2H1M")), result.first().value)
    }
}
