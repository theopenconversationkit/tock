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

package ai.tock.bot.api.client

import ai.tock.bot.api.model.message.bot.I18nText
import ai.tock.shared.jackson.mapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JacksonTest {
    @Test
    fun `I18nText has to be serialized without length property`() {
        val i18n = I18nText("a")
        assertEquals("""{"text":"a"}""", mapper.writeValueAsString(i18n))
        val i18n2 = I18nText("a", listOf("b"), false)
        assertEquals("""{"text":"a","args":["b"],"toBeTranslated":false}""", mapper.writeValueAsString(i18n2))
    }
}
