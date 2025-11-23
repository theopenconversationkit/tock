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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ga.GAAccountLinking.Companion.getUserId
import ai.tock.bot.connector.ga.model.request.GARequest
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.stt.SttListener
import ai.tock.bot.engine.stt.SttService
import ai.tock.shared.jackson.mapper
import ai.tock.shared.resource
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class WebhookActionConverterTest {
    companion object {
        val appId = "test"
        val optionRequest: GARequest = mapper.readValue(resource("/request_with_option.json"))
        val optionRequestWithTwoArguments: GARequest =
            mapper.readValue(resource("/request_with_option_two_arguments.json"))
        val optionWithRawTextRequest: GARequest = mapper.readValue(resource("/request_with_option_and_raw_text.json"))
        val sttRequest: GARequest = mapper.readValue(resource("/request_with_stt_transformer.json"))
        val googleHomeRequest: GARequest = mapper.readValue(resource("/google-home-request.json"))
        val googleAssistantConnectedRequest: GARequest = mapper.readValue(resource("/request_with_access_token.json"))
    }

    @Test
    fun toEvent_shouldReturnsSendChoice_whenOptionArgAndSameInputText() {
        val e = WebhookActionConverter.toEvent(optionRequest, appId)
        assertTrue(e is SendChoice)
    }

    @Test
    fun toEvent_shouldReturnsSendChoice_whenTwoOptionArgs() {
        val e = WebhookActionConverter.toEvent(optionRequestWithTwoArguments, appId)
        assertTrue(e is SendChoice)
    }

    @Test
    fun toEvent_shouldReturnsSendSentence_whenOptionArgAndDifferentInputText() {
        val e = WebhookActionConverter.toEvent(optionWithRawTextRequest, appId)
        assertTrue(e is SendSentence)
    }

    @Test
    fun toEvent_shouldReturnsSendSentenceWhithSttParsed_whenThereIsSttErrorInText() {
        val sttListener =
            object : SttListener {
                override fun transform(
                    stt: String,
                    locale: Locale,
                ): String {
                    return stt.replace("Deezer", "10h")
                }
            }
        SttService.addListener(sttListener)
        val e = WebhookActionConverter.toEvent(sttRequest, appId)
        assertEquals("ds 10h qs", (e as SendSentence).text)
        SttService.removeListener(sttListener)
    }

    @Test
    fun `GIVEN GoogleHome request THEN toEvent returns conversationId as player id`() {
        val e = WebhookActionConverter.toEvent(googleHomeRequest, appId) as SendSentence
        assertEquals(googleHomeRequest.conversation.conversationId, e.playerId.id)
    }

    @Test
    fun `GIVEN Assistant request THEN toEvent returns conversationId as player id`() {
        val e = WebhookActionConverter.toEvent(optionRequest, appId) as SendChoice
        assertEquals(optionRequest.conversation.conversationId, e.playerId.id)
    }

    @Test
    fun `GIVEN Assistant request with connected user THEN toEvent returns userId from accessToken as player id`() {
        val e = WebhookActionConverter.toEvent(googleAssistantConnectedRequest, appId) as SendSentence
        assertEquals(getUserId(googleAssistantConnectedRequest), e.playerId.id)
    }
}
