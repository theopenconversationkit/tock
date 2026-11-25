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

package ai.tock.bot.connector.openai

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.security.web.HttpWebSecurityCookies
import ai.tock.shared.security.web.HttpWebSecurityPassthrough
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenAIConnectorRoutesTest {
    private val controller: ConnectorController = mockk(relaxed = true)
    private val event = slot<Event>()
    private val data = slot<ConnectorData>()

    private fun handle(
        request: HttpRequest,
        cookies: Boolean = false,
    ) = runBlocking {
        coEvery { controller.handleUserEvent(capture(event), capture(data)) } coAnswers {
            val callback = data.captured.callback as OpenAIConnectorCallback
            val user = (event.captured as Action).playerId
            listOf("Hello", " world").forEachIndexed { index, text ->
                val action = SendSentence(PlayerId("bot", PlayerType.bot), "openai", user, text).apply { metadata.lastAnswer = index == 1 }
                if (callback.streamedResponse) callback.sendStreamedResponse(action) else callback.addAction(action)
            }
            if (!callback.streamedResponse) callback.sendResponse()
        }
        val security = if (cookies) HttpWebSecurityCookies() else HttpWebSecurityPassthrough
        val response = OpenAIConnector("openai", "/openai", OpenAIConnectorSecurity.Http(security)).routes(controller, security).handle(request)
        response to String(response.bufferedBody())
    }

    private fun completion(stream: Boolean) =
        HttpRequest(
            HttpMethod.POST,
            "/openai/chat/completions",
            headers = mapOf("X-OpenWebUI-Chat-Id" to listOf("chat")),
            body = """{"model":"tock","stream":$stream,"messages":[{"role":"user","content":"hi"}]}""".toByteArray(),
        )

    @Test
    fun `GIVEN a streamed chat completion WHEN the bot answers THEN the answers are server-sent chunks, ended by DONE`() {
        val (response, body) = handle(completion(stream = true))

        assertEquals(listOf("text/event-stream;charset=UTF-8"), response.headers["Content-Type"])
        assertTrue(body.startsWith("event: ping\ndata: 1\n\n"), body)
        assertTrue(body.contains("Hello") && body.contains(" world"), body)
        assertTrue(body.endsWith("data: [DONE]\n\n"), body)
    }

    @Test
    fun `GIVEN an unstreamed chat completion WHEN it is not supported THEN the answer is an empty 200`() {
        val (response, body) = handle(completion(stream = false))

        assertEquals(200, response.status)
        assertEquals("", body)
    }

    @Test
    fun `GIVEN the models route and the cookies security WHEN it is read THEN the model is answered with the cookie`() {
        val (response, body) = handle(HttpRequest(HttpMethod.GET, "/openai/models"), cookies = true)

        assertTrue(body.contains("tock"), body)
        assertTrue(
            response.headers["Set-Cookie"]
                .orEmpty()
                .single()
                .startsWith("tock_user_id="),
        )
    }

    @Test
    fun `GIVEN a request from an origin WHEN a route is read THEN the answer has the CORS headers`() {
        val (response, _) = handle(HttpRequest(HttpMethod.GET, "/openai/health", headers = mapOf("Origin" to listOf("https://www.tock.ai"))))

        assertEquals(listOf("https://www.tock.ai"), response.headers["Access-Control-Allow-Origin"])
    }
}
