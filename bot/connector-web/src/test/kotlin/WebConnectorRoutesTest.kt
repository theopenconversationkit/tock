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

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.http.DeferredHttpResponse
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.web.HttpWebSecurityCookies
import ai.tock.shared.security.web.HttpWebSecurityPassthrough
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WebConnectorRoutesTest {
    private val controller: ConnectorController = mockk(relaxed = true)
    private val event = slot<Event>()
    private val data = slot<ConnectorData>()

    private fun connector(cookies: Boolean): WebConnector {
        coEvery { controller.handleUserEvent(capture(event), capture(data)) } coAnswers {
            val callback = data.captured.callback as WebConnectorCallback
            val user = (event.captured as Action).playerId
            callback.addAction(SendSentence(PlayerId("bot", PlayerType.bot), "web", user, "hello ${user.id}"))
            callback.sendResponse()
        }
        val security = if (cookies) HttpWebSecurityCookies() else HttpWebSecurityPassthrough
        return WebConnector("web", "/web", WebConnectorSecurity.Http(security), "/public/web")
    }

    private fun post(
        cookies: Boolean,
        headers: Map<String, String> = emptyMap(),
    ) = runBlocking {
        connector(cookies)
            .routes(controller, if (cookies) HttpWebSecurityCookies() else HttpWebSecurityPassthrough)
            .handle(
                HttpRequest(
                    HttpMethod.POST,
                    "/web",
                    headers = headers.mapValues { listOf(it.value) },
                    body = """{"query":"hi","userId":"from_body","locale":"fr"}""".toByteArray(),
                ),
            )
    }

    @Test
    fun `GIVEN the passthrough security WHEN a message is posted THEN the bot answers the user of the body in JSON`() {
        val response = post(cookies = false)

        assertEquals(200, response.status, String(response.body))
        assertEquals(listOf("application/json"), response.headers["Content-Type"])
        assertEquals("hello from_body", mapper.readTree(response.body)["responses"][0]["text"].asText())
    }

    @Test
    fun `GIVEN the cookies security WHEN a message is posted without cookie THEN the user of the new cookie replaces the one of the body`() {
        val response = post(cookies = true)

        val cookie = response.headers["Set-Cookie"].orEmpty().single()
        val userId = cookie.substringAfter("tock_user_id=").substringBefore(';')
        assertTrue(cookie.contains("Path=/public/web"), cookie)
        assertEquals("hello $userId", mapper.readTree(response.body)["responses"][0]["text"].asText())
    }

    @Test
    fun `GIVEN the CORS rules WHEN a message comes from an origin THEN it gets the CORS headers, with the credentials of the cookies`() {
        val response = post(cookies = true, headers = mapOf("Origin" to "https://www.tock.ai"))

        assertEquals(listOf("https://www.tock.ai"), response.headers["Access-Control-Allow-Origin"])
        assertEquals(listOf("true"), response.headers["Access-Control-Allow-Credentials"])
    }

    @Test
    fun `GIVEN a streamed answer WHEN the bot sends events then the final answer THEN they are server-sent events, and the JSON is the last chunk`() {
        runBlocking {
            val deferred = DeferredHttpResponse()
            val responder = HttpWebResponder(deferred)
            val body = async { String(deferred.await().bufferedBody()) }

            responder.sse("""{"a":1}""")
            responder.json("""{"end":true}""")

            assertEquals("event: message\ndata: {\"a\":1}\n\n{\"end\":true}", body.await())
            assertEquals(listOf("text/event-stream;charset=UTF-8"), deferred.await().headers["Content-Type"])
        }
    }
}
