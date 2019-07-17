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
import fr.vsct.tock.bot.connector.messenger.WebhookActionConverter
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.Sender
import fr.vsct.tock.bot.connector.messenger.model.handover.AppRolesWebhook
import fr.vsct.tock.bot.connector.messenger.model.handover.PassThreadControl
import fr.vsct.tock.bot.connector.messenger.model.handover.PassThreadControlWebhook
import fr.vsct.tock.bot.connector.messenger.model.handover.RequestThreadControl
import fr.vsct.tock.bot.connector.messenger.model.handover.RequestThreadControlWebhook
import fr.vsct.tock.bot.connector.messenger.model.handover.TakeThreadControl
import fr.vsct.tock.bot.connector.messenger.model.handover.TakeThreadControlWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.Message
import fr.vsct.tock.bot.connector.messenger.model.webhook.MessageEcho
import fr.vsct.tock.bot.connector.messenger.model.webhook.MessageEchoWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.MessageWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.Optin
import fr.vsct.tock.bot.connector.messenger.model.webhook.OptinWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.PostbackWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.UserActionPayload
import fr.vsct.tock.bot.connector.messenger.model.webhook.Webhook
import fr.vsct.tock.bot.engine.action.ActionMetadata
import fr.vsct.tock.bot.engine.action.ActionVisibility
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.shared.jackson.mapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WebhookDeserializationTest {

    @Test
    fun testMessageWebhookDeserialization() {
        val m = MessageWebhook(Sender("1"), Recipient("2"), 1L, Message("aa", "text"))
        val s = mapper.writeValueAsString(m)
        assertEquals(m, mapper.readValue<Webhook>(s))
    }

    @Test
    fun testMessageWebhookWithEmptyAttachmentDeserialization() {
        val m = MessageWebhook(Sender("1"), Recipient("2"), 1L, Message("aa", "text"))
        val s =
            """{"sender":{"id":"1"},"recipient":{"id":"2"},"timestamp":1,"message":{"mid":"aa","seq":2,"text":"text","attachments":[{}]}}"""
        assertEquals(m, mapper.readValue<Webhook>(s))
    }

    @Test
    fun testMessageEchoWebhookDeserialization() {
        val m = MessageEchoWebhook(Sender("1"), Recipient("2"), 1L, MessageEcho("aa", "text", appId = 123L))
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
        val output = mapper.readValue<Webhook>(input)
        assertEquals(
            MessageWebhook(
                Sender("USER_ID"),
                Recipient("PAGE_ID"),
                1464990849275,
                Message(
                    "mid.1464990849238:b9a22a2bcb1de31773",
                    "Red",
                    emptyList(),
                    UserActionPayload("DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_RED")
                )
            ), output
        )
    }

    @Test
    fun `PassThreadControlWebhook is deserialized successfully `() {
        val input = """
               {
  "sender":{
    "id":"<PSID>"
  },
  "recipient":{
    "id":"<PAGE_ID>"
  },
  "timestamp":1458692752478,
  "pass_thread_control":{
    "new_owner_app_id":"123456789",
    "metadata":"Additional content that the caller wants to set"
  }
}
            """
        val output = mapper.readValue<Webhook>(input)
        assertEquals(
            PassThreadControlWebhook(
                sender = Sender("<PSID>"),
                recipient = Recipient(id = "<PAGE_ID>"),
                timestamp = 1458692752478,
                passThreadControl = PassThreadControl(
                    newOwnerAppId = "123456789",
                    metadata = "Additional content that the caller wants to set"
                )
            ),
            output
        )
    }

    @Test
    fun `TakeThreadControlWebhook is deserialized successfully `() {
        val input = """
               {
  "sender":{
    "id":"<PSID>"
  },
  "recipient":{
    "id":"<PAGE_ID>"
  },
  "timestamp":1458692752478,
  "take_thread_control":{
    "previous_owner_app_id":"123456789",
    "metadata":"Additional content that the caller wants to set"
  }
}
            """
        val output = mapper.readValue<Webhook>(input)
        assertEquals(
            TakeThreadControlWebhook(
                sender = Sender("<PSID>"),
                recipient = Recipient(id = "<PAGE_ID>"),
                timestamp = 1458692752478,
                takeThreadControl = TakeThreadControl(
                    previousOwnerAppId = "123456789",
                    metadata = "Additional content that the caller wants to set"
                )
            ),
            output
        )
    }

    @Test
    fun `RequestThreadControlWebhook is deserialized successfully `() {
        val input = """
               {
  "sender":{
    "id":"<PSID>"
  },
  "recipient":{
    "id":"<PAGE_ID>"
  },
  "timestamp":1458692752478,
  "request_thread_control":{
    "requested_owner_app_id":"123456789",
    "metadata":"Additional content that the caller wants to set"
  }
}
            """
        val output = mapper.readValue<Webhook>(input)
        assertEquals(
            RequestThreadControlWebhook(
                sender = Sender("<PSID>"),
                recipient = Recipient(id = "<PAGE_ID>"),
                timestamp = 1458692752478,
                requestThreadControl = RequestThreadControl(
                    requestOwnerAppId = "123456789",
                    metadata = "Additional content that the caller wants to set"
                )
            ),
            output
        )
    }

    @Test
    fun `AppRolesWebhook is deserialized successfully `() {
        val input = """
               {
  "recipient":{
    "id":"<PSID>"
  },
  "timestamp":1458692752478,
  "app_roles":{
    "123456789":["primary_receiver"]
  }
}
            """
        val output = mapper.readValue<Webhook>(input)
        assertEquals(
            AppRolesWebhook(
                recipient = Recipient(id = "<PSID>"),
                timestamp = 1458692752478,
                appRoles = mapOf("123456789" to listOf("primary_receiver"))
            ),
            output
        )
    }

    @Test
    fun testEmailQuickReplyWebhookDeserialization() {
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
                "    \"text\": \"test@test.com\",\n" +
                "    \"quick_reply\": {\n" +
                "      \"payload\": \"test@test.com\"\n" +
                "    }\n" +
                "  }\n" +
                "} "
        val output = mapper.readValue<Webhook>(input)
        assertEquals(
            MessageWebhook(
                Sender("USER_ID"),
                Recipient("PAGE_ID"),
                1464990849275,
                Message(
                    "mid.1464990849238:b9a22a2bcb1de31773",
                    "test@test.com",
                    emptyList(),
                    UserActionPayload("test@test.com")
                )
            ), output
        )
    }

    @Test
    fun `test emailQuickReply return email in sentence`() {
        val message = MessageWebhook(
            Sender("USER_ID"),
            Recipient("PAGE_ID"),
            1464990849275,
            Message(
                "mid.1464990849238:b9a22a2bcb1de31773",
                "test@test.com",
                emptyList(),
                UserActionPayload("test@test.com")
            )
        )

        val event = WebhookActionConverter.toEvent(message, "appId") as SendSentence
        val expectedEvent = SendSentence(
            PlayerId("USER_ID", PlayerType.bot),
            "appId",
            PlayerId("PAGE_ID", PlayerType.user),
            "test@test.com",
            metadata = ActionMetadata(visibility = ActionVisibility.public)
        )
        val eventMessage = event.messages[0]
        assert(eventMessage is MessageWebhook)
        assert((eventMessage as MessageWebhook).message.quickReply is UserActionPayload)
        assertEquals((eventMessage.message.quickReply as UserActionPayload).payload,"test@test.com")
        assertEquals(event.text,expectedEvent.text)
        assertEquals(event.playerId.type,PlayerType.user)
        assertEquals(event.recipientId.type,PlayerType.bot)
    }

    @Test
    fun `test email regex to recognize type of payload`() {
        assert(!UserActionPayload("kkjalda").hasEmailPayloadFromMessenger())
        assert(!UserActionPayload("kkjalda@hjk@zad.fr").hasEmailPayloadFromMessenger())
        assert(!UserActionPayload("test@@zddd.fr").hasEmailPayloadFromMessenger())
        assert(!UserActionPayload("test@gmai)-l.fr").hasEmailPayloadFromMessenger())
        assert(!UserActionPayload("test@gmaiàl.fr").hasEmailPayloadFromMessenger())
        assert(!UserActionPayload("test&@gmail.fr").hasEmailPayloadFromMessenger())
        assert(!UserActionPayload("test@gmail;fr").hasEmailPayloadFromMessenger())
        assert(!UserActionPayload("test@gmail\$fr").hasEmailPayloadFromMessenger())
        assert(UserActionPayload("test@gma.il.fr").hasEmailPayloadFromMessenger())
        assert(UserActionPayload("test@gmail.fr").hasEmailPayloadFromMessenger())
        assert(UserActionPayload("test8879@gmail.fr").hasEmailPayloadFromMessenger())
        assert(UserActionPayload("8879@gmail.com").hasEmailPayloadFromMessenger())

    }

}