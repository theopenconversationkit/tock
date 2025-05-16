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

package ai.tock.bot.connector.whatsapp.send

import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotAction
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotActionButton
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotActionButtonReply
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotActionSection
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotActionSectionProduct
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotBody
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotFooter
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotHeaderType
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotInteractive
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotInteractiveHeader
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotInteractiveType
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotMedia
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotMessageInteractiveMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotRecipientType
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotRow
import ai.tock.shared.jackson.mapper
import ai.tock.shared.resource
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WhatsAppBotMessageInteractiveMessageSerializationTest {

    private val expectedMessage = WhatsAppBotMessageInteractiveMessage(
        recipientType = WhatsAppBotRecipientType.individual,
        interactive = WhatsAppBotInteractive(
            type = WhatsAppBotInteractiveType.button,
            header = WhatsAppBotInteractiveHeader(
                type = WhatsAppBotHeaderType.document,
                document = WhatsAppBotMedia(
                    id = "mediaId",
                    link = "link",
                    caption = "caption",
                    filename = "filename",
                    provider = "provider"
                ),
                image = WhatsAppBotMedia(
                    id = "mediaId2",
                    link = "link2",
                    caption = "caption2",
                    filename = "filename2",
                    provider = "provider2"
                ),
                video = WhatsAppBotMedia(
                    id = "mediaId3",
                    link = "link3",
                    caption = "caption3",
                    filename = "filename3",
                    provider = "provider3"
                ),
                text = "text"
            ),
            body = WhatsAppBotBody("body"),
            footer = WhatsAppBotFooter("footer"),
            action = WhatsAppBotAction(
                button = "button",
                buttons = listOf(
                    WhatsAppBotActionButton(
                        type = "reply",
                        reply = WhatsAppBotActionButtonReply(
                            title = "buttonReply",
                            id = "id",
                        )
                    )
                ),
                sections = listOf(
                    WhatsAppBotActionSection(
                        title = "section",
                        rows = listOf(
                            WhatsAppBotRow(
                                id = "id",
                                title = "row",
                                description = "description"
                            )
                        ),
                        productItems = listOf(
                            WhatsAppBotActionSectionProduct("id")
                        )
                    )
                ),
                catalogId = "catalogId",
                productRetailerId = "productRetailerId",
            )
        )
    )

    @Test
    fun `interactive message can be successfully serialized`() {
        val message = expectedMessage.copy(userId = "userId")
        val expectedJson = "{\"interactive\":{\"type\":\"button\",\"header\":{\"type\":\"document\",\"document\":" +
                "{\"id\":\"mediaId\",\"link\":\"link\",\"caption\":\"caption\",\"filename\":\"filename\",\"provider\":" +
                "\"provider\"},\"image\":{\"id\":\"mediaId2\",\"link\":\"link2\",\"caption\":\"caption2\",\"filename\":" +
                "\"filename2\",\"provider\":\"provider2\"},\"video\":{\"id\":\"mediaId3\",\"link\":\"link3\"," +
                "\"caption\":\"caption3\",\"filename\":\"filename3\",\"provider\":\"provider3\"},\"text\":\"text\"}," +
                "\"body\":{\"text\":\"body\"},\"footer\":{\"text\":\"footer\"},\"action\":{\"button\":\"button\"," +
                "\"buttons\":[{\"type\":\"reply\",\"reply\":{\"title\":\"buttonReply\",\"id\":\"id\"}}]," +
                "\"sections\":[{\"title\":\"section\",\"rows\":[{\"id\":\"id\",\"title\":\"row\",\"description\"" +
                ":\"description\"}],\"product_items\":[{\"product_retailer_id\":\"id\"}]}],\"catalog_id\":\"catalogId\"" +
                ",\"product_retailer_id\":\"productRetailerId\"}},\"recipient_type\":\"individual\",\"type\"" +
                ":\"interactive\",\"userId\$tock_bot_connector_whatsapp\":\"userId\"}"

        val json = mapper.writeValueAsString(message)

        assertEquals(expectedJson, json)
    }

    @Test
    fun `interactive message can be successfully deserialized`() {
        val json = resource("/model/send/interactive.json")

        val message : WhatsAppBotMessageInteractiveMessage = mapper.readValue(json)

        assertEquals(expectedMessage, message)
    }

}
