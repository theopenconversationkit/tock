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

package model.webhook

import ai.tock.bot.connector.whatsapp.cloud.model.common.TextContent
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsAppCloudMessage
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsAppCloudTextMessage
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsappCloudUnknownMessage
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class WhatsAppCloudMessagesDeserializationTest {
    @Test
    fun testMessageWebhookDeserialization() {
        val m =
            WhatsAppCloudTextMessage(
                text = TextContent("Hello, World!"),
                id = "aaa",
                from = "bbb",
                timestamp = Instant.now().toString(),
            )
        val s = mapper.writeValueAsString(m)
        assertEquals(m, mapper.readValue<WhatsAppCloudMessage>(s))
    }

    @Test
    fun testUnknownMessageDeserialization() {
        val m =
            WhatsappCloudUnknownMessage(
                id = "aaa",
                from = "bbb",
                timestamp = "2025-12-30T10:50:52.355219Z",
                rawType = "unsupported",
                additionalProperties =
                    mutableMapOf(
                        "thing" to "Hello, World!",
                    ),
            )
        val s =
            """
            {
              "thing" : "Hello, World!",
              "id" : "aaa",
              "from" : "bbb",
              "timestamp" : "2025-12-30T10:50:52.355219Z",
              "errors" : [ ],
              "type" : "unsupported"
            }
            """.trimIndent()
        mapper.writeValueAsString(m)
        assertEquals(m, mapper.readValue<WhatsAppCloudMessage>(s))
    }
}
