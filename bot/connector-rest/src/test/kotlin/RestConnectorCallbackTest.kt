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

import ai.tock.bot.connector.rest.model.MessageResponse
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import ai.tock.bot.engine.message.DebugMessage
import ai.tock.bot.engine.message.SentenceWithFootnotes
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.jackson.mapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RestConnectorCallbackTest {
    private val ragAnswer =
        SendSentenceWithFootnotes(
            playerId = PlayerId("bot", PlayerType.bot),
            applicationId = "app",
            recipientId = PlayerId("user"),
            text = "Technical error",
            ragDebug = mapOf("answer" to mapOf("status" to "technical_error"), "error" to mapOf("errorMessage" to "timeout")),
        )

    @Test
    fun `REST debug response preserves RAG errors stored on the answer action`() {
        val messages = ragAnswer.toRestMessages(true)
        assertIs<DebugMessage>(messages.first())
        assertIs<SentenceWithFootnotes>(messages.last())
        val json = mapper.readTree(mapper.writeValueAsString(MessageResponse(messages, "app", "user-action")))
        assertEquals("user-action", json["userActionId"].asText())
        assertEquals("debug", json["messages"][0]["eventType"].asText())
        assertEquals("RAG", json["messages"][0]["text"].asText())
        assertEquals("technical_error", json["messages"][0]["data"]["answer"]["status"].asText())
        assertEquals("timeout", json["messages"][0]["data"]["error"]["errorMessage"].asText())
    }

    @Test
    fun `REST response does not expose RAG debug unless requested`() {
        val messages = ragAnswer.toRestMessages(false)
        assertEquals(1, messages.size)
        assertIs<SentenceWithFootnotes>(messages.single())
    }
}
