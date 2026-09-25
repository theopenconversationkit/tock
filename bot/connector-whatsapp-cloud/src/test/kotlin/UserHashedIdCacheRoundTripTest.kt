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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.connector.whatsapp.cloud.services.SendActionConverter
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Reproduces the receive -> reply round-trip performed by [WebhookActionConverter] and
 * [SendActionConverter] to guard against regressions where the bot's answer can no longer be
 * routed to the real WhatsApp BSUID, since it is now stored directly (unhashed) in DB.
 */
class UserHashedIdCacheRoundTripTest {
    private val apiService = mockk<WhatsAppCloudApiService>()

    @Test
    fun `reply to a BSUID sender resolves back to the BSUID`() {
        val bsuid = "FR.4260778090837221"

        val reply =
            SendSentence(
                PlayerId("appId", PlayerType.bot),
                "appId",
                PlayerId(bsuid),
                "Hi there!",
            )

        val botMessage = SendActionConverter.toBotMessage(apiService, reply)
        checkNotNull(botMessage)
        val json = mapper.readTree(mapper.writeValueAsString(botMessage)) as ObjectNode
        assertEquals(bsuid, json.get("recipient").asText())
    }
}
