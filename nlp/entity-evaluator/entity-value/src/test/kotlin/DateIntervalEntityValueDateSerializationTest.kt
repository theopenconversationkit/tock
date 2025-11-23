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

import ai.tock.nlp.entity.Value
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals

/**
 *
 */
class DateIntervalEntityValueDateSerializationTest {
    @Test
    fun testSerializeAndDeserialize() {
        val v =
            DateIntervalEntityValue(
                DateEntityValue(ZonedDateTime.now(ZoneId.of("Z")), DateEntityGrain.day),
                DateEntityValue(ZonedDateTime.now(ZoneId.of("Z")), DateEntityGrain.day),
            )
        val s = mapper.writeValueAsString(v)
        assertEquals(v, mapper.readValue(s, Value::class.java))
    }
}
