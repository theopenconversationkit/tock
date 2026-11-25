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

package ai.tock.bot.connector.whatsapp

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

class WhatsAppConnectorRoutesTest {
    private val controller: ConnectorController = mockk(relaxed = true)
    private val executor: Executor = mockk(relaxed = true)

    @BeforeEach
    fun inject() {
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
        body: String,
        filter: HttpRequestFilter = HttpRequestFilter.acceptAll,
    ) = runBlocking {
        WhatsAppConnector("whatsapp", "/whatsapp", "http://localhost", "login", "password", filter)
            .routes(controller, filter)
            .handle(HttpRequest(HttpMethod.POST, "/whatsapp", body = body.toByteArray()))
    }

    @Test
    fun `GIVEN messages WHEN they are posted THEN each one is handled by the executor, and the answer is an empty 200`() {
        val response = post("""{"messages":[{"from":"336","id":"1","timestamp":"1","type":"text","text":{"body":"a"}},{"from":"336","id":"2","timestamp":"2","type":"text","text":{"body":"b"}}]}""")

        assertEquals(200, response.status)
        assertEquals(0, response.body.size)
        verify(exactly = 2) { executor.executeBlocking(any<() -> Unit>()) }
    }

    @Test
    fun `GIVEN a refused request or an invalid body WHEN it is posted THEN the answers are 403 and 200`() {
        assertEquals(403, post("{}", { false }).status)
        assertEquals(200, post("not json").status)
    }
}
