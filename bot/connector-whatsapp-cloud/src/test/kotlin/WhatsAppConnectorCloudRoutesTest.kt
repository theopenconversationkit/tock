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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.engine.ConnectorController
import ai.tock.shared.Executor
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.security.HttpRequestFilter
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WhatsAppConnectorCloudRoutesTest {
    private val controller: ConnectorController = mockk(relaxed = true)

    @BeforeEach
    fun inject() {
        tockInternalInjector =
            KodeinInjector().apply {
                inject(Kodein { bind<Executor>() with provider { mockk<Executor>(relaxed = true) } })
            }
    }

    @AfterEach
    fun reset() {
        tockInternalInjector = KodeinInjector()
    }

    private fun connector(filter: HttpRequestFilter = HttpRequestFilter.acceptAll) =
        WhatsAppConnectorCloudConnector(
            connectorId = "whatsapp",
            phoneNumberId = "phone",
            whatsAppBusinessAccountId = "account",
            metaApplicationId = null,
            path = "/whatsapp",
            verifyToken = "verify",
            requestFilter = filter,
            client = mockk(relaxed = true),
        )

    private fun handle(
        connector: WhatsAppConnectorCloudConnector,
        request: HttpRequest,
    ) = runBlocking { connector.routes(controller).handle(request) }

    private fun verification(
        token: String,
        challenge: String?,
    ) = HttpRequest(
        HttpMethod.GET,
        "/whatsapp",
        queryParameters =
            listOfNotNull(
                "hub.mode" to listOf("subscribe"),
                "hub.verify_token" to listOf(token),
                challenge?.let { "hub.challenge" to listOf(it) },
            ).toMap(),
    )

    @Test
    fun `GIVEN the right verify token WHEN Meta verifies the webhook THEN the challenge is sent back without content type`() {
        val response = handle(connector(), verification("verify", "1234"))

        assertEquals(200, response.status)
        assertEquals("1234", String(response.body))
        assertNull(response.headers["Content-Type"])
    }

    @Test
    fun `GIVEN a wrong verify token WHEN Meta verifies the webhook THEN the verification is refused with a 200`() {
        val response = handle(connector(), verification("wrong", "1234"))

        assertEquals(200, response.status)
        assertEquals("Invalid verify token", String(response.body))
    }

    @Test
    fun `GIVEN no challenge WHEN Meta verifies the webhook THEN the answer is a 500`() {
        assertEquals(500, handle(connector(), verification("verify", null)).status)
    }

    @Test
    fun `GIVEN a refused request WHEN a message is posted THEN the answer is a 403 and the controller is not called`() {
        val response = handle(connector { false }, HttpRequest(HttpMethod.POST, "/whatsapp", body = "{}".toByteArray()))

        assertEquals(403, response.status)
        verify(exactly = 0) { controller.handle(any(), any()) }
    }

    @Test
    fun `GIVEN an invalid body WHEN a message is posted THEN the error is logged and the answer is a 200`() {
        val response = handle(connector(), HttpRequest(HttpMethod.POST, "/whatsapp", body = "not json".toByteArray()))

        assertEquals(200, response.status)
    }

    @Test
    fun `GIVEN a refused request WHEN a template is posted THEN the answer is a 403`() {
        assertEquals(403, handle(connector { false }, HttpRequest(HttpMethod.POST, "/create_template")).status)
    }
}
