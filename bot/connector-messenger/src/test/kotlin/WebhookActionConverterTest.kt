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

package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.messenger.model.Recipient
import ai.tock.bot.connector.messenger.model.Sender
import ai.tock.bot.connector.messenger.model.webhook.PostbackWebhook
import ai.tock.bot.connector.messenger.model.webhook.UserActionPayload
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerType.bot
import ai.tock.bot.engine.user.PlayerType.user
import kotlin.test.Test
import kotlin.test.assertEquals

class WebhookActionConverterTest {
    @Test
    fun `GIVEN A nlp postback THEN toEvent returns a Sentence`() {
        val postback =
            PostbackWebhook(
                Sender("senderId"),
                Recipient("recipientId"),
                123,
                UserActionPayload(SendChoice.encodeNlpChoiceId("Hey")),
            )
        val event = WebhookActionConverter.toEvent(postback, "appId")
        assertEquals(
            SendSentence(
                postback.playerId(user),
                "appId",
                postback.recipientId(bot),
                "Hey",
            ).toMessage(),
            (event as? SendSentence)?.toMessage(),
        )
    }
}
