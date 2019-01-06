/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.messenger.json.send

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.UserProfile
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.send.Element
import fr.vsct.tock.bot.connector.messenger.model.send.GenericPayload
import fr.vsct.tock.bot.connector.messenger.model.send.LocationQuickReply
import fr.vsct.tock.bot.connector.messenger.model.send.MediaElement
import fr.vsct.tock.bot.connector.messenger.model.send.MediaPayload
import fr.vsct.tock.bot.connector.messenger.model.send.MediaType
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.send.MessageTag
import fr.vsct.tock.bot.connector.messenger.model.send.PostbackButton
import fr.vsct.tock.bot.connector.messenger.model.send.QuickReply
import fr.vsct.tock.bot.connector.messenger.model.send.TextMessage
import fr.vsct.tock.bot.connector.messenger.model.send.TextQuickReply
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.shared.jackson.mapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class MessageRequestDeserializationTest {

    @Test
    fun testMessageRequestWithButtonDeserialization() {
        val m = MessageRequest(
            Recipient("2"),
            AttachmentMessage(
                Attachment(
                    AttachmentType.template,
                    GenericPayload(
                        listOf(
                            Element(
                                "title",
                                buttons = listOf(
                                    PostbackButton(
                                        "payload",
                                        "titleButton"
                                    )
                                )
                            )
                        )
                    )
                )
            )
            , tag = MessageTag.FEATURE_FUNCTIONALITY_UPDATE
        )
        val s = mapper.writeValueAsString(m)
        assertEquals(m, mapper.readValue(s))
    }

    @Test
    fun `media payload deserialization is ok`() {
        val m = MessageRequest(
            Recipient("2"),
            AttachmentMessage(
                Attachment(
                    AttachmentType.template,
                    MediaPayload(
                        listOf(
                            MediaElement(
                                MediaType.image,
                                "http://test.com",
                                buttons = listOf(
                                    PostbackButton(
                                        "payload",
                                        "titleButton"
                                    )
                                )
                            )
                        ),
                        true

                    )
                )
            )
            , tag = MessageTag.FEATURE_FUNCTIONALITY_UPDATE
        )
        val s = mapper.writeValueAsString(m)
        assertEquals(m, mapper.readValue(s))
    }

    @Test
    fun testMessageRequestWithUrlPayload() {
        val m = MessageRequest(
            Recipient("2"),
            AttachmentMessage(Attachment(AttachmentType.image, UrlPayload("http://test/test.png", null, null)))
        )
        val s = mapper.writeValueAsString(m)
        assertEquals(m, mapper.readValue<MessageRequest>(s))
    }

    @Test
    fun testTextQuickReplyDeserialization() {
        val input = "{\n" +
                "        \"content_type\":\"text\",\n" +
                "        \"title\":\"Green\",\n" +
                "        \"payload\":\"DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_GREEN\"\n" +
                "      }"
        val output = mapper.readValue<QuickReply>(input)
        assertEquals(
            TextQuickReply(
                "Green",
                "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_GREEN"
            ),
            output
        )
    }

    @Test
    fun testLocationQuickReplyDeserialization() {
        val input = "{\n" +
                "        \"content_type\":\"location\"\n" +
                "      }"
        val output = mapper.readValue<QuickReply>(input)
        assertEquals(
            LocationQuickReply(),
            output
        )
    }

    @Test
    fun testQuickRepliesMessageRequestDeserialization() {
        val input = "{\n" +
                "  \"recipient\":{\n" +
                "    \"id\":\"USER_ID\"\n" +
                "  },\n" +
                "  \"message\":{\n" +
                "    \"text\":\"Pick a color:\",\n" +
                "    \"quick_replies\":[\n" +
                "      {\n" +
                "        \"content_type\":\"text\",\n" +
                "        \"title\":\"Red\",\n" +
                "        \"payload\":\"DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_RED\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"content_type\":\"text\",\n" +
                "        \"title\":\"Green\",\n" +
                "        \"payload\":\"DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_GREEN\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}"
        val output = mapper.readValue<MessageRequest>(input)
        assertEquals(
            MessageRequest(
                Recipient("USER_ID"),
                TextMessage(
                    "Pick a color:",
                    listOf(
                        TextQuickReply(
                            "Red",
                            "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_RED"
                        ),
                        TextQuickReply(
                            "Green",
                            "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_GREEN"
                        )
                    )
                )

            ),
            output
        )

        assertEquals(output, mapper.readValue(mapper.writeValueAsString(output)))
    }

    @Test
    fun `UserProfile deserialization is ok`() {
        val p =mapper.readValue<UserProfile>("""{"first_name":"Simone","last_name":"En Gare","profile_pic":"https:\/\/platform-lookaside.fbsbx.com\/platform\/profilepic\/?psid=1107678927&width=1024&ext=1549209201&hash=AeQ5_frOwCI51K21","id":"1107678927"}""")
        println(p)
    }

}