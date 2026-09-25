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

package ai.tock.shared.vertx

import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.encryptAesGcm
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.vertx.core.http.Cookie
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class WebSecurityEncryptedCookiesHandlerTest {
    private var now = Instant.fromEpochSeconds(0)

    private val testClock =
        object : Clock {
            override fun now(): Instant = now
        }

    private val handler by lazy {
        object : WebSecurityEncryptedCookiesHandler(clock = testClock) {
            public override val cookieName: String get() = super.cookieName

            public override fun readCookie(context: RoutingContext) = super.readCookie(context)

            public override fun writeCookie(
                context: RoutingContext,
                cookieValue: String,
            ) = super.writeCookie(context, cookieValue)
        }
    }

    private val request: HttpServerRequest = mockk()
    private val response: HttpServerResponse = mockk { every { addCookie(any()) } returns this@mockk }
    val context: RoutingContext =
        mockk {
            every { request() } returns request
            every { response() } returns response
            every { get<String?>(any()) } returns null
        }

    private fun mockContext(cookie: Cookie? = null) {
        every { request.getCookie(any<String>()) } returns cookie
    }

    /**
     * Builds an encrypted cookie value the same way [WebSecurityEncryptedCookiesHandler.writeCookie] would,
     * using the real (private) [WebSecurityEncryptedCookiesHandler.SessionToken] serialization, to avoid
     * relying on assumptions about the JSON representation of [Instant].
     */
    private fun encryptedSessionToken(
        sub: String,
        exp: Instant?,
    ): String = encryptAesGcm(mapper.writeValueAsString(WebSecurityEncryptedCookiesHandler.SessionToken(sub, exp)))

    @Test
    fun `cookieName is prefixed to distinguish it from the plain cookie`() {
        assertEquals("__Http-tock_user_token", handler.cookieName)
    }

    @Test
    fun `readCookie returns null when no cookie is present`() {
        mockContext(cookie = null)

        assertNull(handler.readCookie(context))
    }

    @Test
    fun `readCookie returns null when the cookie value cannot be decrypted (tampered or invalid key)`() {
        val cookie: Cookie = mockk { every { value } returns "not-a-valid-encrypted-payload" }
        mockContext(cookie)

        assertNull(handler.readCookie(context))
    }

    @Test
    fun `readCookie returns null when the decrypted content is not a valid session token`() {
        val cookie: Cookie = mockk { every { value } returns encryptAesGcm("""{"not":"a session token"}""") }
        mockContext(cookie)

        assertNull(handler.readCookie(context))
    }

    @Test
    fun `readCookie returns null when the session is expired`() {
        now = Instant.fromEpochSeconds(1_000)
        val cookie: Cookie = mockk { every { value } returns encryptedSessionToken("user-1", now - 1.seconds) }
        mockContext(cookie)

        assertNull(handler.readCookie(context))
    }

    @Test
    fun `readCookie returns the user id when the session is still valid`() {
        now = Instant.fromEpochSeconds(1_000)
        val cookie: Cookie = mockk { every { value } returns encryptedSessionToken("user-2", now + 1.seconds) }
        mockContext(cookie)

        assertEquals("user-2", handler.readCookie(context))
    }

    @Test
    fun `readCookie returns the user id when the session cookie has no expiration`() {
        val cookie: Cookie = mockk { every { value } returns encryptedSessionToken("user-1", exp = null) }
        mockContext(cookie)

        assertEquals("user-1", handler.readCookie(context))
    }

    @Test
    fun `writeCookie stores an encrypted payload distinct from the plain user id, that can be read back`() {
        mockContext()
        val cookieSlot = slot<Cookie>()
        every { response.addCookie(capture(cookieSlot)) } returns response

        val writtenCookie = handler.writeCookie(context, "user-42")

        assertEquals("__Http-tock_user_token", writtenCookie.name)
        assertNotEquals("user-42", writtenCookie.value)

        // the written cookie value must be readable back to the original user id
        mockContext(cookie = mockk { every { value } returns writtenCookie.value })
        assertEquals("user-42", handler.readCookie(context))
    }

    @Test
    fun `writeCookie does not set an expiration when cookieAuthMaxAge is disabled (default)`() {
        System.clearProperty("tock_web_cookie_auth_max_age")
        mockContext()

        val writtenCookie = handler.writeCookie(context, "user-1")
        mockContext(cookie = mockk { every { value } returns writtenCookie.value })

        // no expiration was embedded: the session remains valid regardless of elapsed time
        now = Instant.fromEpochSeconds(10_000_000)
        assertEquals("user-1", handler.readCookie(context))
    }

    @Test
    fun `writeCookie sets an expiration derived from cookieAuthMaxAge when enabled`() {
        System.setProperty("tock_web_cookie_auth_max_age", "60")
        try {
            now = Instant.fromEpochSeconds(1_000)
            mockContext()

            val writtenCookie = handler.writeCookie(context, "user-1")
            mockContext(cookie = mockk { every { value } returns writtenCookie.value })

            // still valid just before expiration
            now += 59.seconds
            assertEquals("user-1", handler.readCookie(context))

            // expired just after
            now += 2.seconds
            assertNull(handler.readCookie(context))
        } finally {
            System.clearProperty("tock_web_cookie_auth_max_age")
        }
    }
}
