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

package ai.tock.bot.connector.mattermost

import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MattermostConnectorRoutesTest {
    private val controller: ConnectorController = mockk(relaxed = true)
    private val event = slot<Event>()

    private fun post(
        body: String,
        contentType: String,
    ) = runBlocking {
        every { controller.handle(capture(event), any()) } answers { }
        MattermostConnector("mattermost", "/mattermost", "http://localhost", "token", outgoingToken = "outgoing")
            .routes(controller)
            .handle(HttpRequest(HttpMethod.POST, "/mattermost", headers = mapOf("Content-Type" to listOf(contentType)), body = body.toByteArray()))
    }

    @Test
    fun `GIVEN a JSON message WHEN it is posted THEN it is handled and the answer is an empty 200`() {
        val response =
            post(
                """{"channel_id":"c","channel_name":"town","team_domain":"d","team_id":"t","text":"hello","token":"outgoing","user_id":"u","user_name":"julien"}""",
                "application/json",
            )

        assertEquals(200, response.status)
        assertEquals(0, response.body.size)
        assertEquals("hello", (event.captured as SendSentence).stringText)
    }

    @Test
    fun `GIVEN an URL encoded form WHEN it is posted THEN its attributes are decoded`() {
        post(
            "channel_id=c&channel_name=town&team_domain=d&team_id=t&text=tock+bonjour+%C3%A0+tous&trigger_word=tock&token=outgoing&user_id=u&user_name=julien&timestamp=12",
            "application/x-www-form-urlencoded",
        )

        assertEquals("bonjour à tous", (event.captured as SendSentence).stringText)
    }

    @Test
    fun `GIVEN an invalid token WHEN a message is posted THEN the answer is a 400 and it is not handled`() {
        val response =
            post(
                """{"channel_id":"c","channel_name":"town","team_domain":"d","team_id":"t","text":"hello","token":"wrong","user_id":"u","user_name":"julien"}""",
                "application/json",
            )

        assertEquals(400, response.status)
        verify(exactly = 0) { controller.handle(any(), any()) }
    }
}
