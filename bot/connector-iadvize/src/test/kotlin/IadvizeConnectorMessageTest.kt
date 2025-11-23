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

import ai.tock.bot.connector.iadvize.model.payload.TextPayload
import ai.tock.bot.connector.iadvize.model.response.conversation.Duration
import ai.tock.bot.connector.iadvize.model.response.conversation.QuickReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeAwait
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeClose
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMessage
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMultipartReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeTransfer
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericMessage
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IadvizeConnectorMessageTest {
    private val hi = "Hi !"
    private val question = "You're okay ?"
    private val yesAndNo = listOf("Yes", "No")
    private val error = "Error !"
    private val await = IadvizeAwait(Duration(7, minutes))
    private val transfer =
        IadvizeTransfer(
            distributionRule = "distributionRule",
            transferOptions = IadvizeTransfer.TransferOptions(Duration(2, seconds)),
        )
    private val close = IadvizeClose()
    private val message = IadvizeMessage(TextPayload(hi))
    private val messageError = IadvizeMessage(TextPayload(error))
    private val messageWithQuickReplies =
        IadvizeMessage(
            TextPayload(question),
            yesAndNo.map { QuickReply(it) }.toMutableList(),
        )
    private val multipartReply =
        IadvizeMultipartReply(
            listOf(await, messageError, transfer, message, close),
        )

    @Test
    fun `convert await to generic message`() {
        val message = IadvizeConnectorMessage(await)
        assertNull(message.toGenericMessage())
    }

    @Test
    fun `convert transfer to generic message`() {
        val message = IadvizeConnectorMessage(transfer)
        assertNull(message.toGenericMessage())
    }

    @Test
    fun `convert close to generic message`() {
        val message = IadvizeConnectorMessage(close)
        assertNull(message.toGenericMessage())
    }

    @Test
    fun `convert message to generic message`() {
        val message = IadvizeConnectorMessage(message)
        val e = message.toGenericMessage()
        assertEquals(
            GenericMessage(
                iadvizeConnectorType,
                texts =
                    mapOf(
                        "text" to hi,
                    ),
            ),
            e,
        )
    }

    @Test
    fun `convert message with quick replies to generic message`() {
        val message = IadvizeConnectorMessage(messageWithQuickReplies)
        val e = message.toGenericMessage()
        assertEquals(
            GenericMessage(
                iadvizeConnectorType,
                texts =
                    mapOf(
                        "text" to question,
                    ),
                choices = yesAndNo.map { Choice.fromText(it) },
            ),
            e,
        )
    }

    @Test
    fun `convert multipart reply to generic message`() {
        val message = IadvizeConnectorMessage(multipartReply)
        val e = message.toGenericMessage()
        assertEquals(
            GenericMessage(
                iadvizeConnectorType,
                texts =
                    mapOf(
                        "text" to error,
                    ),
            ),
            e,
        )
    }
}
