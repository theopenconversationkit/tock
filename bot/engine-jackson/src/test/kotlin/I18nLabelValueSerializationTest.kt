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

package ai.tock.bot.jackson

import ai.tock.shared.jackson.mapper
import ai.tock.translator.I18nLabelValue
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class I18nLabelValueSerializationTest {
    @Test
    fun `json serialization of I18nLabelValue does not contain length`() {
        val value = I18nLabelValue("key", "namespace", "category", "defaultLabel")

        val json = mapper.writeValueAsString(value)

        assertEquals(
            """{"key":"key","namespace":"namespace","category":"category","defaultLabel":"defaultLabel","args":[],"defaultI18n":[]}""",
            json,
        )

        assertEquals(value, mapper.readValue(json))
    }
}
