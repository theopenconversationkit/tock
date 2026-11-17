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

package ai.tock.bot.connector.mattermost

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.mattermost.model.MattermostMessageOut
import ai.tock.bot.connector.mattermost.model.MattermostPost
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.bot
import ai.tock.shared.Executor
import ai.tock.shared.jackson.mapper
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MattermostConnectorReplyInThreadTest {
    private val connectorId = "mattermost"
    private val sourceChannelId = "source-channel"
    private val postId = "yxgx6rfhq3fafbsrffa8wguy4a"
    private val threadRootId = "pq9fbe1yxtgm5ysc4z9mc3xbne"
    private val sentMessages = mutableListOf<MattermostMessageOut>()
    private val sentMessage get() = sentMessages.single()

    @BeforeEach
    fun setUp() {
        val executor = mockk<Executor>()
        every { executor.executeBlocking(any<Duration>(), any()) } answers { secondArg<() -> Unit>().invoke() }
        tockInternalInjector =
            KodeinInjector().apply {
                inject(Kodein { bind<Executor>() with provider { executor } })
            }
        mockkConstructor(MattermostClient::class)
        every { anyConstructed<MattermostClient>().sendMessage(capture(sentMessages)) } returns true
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        tockInternalInjector = KodeinInjector()
    }

    private fun connector(parameters: Map<String, String> = emptyMap()) =
        MattermostConnectorProvider.connector(
            ConnectorConfiguration(
                connectorId,
                "/mattermost",
                mattermostConnectorType,
                parameters =
                    mapOf(
                        "_url_" to "http://localhost:8065",
                        "_token_" to "incoming-token",
                        "_outgoing_token_" to "outgoing-token",
                    ) + parameters,
            ),
        )

    private fun send(
        connector: Connector,
        callback: ConnectorCallback = MattermostConnectorCallback(connectorId, sourceChannelId, postId),
    ) {
        connector.send(
            SendSentence(PlayerId(connectorId, bot), connectorId, PlayerId("user"), "answer"),
            callback,
        )
    }

    @Test
    fun `answer is sent in the thread of the post when reply in thread is enabled`() {
        send(connector(mapOf("_reply_in_thread_" to "1")))

        assertEquals(postId, sentMessage.rootId)
    }

    @Test
    fun `answer is sent as a new post when reply in thread is disabled`() {
        send(connector(mapOf("_reply_in_thread_" to "0")))

        assertNull(sentMessage.rootId)
    }

    @Test
    fun `answer is sent as a new post when reply in thread is not configured`() {
        send(connector())

        assertNull(sentMessage.rootId)
    }

    @Test
    fun `answer is sent as a new post when the incoming post id is unknown`() {
        send(
            connector(mapOf("_reply_in_thread_" to "1")),
            MattermostConnectorCallback(connectorId, sourceChannelId, null),
        )

        assertNull(sentMessage.rootId)
    }

    @Test
    fun `answer is sent as a new post when the request does not come from a Mattermost post`() {
        send(
            connector(mapOf("_reply_in_thread_" to "1")),
            ConnectorCallbackBase(connectorId, mattermostConnectorType),
        )

        assertNull(sentMessage.rootId)
    }

    @Test
    fun `answer is sent as a new post when it goes to another channel than the post`() {
        send(connector(mapOf("_reply_in_thread_" to "1", "_dedicated_room_id_" to "dedicated-channel")))

        assertEquals("dedicated-channel", sentMessage.channel)
        assertNull(sentMessage.rootId)
    }

    @Test
    fun `answer is sent in the thread when the dedicated channel is the channel of the post`() {
        send(connector(mapOf("_reply_in_thread_" to "1", "_dedicated_room_id_" to sourceChannelId)))

        assertEquals(postId, sentMessage.rootId)
    }

    @Test
    fun `answer is sent in the thread root when the post is a reply in a thread`() {
        val connector = connector(mapOf("_reply_in_thread_" to "1"))
        val callback = MattermostConnectorCallback(connectorId, sourceChannelId, postId) { threadRootId }

        send(connector, callback)
        send(connector, callback)

        assertEquals(listOf(threadRootId, threadRootId), sentMessages.map { it.rootId })
    }

    @Test
    fun `thread root is resolved only once per request`() {
        var calls = 0
        val connector = connector(mapOf("_reply_in_thread_" to "1"))
        val callback =
            MattermostConnectorCallback(connectorId, sourceChannelId, postId) {
                calls++
                threadRootId
            }

        send(connector, callback)
        send(connector, callback)

        assertEquals(1, calls)
    }

    @Test
    fun `answer is sent in the thread of the post when the thread root cannot be resolved`() {
        send(
            connector(mapOf("_reply_in_thread_" to "1")),
            MattermostConnectorCallback(connectorId, sourceChannelId, postId) { null },
        )

        assertEquals(postId, sentMessage.rootId)
    }

    @Test
    fun `thread root is not resolved when reply in thread is disabled`() {
        var calls = 0
        send(
            connector(),
            MattermostConnectorCallback(connectorId, sourceChannelId, postId) {
                calls++
                threadRootId
            },
        )

        assertEquals(0, calls)
        assertNull(sentMessage.rootId)
    }

    @Test
    fun `answer is sent as a new post when Mattermost rejects the reply in thread`() {
        every { anyConstructed<MattermostClient>().sendMessage(any()) } answers {
            firstArg<MattermostMessageOut>().also { sentMessages.add(it) }.rootId == null
        }

        send(connector(mapOf("_reply_in_thread_" to "1")))

        assertEquals(listOf(postId, null), sentMessages.map { it.rootId })
    }

    @Test
    fun `answer is not sent again when Mattermost rejects a new post`() {
        every { anyConstructed<MattermostClient>().sendMessage(any()) } returns false

        send(connector())

        verify(exactly = 1) { anyConstructed<MattermostClient>().sendMessage(any()) }
    }

    @Test
    fun `thread root is read from the root_id of the post`() {
        val reply = mapper.readValue("""{"id":"$postId","root_id":"$threadRootId","message":"hi"}""", MattermostPost::class.java)
        val root = mapper.readValue("""{"id":"$threadRootId","root_id":"","message":"hi"}""", MattermostPost::class.java)

        assertEquals(threadRootId, reply.rootId)
        assertEquals("", root.rootId)
    }

    @Test
    fun `root_id is serialized only when set`() {
        val inThread = mapper.writeValueAsString(MattermostMessageOut("answer", rootId = postId))
        val newPost = mapper.writeValueAsString(MattermostMessageOut("answer"))

        assertTrue(inThread.contains("\"root_id\":\"$postId\""), inThread)
        assertFalse(newPost.contains("root_id"), newPost)
    }
}
