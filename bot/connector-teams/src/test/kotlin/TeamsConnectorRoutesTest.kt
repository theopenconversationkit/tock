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

package ai.tock.bot.connector.teams

import ai.tock.bot.engine.ConnectorController
import ai.tock.shared.Executor
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
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

class TeamsConnectorRoutesTest {
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

    private fun post(body: String) =
        runBlocking {
            TeamsConnector("teams", "/teams", "app", "password")
                .routes(controller)
                .handle(HttpRequest(HttpMethod.POST, "/teams", body = body.toByteArray()))
        }

    @Test
    fun `GIVEN an activity that is not a message WHEN it is posted THEN the answer is an empty 200`() {
        val response = post("""{"type":"typing"}""")

        assertEquals(200, response.status)
        assertEquals(0, response.body.size)
    }

    @Test
    fun `GIVEN a message without a valid token WHEN it is posted THEN the answer is a 403 and it is not handled`() {
        assertEquals(403, post("""{"type":"message","text":"hello","from":{"id":"user"},"serviceUrl":"https://smba"}""").status)
        verify(exactly = 0) { executor.executeBlocking(any<() -> Unit>()) }
    }

    @Test
    fun `GIVEN an invalid body WHEN it is posted THEN the answer is a 500`() {
        assertEquals(500, post("not json").status)
    }
}
