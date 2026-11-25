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

package ai.tock.bot.connector.messenger

import ai.tock.bot.engine.ConnectorController
import ai.tock.shared.Executor
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.binary.Hex
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MessengerConnectorRoutesTest {
    private val executor: Executor = mockk(relaxed = true)
    private val controller: ConnectorController = mockk(relaxed = true)
    private val client: MessengerClient = mockk { every { secretKey } returns "secret" }

    @BeforeEach
    fun inject() {
        tockInternalInjector = KodeinInjector().apply { inject(Kodein { bind<Executor>() with provider { executor } }) }
    }

    @AfterEach
    fun reset() {
        tockInternalInjector = KodeinInjector()
    }

    private fun connector() = MessengerConnector("routes", "app", "/messenger", "page", "appToken", "token", "verify", client, false)

    private fun handle(request: HttpRequest) = runBlocking { connector().routes(controller).handle(request) }

    private fun verification(
        token: String,
        challenge: String?,
    ) = HttpRequest(
        HttpMethod.GET,
        "/messenger",
        queryParameters =
            listOfNotNull("hub.verify_token" to listOf(token), challenge?.let { "hub.challenge" to listOf(it) }).toMap(),
    )

    private fun signature(body: String): String {
        val mac = Mac.getInstance("HmacSHA1").apply { init(SecretKeySpec("secret".toByteArray(), "HmacSHA1")) }
        return "sha1=" + String(Hex().encode(mac.doFinal(body.toByteArray())))
    }

    private fun event(
        body: String,
        signature: String?,
    ) = HttpRequest(
        HttpMethod.POST,
        "/messenger",
        headers = listOfNotNull(signature?.let { "X-Hub-Signature" to listOf(it) }).toMap(),
        body = body.toByteArray(),
    )

    @Test
    fun `GIVEN the right verify token WHEN Facebook verifies the webhook THEN the challenge is sent back without content type`() {
        val response = handle(verification("verify", "1234"))

        assertEquals(200, response.status)
        assertEquals("1234", String(response.body))
        assertNull(response.headers["Content-Type"])
    }

    @Test
    fun `GIVEN a wrong verify token WHEN Facebook verifies the webhook THEN the verification is refused with a 200`() {
        assertEquals("Invalid verify token", String(handle(verification("wrong", "1234")).body))
    }

    @Test
    fun `GIVEN no challenge WHEN Facebook verifies the webhook THEN the answer is a 500`() {
        assertEquals(500, handle(verification("verify", null)).status)
    }

    @Test
    fun `GIVEN a signed event WHEN it is posted THEN it is processed by the executor and the answer is a 200`() {
        val body = """{"object":"page","entry":[]}"""

        assertEquals(200, handle(event(body, signature(body))).status)
        verify(exactly = 1) { executor.executeBlocking(any<() -> Unit>()) }
    }

    @Test
    fun `GIVEN an event without a valid signature WHEN it is posted THEN it is ignored and the answer is a 200`() {
        val body = """{"object":"page","entry":[]}"""

        assertEquals(200, handle(event(body, "sha1=wrong")).status)
        assertEquals(200, handle(event(body, null)).status)
        verify(exactly = 0) { executor.executeBlocking(any<() -> Unit>()) }
    }
}
