/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.bot.connector.web.WebConnectorResponse
import ai.tock.bot.connector.web.WebMessage
import ai.tock.bot.connector.web.channel.ChannelDAO
import ai.tock.bot.connector.web.channel.ChannelEvent
import ai.tock.bot.connector.web.channel.Channels
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.vertx.core.Future
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


internal class ChannelsTest {
    private val channelDaoMock: ChannelDAO = mockk()

    @BeforeEach
    fun setUp() {
        injector.inject(Kodein {
            bind<ChannelDAO>() with singleton { channelDaoMock }
        })
    }

    @Test
    fun `Channels process both missed and new events`() {
        val listenerSlot = slot<ChannelEvent.Handler>()
        val appId = "my-app"
        val recipientId = "user1"
        val expectedMissedResponses = listOf(
            WebConnectorResponse(listOf(WebMessage("Hello, are you still there?"))),
            WebConnectorResponse(listOf(WebMessage("I think the connection broke")))
        )
        val expectedNewResponses = listOf(
            WebConnectorResponse(listOf(WebMessage("Welcome back")))
        )
        every { channelDaoMock.listenChanges(capture(listenerSlot)) } just runs
        every { channelDaoMock.handleMissedEvents(appId, recipientId, any()) } answers {
            val handler = thirdArg<ChannelEvent.Handler>()
            expectedMissedResponses.forEach {
                handler(ChannelEvent(appId, recipientId, it))
            }
        }
        val channels = Channels()
        val responses = mutableListOf<WebConnectorResponse>()
        channels.register(appId, recipientId) {
            responses.add(it)
            Future.succeededFuture()
        }
        assertEquals(expectedMissedResponses, responses)
        expectedNewResponses.forEach {
            listenerSlot.captured.invoke(ChannelEvent(appId, recipientId, it))
        }
        assertEquals(expectedMissedResponses + expectedNewResponses, responses)
    }
}
