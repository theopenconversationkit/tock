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

package ai.tock.bot.connector.alcmeon

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.Executor
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.jackson.mapper
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
import kotlin.test.assertTrue

@Suppress("ktlint:standard:max-line-length")
class AlcmeonConnectorRoutesTest {
    private val controller: ConnectorController = mockk(relaxed = true)
    private val executor: Executor = mockk()
    private val task = slot<() -> Unit>()
    private val data = slot<ConnectorData>()
    private val body = "{\"backend\": \"whatsapp\", \"parameters\": [], \"user_external_id\": \"33671485945\", \"user_name\": \"Germain\", \"event\": {\"type\": \"text\", \"text\": {\"body\": \"Bonjour\"}}, \"global_variables\": {}}"
    private val signature = "4d737b25de54f0ec47bc9b5fee2b3baf7231733cabe7731830912bb9c0c548a9"

    @BeforeEach
    fun inject() {
        every { executor.executeBlocking(capture(task)) } answers { task.captured() }
        every { controller.handle(any(), capture(data)) } answers {
            val callback = data.captured.callback as AlcmeonCallback
            // the answer to the (hashed) user of the message
            val user = firstArg<SendSentence>().playerId
            callback.addAction(SendSentence(PlayerId("bot", PlayerType.bot), "alcmeon", user, "Hello").apply { metadata.lastAnswer = true }, 0)
            callback.sendResponseWithoutExit()
        }
        tockInternalInjector =
            KodeinInjector().apply {
                inject(Kodein { bind<Executor>() with provider { executor } })
            }
    }

    @AfterEach
    fun reset() {
        tockInternalInjector = KodeinInjector()
    }

    private fun handle(request: HttpRequest) =
        runBlocking {
            // created once the executor is injected
            val verifier = AlcmeonSignatureVerifier("1309768d3f6f8462")
            AlcmeonConnector("alcmeon", "/alcmeon", "the description", verifier).routes(controller, verifier).handle(request)
        }

    @Test
    fun `GIVEN the description route WHEN it is read THEN the description is answered`() {
        assertEquals("the description", String(handle(HttpRequest(HttpMethod.GET, "/alcmeon/description")).body))
    }

    @Test
    fun `GIVEN a signed message WHEN it is posted THEN the answer of the bot is the response`() {
        val response =
            handle(HttpRequest(HttpMethod.POST, "/alcmeon/start", headers = mapOf("X-Alcmeon-Webhook-Signature" to listOf(signature)), body = body.toByteArray()))

        assertEquals(200, response.status, String(response.body))
        assertEquals(listOf("application/json"), response.headers["Content-Type"])
        val json = mapper.readTree(response.body).toString()
        assertTrue(json.contains("\"Hello\""), json)
    }

    @Test
    fun `GIVEN an invalid signature WHEN a message is posted THEN the answer is a 403 and it is not handled`() {
        val response = handle(HttpRequest(HttpMethod.POST, "/alcmeon/handle-event", headers = mapOf("X-Alcmeon-Webhook-Signature" to listOf("wrong")), body = body.toByteArray()))

        assertEquals(403, response.status)
        assertEquals("Invalid Alcmeon webhook signature", String(response.body))
        verify(exactly = 0) { controller.handle(any(), any()) }
    }
}
