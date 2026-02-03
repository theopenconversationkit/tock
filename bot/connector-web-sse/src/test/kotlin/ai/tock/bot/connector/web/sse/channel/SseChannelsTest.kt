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

package ai.tock.bot.connector.web.sse.channel

import ai.tock.bot.connector.web.WebConnectorResponseContract
import ai.tock.bot.connector.web.sse.botResponse
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import io.vertx.core.Future
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException

internal class SseChannelsTest {
    private val channelDaoMock: ChannelDAO = mockk()
    private val channels = SseChannels(channelDaoMock)

    @Test
    fun `Channels process both missed and new events`() {
        val listenerSlot = slot<ChannelEvent.Handler>()
        val appId = "my-app"
        val recipientId = "user1"
        val expectedMissedResponses =
            listOf(
                botResponse("Hello, are you still there?"),
                botResponse("I think the connection broke"),
            )
        val expectedNewResponses =
            listOf(
                botResponse("Welcome back"),
            )
        every { channelDaoMock.listenChanges(capture(listenerSlot)) } just runs
        every { channelDaoMock.handleMissedEvents(any(), any(), any()) } answers {
            val handler = thirdArg<ChannelEvent.Handler>()
            expectedMissedResponses.forEach {
                handler(ChannelEvent(appId, recipientId, it))
            }
        }
        val responses = mutableListOf<WebConnectorResponseContract>()
        channels.initListeners()
        channels.register(appId, recipientId) {
            responses.add(it)
            Future.succeededFuture<Unit>()
        }.also(channels::sendMissedEvents)
        assertEquals(expectedMissedResponses, responses)
        expectedNewResponses.forEach {
            listenerSlot.captured.invoke(ChannelEvent(appId, recipientId, it))
        }
        assertEquals(expectedMissedResponses + expectedNewResponses, responses)
    }

    @Test
    fun `Channels do not go through database when unnecessary`() {
        val appId = "my-app"
        val recipientId = "user1"
        val message = botResponse("Welcome back")
        val responses = mutableListOf<WebConnectorResponseContract>()
        channels.register(appId, recipientId) {
            responses.add(it)
            Future.succeededFuture<Unit>()
        }
        channels.send(appId, recipientId, message).await()
        assertEquals(listOf(message), responses)
        verify(inverse = true) { channelDaoMock.save(any()) }
    }

    @Test
    fun `Channels register in database when user unavailable on instance`() {
        val appId = "my-app"
        val recipientId = "user1"
        val message = botResponse("Welcome back")
        every { channelDaoMock.save(any()) } just runs
        channels.send(appId, recipientId, message).await()
        verify { channelDaoMock.save(any()) }
    }

    @Test
    fun `Channels register in database when exception occurs`() {
        val appId = "my-app"
        val recipientId = "user1"
        val message = botResponse("Welcome back")
        every { channelDaoMock.save(any()) } just runs
        channels.register(appId, recipientId) {
            Future.failedFuture<Unit>(IOException("Failed to write"))
        }
        channels.send(appId, recipientId, message).await()
        verify { channelDaoMock.save(any()) }
    }
}
