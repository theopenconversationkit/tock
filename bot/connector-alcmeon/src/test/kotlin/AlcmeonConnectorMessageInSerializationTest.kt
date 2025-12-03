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

import ai.tock.bot.connector.alcmeon.AlcmeonConnectorWhatsappMessageIn
import ai.tock.bot.connector.alcmeon.AlcmeonConnectorWhatsappMessageInteractiveEvent
import ai.tock.bot.connector.alcmeon.AlcmeonConnectorWhatsappMessageTextEvent
import ai.tock.bot.connector.whatsapp.model.common.WhatsAppTextBody
import ai.tock.bot.connector.whatsapp.model.webhook.WhatsAppInteractive
import ai.tock.bot.connector.whatsapp.model.webhook.WhatsAppInteractiveButtonReply
import ai.tock.bot.connector.whatsapp.model.webhook.WhatsAppInteractiveListReply
import ai.tock.shared.jackson.mapper
import ai.tock.shared.resource
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AlcmeonConnectorMessageInSerializationTest {
    val whatsAppTextMessage =
        AlcmeonConnectorWhatsappMessageIn(
            userExternalId = "userExternalId",
            userName = "user",
            event =
                AlcmeonConnectorWhatsappMessageTextEvent(
                    text = WhatsAppTextBody("body"),
                ),
        )

    val whatsappInteractiveButtonReply =
        AlcmeonConnectorWhatsappMessageIn(
            userExternalId = "userExternalId",
            userName = "user",
            event =
                AlcmeonConnectorWhatsappMessageInteractiveEvent(
                    interactive =
                        WhatsAppInteractive(
                            listReply = null,
                            buttonReply =
                                WhatsAppInteractiveButtonReply(
                                    title = "title",
                                    payload = "payload",
                                ),
                        ),
                ),
        )

    val whatsappInteractiveListReply =
        AlcmeonConnectorWhatsappMessageIn(
            userExternalId = "userExternalId",
            userName = "user",
            event =
                AlcmeonConnectorWhatsappMessageInteractiveEvent(
                    interactive =
                        WhatsAppInteractive(
                            listReply =
                                WhatsAppInteractiveListReply(
                                    title = "title",
                                    description = "description",
                                    payload = "payload",
                                ),
                            buttonReply = null,
                        ),
                ),
        )

    @Test
    fun `alcmeon connector whats app text message can be serialized`() {
        val expectedJson =
            "{\"user_external_id\":\"userExternalId\",\"user_name\":\"user\",\"event\":{\"text\":{" +
                "\"body\":\"body\"},\"type\":\"text\"},\"backend\":\"whatsapp\"}"

        val json = mapper.writeValueAsString(whatsAppTextMessage)

        assertEquals(expectedJson, json)
    }

    @Test
    fun `alcmeon connector whats app text message can be deserialized`() {
        val json = resource("/whatsappTextMessage.json")

        val message: AlcmeonConnectorWhatsappMessageIn = mapper.readValue(json)

        assertEquals(whatsAppTextMessage, message)
    }

    @Test
    fun `alcmeon connector whats app interactive button reply message can be serialized`() {
        val expectedJson =
            "{\"user_external_id\":\"userExternalId\",\"user_name\":\"user\",\"event\":{\"interactive\":" +
                "{\"button_reply\":{\"title\":\"title\",\"payload\":\"payload\"}},\"type\":\"interactive\"}," +
                "\"backend\":\"whatsapp\"}"

        val json = mapper.writeValueAsString(whatsappInteractiveButtonReply)

        assertEquals(expectedJson, json)
    }

    @Test
    fun `alcmeon connector whats app interactive button reply message can be deserialized`() {
        val json = resource("/whatsappInteractiveButtonReplyMessage.json")

        val message: AlcmeonConnectorWhatsappMessageIn = mapper.readValue(json)

        assertEquals(whatsappInteractiveButtonReply, message)
    }

    @Test
    fun `alcmeon connector whats app interactive list message can be serialized`() {
        val expectedJson =
            "{\"user_external_id\":\"userExternalId\",\"user_name\":\"user\",\"event\":{\"interactive\":" +
                "{\"list_reply\":{\"title\":\"title\",\"description\":\"description\",\"payload\":\"payload\"}}," +
                "\"type\":\"interactive\"},\"backend\":\"whatsapp\"}"

        val json = mapper.writeValueAsString(whatsappInteractiveListReply)

        assertEquals(expectedJson, json)
    }

    @Test
    fun `alcmeon connector whats app interactive list message can be deserialized`() {
        val json = resource("/whatsappInteractiveListMessage.json")

        val message: AlcmeonConnectorWhatsappMessageIn = mapper.readValue(json)

        assertEquals(whatsappInteractiveListReply, message)
    }
}
