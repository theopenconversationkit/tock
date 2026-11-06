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

package ai.tock.bot.connector.web

import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.MetadataEvent.Companion.RESPONSE_ID_METADATA
import ai.tock.bot.engine.event.MetadataEvent.Companion.STREAM_RESPONSE_METADATA
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.connector.web.sse.SseEndpoint.Companion.webMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class WebConnectorCallbackStreamingTest {
    private val sent = mutableListOf<WebConnectorResponse>()

    private fun callback(sender: ((WebConnectorResponse) -> Unit)?) =
        WebConnectorCallback(
            applicationId = "app",
            errorListener = null,
            locale = java.util.Locale.FRENCH,
            context = null,
            metadata = java.util.concurrent.ConcurrentHashMap(mapOf(RESPONSE_ID_METADATA to "r1")),
            webMapper = webMapper,
            eventId = "e1",
            messageProcessor = WebMessageProcessor(false),
            streamedResponse = false,
            sseTokenSender = sender,
        )

    @Test
    fun `tokens are sent as accumulated messages flagged as streamed`() {
        val callback = callback { sent.add(it) }
        callback.sendStreamedToken("Hel")
        callback.sendStreamedToken("lo")

        assertEquals(listOf("Hel", "Hello"), sent.map { it.responses.single().text })
        sent.forEach {
            assertEquals("true", it.metadata[STREAM_RESPONSE_METADATA])
            assertEquals("r1", it.metadata[RESPONSE_ID_METADATA])
        }
    }

    @Test
    fun `final response keeps the stream flag and is not merged with streamed text`() {
        val callback = callback { sent.add(it) }
        callback.sendStreamedToken("Hello")
        val playerId = PlayerId("")
        val final = callback.createResponse(listOf(SendSentence(playerId, "", playerId, "Hello world")))

        assertEquals("Hello world", final.responses.single().text)
        assertEquals("true", final.metadata[STREAM_RESPONSE_METADATA])
    }

    @Test
    fun `no stream flag without streaming`() {
        val callback = callback(null)
        callback.sendStreamedToken("Hello")
        val playerId = PlayerId("")
        val final = callback.createResponse(listOf(SendSentence(playerId, "", playerId, "Hello")))

        assertNull(final.metadata[STREAM_RESPONSE_METADATA])
        assertEquals(0, sent.size)
    }
}
