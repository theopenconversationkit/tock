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

package ai.tock.bot.connector.googlechat

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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GoogleChatConnectorRoutesTest {
    private val controller: ConnectorController = mockk(relaxed = true)
    private val executor: Executor = mockk(relaxed = true)
    private val verifier: GoogleChatTokenVerifier = mockk()

    @BeforeEach
    fun inject() {
        every { verifier.verify(any()) } returns VerificationFailure.NO_BEARER_AUTHORISATION
        every { verifier.verify("Bearer valid") } returns null
        tockInternalInjector =
            KodeinInjector().apply {
                inject(Kodein { bind<Executor>() with provider { executor } })
            }
    }

    @AfterEach
    fun reset() {
        tockInternalInjector = KodeinInjector()
    }

    private fun post(
        authorization: String?,
        body: String,
    ) = runBlocking {
        GoogleChatConnector("chat", "/chat", mockk(relaxed = true), verifier, false, true, sourcesLabel = "Sources", waitingMessage = "...")
            .routes(controller)
            .handle(HttpRequest(HttpMethod.POST, "/chat", headers = listOfNotNull(authorization?.let { "Authorization" to listOf(it) }).toMap(), body = body.toByteArray()))
    }

    @Test
    fun `GIVEN no valid token WHEN an event is posted THEN the answer is a 403 and nothing is handled`() {
        assertEquals(403, post(null, "{}").status)
        assertEquals(403, post("Bearer invalid", "{}").status)
        verify(exactly = 0) { executor.executeBlocking(any<() -> Unit>()) }
    }

    @Test
    fun `GIVEN a valid token WHEN an unsupported or invalid event is posted THEN the answer is the empty JSON object`() {
        val response = post("Bearer valid", """{"chat":{}}""")

        assertEquals(200, response.status)
        assertEquals("{}", String(response.body))
        assertEquals(listOf("application/json; charset=UTF-8"), response.headers["Content-Type"])
        assertEquals(200, post("Bearer valid", "not json").status)
    }
}
