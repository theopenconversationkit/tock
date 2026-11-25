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

package ai.tock.bot.connector.iadvize

import ai.tock.bot.engine.ConnectorController
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigInteger
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.test.assertEquals

class IadvizeConnectorRoutesTest {
    private val controller: ConnectorController = mockk(relaxed = true)

    @BeforeEach
    fun inject() {
        tockInternalInjector = KodeinInjector().apply { inject(Kodein { import(sharedTestModule) }) }
        every { controller.botDefinition.botId } returns "bot"
        every { controller.botConfiguration.name } returns "the bot"
    }

    @AfterEach
    fun reset() {
        tockInternalInjector = KodeinInjector()
    }

    private fun signature(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec("secret".toByteArray(), "HmacSHA256")) }
        return String.format("sha256=%064x", BigInteger(1, mac.doFinal(payload.toByteArray())))
    }

    private fun handle(request: HttpRequest) =
        runBlocking {
            IadvizeConnector("iadvize", "/iadvize", "http://editor", "hello", null, "secret", "unavailable", null)
                .routes(controller)
                .handle(request)
        }

    @Test
    fun `GIVEN a GET request signed on its raw query string WHEN it is handled THEN the bots are answered as JSON`() {
        val query = "idConnectorVersion=v1&idWebsite=w%201"
        val response =
            handle(
                HttpRequest(
                    HttpMethod.GET,
                    "/iadvize/external-bots",
                    headers = mapOf("X-iAdvize-Signature" to listOf(signature(query))),
                    queryParameters = mapOf("idConnectorVersion" to listOf("v1"), "idWebsite" to listOf("w 1")),
                    rawQuery = query,
                ),
            )

        assertEquals(200, response.status, String(response.body))
        assertEquals(listOf("application/json"), response.headers["Content-Type"])
        assertEquals(
            "bot",
            ai.tock.shared.jackson.mapper
                .readTree(response.body)[0]["idBot"]
                .asText(),
        )
    }

    @Test
    fun `GIVEN an invalid signature WHEN a request is handled THEN the answer is a 400`() {
        val response =
            handle(
                HttpRequest(
                    HttpMethod.POST,
                    "/iadvize/conversations",
                    headers = mapOf("X-iAdvize-Signature" to listOf(signature("another body"))),
                    body = "{}".toByteArray(),
                ),
            )

        assertEquals(400, response.status)
    }

    @Test
    fun `GIVEN a failing handler WHEN a request is handled THEN the answer is a 500 without detail`() {
        val body = "not json"
        val response =
            handle(
                HttpRequest(
                    HttpMethod.POST,
                    "/iadvize/conversations",
                    headers = mapOf("X-iAdvize-Signature" to listOf(signature(body))),
                    body = body.toByteArray(),
                ),
            )

        assertEquals(500, response.status)
        assertEquals("Internal Server Error", String(response.body))
    }
}
