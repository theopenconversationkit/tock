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

package fr.vsct.tock.bot.connector.messenger.json.webhook

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.Sender
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.webhook.Message
import fr.vsct.tock.bot.connector.messenger.model.webhook.MessageEcho
import fr.vsct.tock.bot.connector.messenger.model.webhook.MessageEchoWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.MessageWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.Optin
import fr.vsct.tock.bot.connector.messenger.model.webhook.OptinWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.UserActionPayload
import fr.vsct.tock.bot.connector.messenger.model.webhook.PostbackWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.Webhook
import fr.vsct.tock.shared.jackson.mapper
import org.junit.Test
import kotlin.test.assertEquals

class WebhookDeserializationTest {

    @Test
    fun testMessageWebhookDeserialization() {
        val m = MessageWebhook(Sender("1"), Recipient("2"), 1L, Message("aa", 2, "text"))
        val s = mapper.writeValueAsString(m)
        assertEquals(m, mapper.readValue<Webhook>(s))
    }

    @Test
    fun testMessageWebhookWithEmptyAttachmentDeserialization() {
        val m = MessageWebhook(Sender("1"), Recipient("2"), 1L, Message("aa", 2, "text"))
        val s = """{"sender":{"id":"1"},"recipient":{"id":"2"},"timestamp":1,"message":{"mid":"aa","seq":2,"text":"text","attachments":[{}]}}"""
        assertEquals(m, mapper.readValue<Webhook>(s))
    }

    @Test
    fun testMessageEchoWebhookDeserialization() {
        val m = MessageEchoWebhook(Sender("1"), Recipient("2"), 1L, MessageEcho("aa", 2, "text", appId = 123L))
        val s = mapper.writeValueAsString(m)
        assertEquals(m, mapper.readValue<Webhook>(s))
    }

    @Test
    fun testOptinWebhookDeserialization() {
        val m = OptinWebhook(Sender("1"), Recipient("2"), 1L, Optin("a"))
        val s = mapper.writeValueAsString(m)
        assertEquals(m, mapper.readValue<Webhook>(s))
    }

    @Test
    fun testPostbackWebhookDeserialization() {
        val m = PostbackWebhook(Sender("1"), Recipient("2"), 1L, UserActionPayload("a"))
        val s = mapper.writeValueAsString(m)
        assertEquals(m, mapper.readValue<Webhook>(s))
    }

    @Test
    fun testQuickReplayWebhookDeserialization() {
        val input = "{\n" +
                "  \"sender\": {\n" +
                "    \"id\": \"USER_ID\"\n" +
                "  },\n" +
                "  \"recipient\": {\n" +
                "    \"id\": \"PAGE_ID\"\n" +
                "  },\n" +
                "  \"timestamp\": 1464990849275,\n" +
                "  \"message\": {\n" +
                "    \"mid\": \"mid.1464990849238:b9a22a2bcb1de31773\",\n" +
                "    \"seq\": 69,\n" +
                "    \"text\": \"Red\",\n" +
                "    \"quick_reply\": {\n" +
                "      \"payload\": \"DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_RED\"\n" +
                "    }\n" +
                "  }\n" +
                "} "
        val output = mapper.readValue<MessageWebhook>(input)
        assertEquals(MessageWebhook(
                Sender("USER_ID"),
                Recipient("PAGE_ID"),
                1464990849275,
                Message(
                    "mid.1464990849238:b9a22a2bcb1de31773",
                        69,
                        "Red",
                        emptyList(),
                        UserActionPayload("DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_RED")
                )
        ), output)
    }
}