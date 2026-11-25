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

package ai.tock.shared.http

import ai.tock.shared.security.auth.spi.HttpWebSecurityContext
import ai.tock.shared.security.web.HttpWebSecurityCookies
import ai.tock.shared.security.web.HttpWebSecurityEncryptedCookies
import ai.tock.shared.security.web.cookie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HttpWebTest {
    private val context = HttpWebSecurityContext("web", "/io/app/web")

    @Test
    fun `GIVEN no cookie WHEN a user is authenticated THEN a user id is created in a secure session cookie on the public path`() {
        val result = HttpWebSecurityCookies().authenticate(HttpRequest(HttpMethod.POST, "/web"), context)

        val userId = checkNotNull(result.userId)
        assertEquals(listOf("tock_user_id=$userId; Path=/io/app/web; Secure; HTTPOnly; SameSite=None"), result.setCookies)
    }

    @Test
    fun `GIVEN a session cookie WHEN a user is authenticated THEN its id is kept and the cookie is not sent again`() {
        val request = HttpRequest(HttpMethod.POST, "/web", headers = mapOf("Cookie" to listOf("other=1; tock_user_id=abc")))

        val result = HttpWebSecurityCookies().authenticate(request, context)

        assertEquals("abc", result.userId)
        assertEquals(emptyList(), result.setCookies)
        assertEquals("1", request.cookie("other"))
    }

    @Test
    fun `GIVEN an encrypted cookie WHEN it is read back THEN the user id is the same, and a forged cookie is refused`() {
        val security = HttpWebSecurityEncryptedCookies()
        val first = security.authenticate(HttpRequest(HttpMethod.POST, "/web"), context)
        val cookie = first.setCookies.single()
        assertTrue(cookie.startsWith("__Http-tock_user_token=") && cookie.endsWith("SameSite=Lax"), cookie)

        val value = cookie.substringBefore(';')
        val again = security.authenticate(HttpRequest(HttpMethod.POST, "/web", headers = mapOf("Cookie" to listOf(value))), context)
        assertEquals(first.userId, again.userId)

        val forged = security.authenticate(HttpRequest(HttpMethod.POST, "/web", headers = mapOf("Cookie" to listOf("__Http-tock_user_token=forged"))), context)
        assertFalse(forged.userId == first.userId)
    }

    private val cors = HttpCors("https://.*\\.tock\\.ai", setOf(HttpMethod.POST), setOf("Content-Type"), allowCredentials = true)

    private fun cors(
        method: HttpMethod,
        headers: Map<String, String>,
    ) = runBlocking { cors.handle(HttpRequest(method, "/web", headers = headers.mapValues { listOf(it.value) })) { HttpResponse.text("ok") } }

    @Test
    fun `GIVEN CORS rules WHEN requests come from an allowed origin, another one or none THEN they are answered as Vert_x does`() {
        val preflight = cors(HttpMethod.OPTIONS, mapOf("Origin" to "https://www.tock.ai", "Access-Control-Request-Method" to "POST"))
        assertEquals(204, preflight.status)
        assertEquals(listOf("POST"), preflight.headers["Access-Control-Allow-Methods"])
        assertEquals(listOf("true"), preflight.headers["Access-Control-Allow-Credentials"])

        val allowed = cors(HttpMethod.POST, mapOf("Origin" to "https://www.tock.ai"))
        assertEquals("ok", String(allowed.body))
        assertEquals(listOf("https://www.tock.ai"), allowed.headers["Access-Control-Allow-Origin"])

        assertEquals(403, cors(HttpMethod.POST, mapOf("Origin" to "https://evil.com")).status)
        assertNull(cors(HttpMethod.POST, emptyMap()).headers["Access-Control-Allow-Origin"])
    }

    @Test
    fun `GIVEN a deferred response WHEN it is completed or streamed THEN the route answers it`() {
        runBlocking {
            val completed = DeferredHttpResponse()
            completed.complete(HttpResponse.text("done"))
            completed.complete(HttpResponse.text("ignored"))
            assertEquals("done", String(completed.await().body))

            val streamed = DeferredHttpResponse()
            streamed.startStream(prelude = "start;", keepAlive = "ping;", keepAliveDelay = Duration.ofMillis(200))
            val body = async { String(streamed.await().bufferedBody()) }
            streamed.write("a;")
            delay(500)
            streamed.write("b;")
            // a response completed after the start of the stream is its last chunk
            streamed.complete(HttpResponse.text("end"))
            val text = body.await()
            assertTrue(text.startsWith("start;a;ping;") && text.endsWith("b;end"), text)
        }
    }

    @Test
    fun `GIVEN a stream whose keep-alive delay expires as chunks arrive WHEN it is read THEN no chunk is lost`() {
        runBlocking {
            val streamed = DeferredHttpResponse()
            streamed.startStream(keepAlive = ".", keepAliveDelay = Duration.ofMillis(1))
            val body = async(Dispatchers.Default) { String(streamed.await().bufferedBody()) }
            val expected = (1..2000).map { "<$it>" }
            for ((index, chunk) in expected.withIndex()) {
                streamed.write(chunk)
                if (index % 10 == 0) delay(1)
            }
            streamed.close()
            val chunks = Regex("<\\d+>").findAll(body.await()).map { it.value }.toList()
            assertEquals(expected, chunks)
        }
    }

    @Test
    fun `GIVEN a started stream WHEN the response fails THEN the stream is aborted`() {
        runBlocking {
            val streamed = DeferredHttpResponse()
            streamed.startStream(prelude = "start;")
            streamed.write("a;")
            streamed.fail(IllegalStateException("bot failure"))
            assertFailsWith<IllegalStateException> { streamed.await().bufferedBody() }

            val notStarted = DeferredHttpResponse()
            notStarted.fail(IllegalStateException("bot failure"))
            assertEquals(500, notStarted.await().status)
        }
    }
}
