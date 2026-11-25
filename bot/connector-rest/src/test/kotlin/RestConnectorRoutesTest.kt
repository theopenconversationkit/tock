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

package ai.tock.bot.connector.rest

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.rest.model.MessageRequest
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.HttpRequestFilter
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestConnectorRoutesTest {
    private val controller: ConnectorController = mockk(relaxed = true)
    private val data = slot<ConnectorData>()

    private fun post(
        filter: HttpRequestFilter = HttpRequestFilter.acceptAll,
        answer: Boolean = true,
    ) = runBlocking {
        every { controller.handle(any(), capture(data)) } answers {
            val action = firstArg<Action>()
            val callback = data.captured.callback as RestConnectorCallback
            if (answer) {
                callback.actions += SendSentence(PlayerId("bot", PlayerType.bot), "rest", action.playerId, "world")
                callback.eventAnswered(action)
            } else {
                callback.eventSkipped(action)
            }
        }
        val connector = RestConnector("rest", "/rest", filter)
        val body = mapper.writeValueAsBytes(MessageRequest("user", "bot", Sentence("hello"), ConnectorType.rest))
        connector.routes(controller, filter).handle(HttpRequest(HttpMethod.POST, "/rest/fr", body = body))
    }

    @Test
    fun `GIVEN a message WHEN the bot answers THEN the answer is the JSON of the bot messages`() {
        val response = post()

        assertEquals(200, response.status)
        val json = mapper.readTree(response.body)
        assertEquals("world", json["messages"][0]["text"].asText())
        assertEquals("fr", json["userLocale"].asText())
        assertTrue(json["hasNlpStats"].isBoolean)
    }

    @Test
    fun `GIVEN a message WHEN the bot has no answer THEN the answer is a 500`() {
        assertEquals(500, post(answer = false).status)
    }

    @Test
    fun `GIVEN a refused request WHEN it is posted THEN the answer is a 403`() {
        assertEquals(403, post(filter = { false }).status)
    }
}
