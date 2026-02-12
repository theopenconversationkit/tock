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

import ai.tock.bot.connector.web.WebMarkdown
import ai.tock.bot.connector.web.WebMessage
import ai.tock.bot.connector.web.WebMessageProcessor
import ai.tock.bot.connector.web.send.QuickReply
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WebMessageProcessorTest {
    private val markdownText = "Hello *user*"
    private val renderedText = WebMarkdown.markdown(markdownText)

    @Test
    fun `web messages do not get processed by default`() {
        val playerId = PlayerId("")
        val action = SendSentence(playerId, "", playerId, markdownText)
        val messageProcessor = WebMessageProcessor(false)
        assertEquals(WebMessage(markdownText, actionId = action.id.toString()), messageProcessor.process(action))
    }

    @Test
    fun `simple web messages get rendered as HTML when markdown is enabled`() {
        val playerId = PlayerId("")
        val action = SendSentence(playerId, "", playerId, markdownText)
        val messageProcessor = WebMessageProcessor(true)
        assertEquals(WebMessage(renderedText, actionId = action.id.toString()), messageProcessor.process(action))
    }

    @Test
    fun `web messages' bodies get rendered as HTML when markdown is enabled`() {
        val playerId = PlayerId("")
        val quickReplies = listOf(QuickReply("QR", null, null))
        val action = SendSentence(playerId, "", playerId, null, mutableListOf(WebMessage(markdownText, quickReplies)))
        val messageProcessor = WebMessageProcessor(true)
        assertEquals(WebMessage(renderedText, quickReplies, actionId = action.id.toString()), messageProcessor.process(action))
    }
}
