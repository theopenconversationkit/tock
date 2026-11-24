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
package ai.tock.bot.connector.slack

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
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SlackConnectorRoutesTest {
    private val controller: ConnectorController = mockk(relaxed = true)
    private val executor: Executor = mockk(relaxed = true)
    private val task = slot<() -> Unit>()

    @BeforeEach
    fun inject() {
        every { executor.executeBlocking(capture(task)) } answers { task.captured() }
        tockInternalInjector =
            KodeinInjector().apply {
                inject(Kodein { bind<Executor>() with provider { executor } })
            }
    }

    @AfterEach
    fun reset() {
        tockInternalInjector = KodeinInjector()
    }

    private fun post(body: String) =
        runBlocking {
            SlackConnector("slack", "/slack", "a", "b", "c", mockk(relaxed = true))
                .routes(controller)
                .handle(HttpRequest(HttpMethod.POST, "/slack", body = body.toByteArray()))
        }

    @Test
    fun `GIVEN an URL verification WHEN it is posted THEN the challenge is answered as text`() {
        val response = post("""{"type":"url_verification","token":"t","challenge":"the challenge"}""")

        assertEquals(200, response.status)
        assertEquals("the challenge", String(response.body))
        assertEquals(listOf("text/plain"), response.headers["Content-type"])
    }

    @Test
    fun `GIVEN a message WHEN it is posted THEN the answer is empty and the event is handled by the executor`() {
        val response =
            post(
                """{"token":"t","team_id":"T","api_app_id":"A","type":"event_callback","event_id":"E","event_time":1,
                   "event":{"type":"message","user":"U","text":"Hello","ts":"1.2","channel":"C","event_ts":"1.2","channel_type":"channel"}}""",
            )

        assertEquals(200, response.status)
        assertEquals(0, response.body.size)
        verify { controller.handle(any(), any()) }
    }

    @Test
    fun `GIVEN an invalid body WHEN it is posted THEN the answer is still a 200`() {
        assertEquals(200, post("not json").status)
        verify(exactly = 0) { controller.handle(any(), any()) }
    }
}
