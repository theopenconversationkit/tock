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

package ai.tock.bot.admin

import ai.tock.bot.admin.model.BotMediaActionDescriptor
import ai.tock.bot.admin.model.BotMediaCardDescriptor
import ai.tock.shared.jackson.mapper
import ai.tock.translator.I18nLabel
import org.litote.kmongo.toId
import kotlin.test.Test
import kotlin.test.assertEquals

class BotMediaMessageSerializationTest {

    @Test
    fun `check BotMediaMessage serialization`() {
        val card = BotMediaCardDescriptor(
            null,
            null,
            null,
            listOf(
                BotMediaActionDescriptor(
                    I18nLabel(
                        "id".toId(),
                        "namespace",
                        "category",
                        LinkedHashSet()
                    ),
                    "https://demo.tock.ai"
                )
            )
        )
        val json = mapper.writeValueAsString(card)
        assertEquals(
            "{\"actions\":[{\"title\":{\"_id\":\"id\",\"namespace\":\"namespace\",\"category\":\"category\",\"i18n\":[],\"defaultLabel\":null,\"defaultLocale\":\"en\",\"defaultI18n\":[],\"version\":0},\"url\":\"https://demo.tock.ai\",\"type\":\"action\"}],\"fillCarousel\":false,\"type\":\"card\"}",
            json
        )
    }
}
