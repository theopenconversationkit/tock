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

package fr.vsct.tock.bot.connector.ga

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.ga.GAAccountLinking.Companion.getUserId
import fr.vsct.tock.bot.connector.ga.model.request.GARequest
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.stt.SttListener
import fr.vsct.tock.bot.engine.stt.SttService
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.resource
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
        val sttListener = object : SttListener {
            override fun transform(stt: String, locale: Locale): String {
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