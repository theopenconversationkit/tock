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

    private fun callback(
        streamDelta: Boolean = false,
        markdown: Boolean = false,
        sender: ((WebConnectorResponse) -> Unit)?,
    ) =
        WebConnectorCallback(
            applicationId = "app",
            errorListener = null,
            locale = java.util.Locale.FRENCH,
            context = null,
            metadata = java.util.concurrent.ConcurrentHashMap(mapOf(RESPONSE_ID_METADATA to "r1")),
            webMapper = webMapper,
            eventId = "e1",
            messageProcessor = WebMessageProcessor(markdown),
            streamedResponse = false,
            sseTokenSender = sender,
            streamDelta = streamDelta,
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
        val callback = callback(sender = null)
        callback.sendStreamedToken("Hello")
        val playerId = PlayerId("")
        val final = callback.createResponse(listOf(SendSentence(playerId, "", playerId, "Hello")))

        assertNull(final.metadata[STREAM_RESPONSE_METADATA])
        assertEquals(0, sent.size)
    }

    @Test
    fun `final response is written after queued partial frames and late partials are dropped`() {
        val vertx = io.vertx.core.Vertx.vertx()
        try {
            val routingContext = io.mockk.mockk<io.vertx.ext.web.RoutingContext>()
            io.mockk.every { routingContext.vertx() } returns vertx
            val order = java.util.concurrent.CopyOnWriteArrayList<String>()
            val done = java.util.concurrent.CountDownLatch(1)
            val callback =
                WebConnectorCallback(
                    applicationId = "app",
                    errorListener = null,
                    locale = java.util.Locale.FRENCH,
                    context = routingContext,
                    metadata = java.util.concurrent.ConcurrentHashMap(mapOf(RESPONSE_ID_METADATA to "r1")),
                    webMapper = webMapper,
                    eventId = "e1",
                    messageProcessor = WebMessageProcessor(false),
                    streamedResponse = false,
                    sseTokenSender = { order.add(it.responses.single().text ?: "") },
                )
            repeat(50) { callback.sendStreamedToken("a") }
            callback.sendFinalOverSse {
                order.add("FINAL")
                done.countDown()
            }
            callback.sendStreamedToken("late")

            assertEquals(true, done.await(5, java.util.concurrent.TimeUnit.SECONDS))
            Thread.sleep(100)
            assertEquals(51, order.size)
            assertEquals("FINAL", order.last())
        } finally {
            vertx.close()
        }
    }

    @Test
    fun `delta mode sends only the new fragment and the final response stays complete`() {
        val callback = callback(streamDelta = true) { sent.add(it) }
        callback.sendStreamedToken("Hel")
        callback.sendStreamedToken("lo")

        assertEquals(listOf("Hel", "lo"), sent.map { it.responses.single().text })
        assertEquals(listOf("0", "3"), sent.map { it.metadata[STREAM_KEEP_METADATA] })
        sent.forEach {
            assertEquals("true", it.metadata[STREAM_RESPONSE_METADATA])
            assertEquals("delta", it.metadata[STREAM_MODE_METADATA])
        }
        val playerId = PlayerId("")
        val final = callback.createResponse(listOf(SendSentence(playerId, "", playerId, "Hello world")))
        assertEquals("Hello world", final.responses.single().text)
        assertNull(final.metadata[STREAM_MODE_METADATA])
    }

    @Test
    fun `cumulative mode does not carry a stream mode`() {
        val callback = callback { sent.add(it) }
        callback.sendStreamedToken("Hel")
        assertNull(sent.single().metadata[STREAM_MODE_METADATA])
    }

    @Test
    fun `delta frames rebuild exactly the text of the default mode when markdown is rendered`() {
        val tokens = listOf("Bonjour", " **le", " monde**", "\n\n- un", "\n- deux")
        val cumulative = mutableListOf<WebConnectorResponse>()
        val reference = callback(markdown = true) { cumulative.add(it) }
        tokens.forEach { reference.sendStreamedToken(it) }

        val callback = callback(streamDelta = true, markdown = true) { sent.add(it) }
        tokens.forEach { callback.sendStreamedToken(it) }

        var text = ""
        sent.forEach {
            val keep = it.metadata.getValue(STREAM_KEEP_METADATA).toInt()
            text = text.substring(0, keep) + it.responses.single().text
        }
        assertEquals(cumulative.last().responses.single().text, text)
        assertEquals(true, text.contains("<ul>"))
    }
}
