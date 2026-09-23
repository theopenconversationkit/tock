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

package model.send.message

import ai.tock.bot.connector.whatsapp.cloud.model.common.TextContent
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotRecipientType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotTextMessage
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WhatsAppCloudSendBotMessageTest {
    @Test
    fun `recipient BSUID is serialized as 'recipient'`() {
        val message =
            WhatsAppCloudSendBotTextMessage(
                text = TextContent("Hello!"),
                recipientType = WhatsAppCloudBotRecipientType.individual,
                recipient = "FR.4260778090837221",
            )
        val json = mapper.readTree(mapper.writeValueAsString(message)) as ObjectNode
        assertEquals("FR.4260778090837221", json.get("recipient").asText())
        assertEquals(false, json.has("to"))
    }
}
