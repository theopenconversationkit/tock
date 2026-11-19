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

import ai.tock.bot.connector.mattermost.model.MattermostMessageOut
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MattermostClientTest {
    private val postId = "yxgx6rfhq3fafbsrffa8wguy4a"
    private val rootId = "pq9fbe1yxtgm5ysc4z9mc3xbne"
    private lateinit var server: MockWebServer

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun client(apiToken: String? = "api-token") = MattermostClient(server.url("/").toString(), "hook-token", apiToken)

    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    @Test
    fun `thread root of a reply is its root_id`() {
        server.enqueue(json("""{"id":"$postId","root_id":"$rootId","channel_id":"c"}"""))

        assertEquals(rootId, client().threadRootOf(postId))

        val request = server.takeRequest()
        assertEquals("/api/v4/posts/$postId", request.path)
        assertEquals("Bearer api-token", request.getHeader("Authorization"))
    }

    @Test
    fun `thread root of a root post is the post itself`() {
        server.enqueue(json("""{"id":"$postId","root_id":"","channel_id":"c"}"""))

        assertEquals(postId, client().threadRootOf(postId))
    }

    @Test
    fun `thread root is unknown when the post cannot be read`() {
        server.enqueue(MockResponse().setResponseCode(403))

        assertNull(client().threadRootOf(postId))
    }

    @Test
    fun `thread root is not requested without api token`() {
        assertNull(client(apiToken = null).threadRootOf(postId))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `sendMessage reports whether Mattermost accepted the message`() {
        server.enqueue(MockResponse().setBody("ok"))
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"id":"api.post.create_post.root_id.app_error"}"""))

        assertTrue(client().sendMessage(MattermostMessageOut("answer")))
        assertFalse(client().sendMessage(MattermostMessageOut("answer", rootId = postId)))
        assertEquals("/hooks/hook-token", server.takeRequest().path)
    }
}
