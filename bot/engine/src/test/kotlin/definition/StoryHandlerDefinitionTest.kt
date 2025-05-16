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

package ai.tock.bot.definition

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.bot.engine.message.MessagesList
import ai.tock.bot.engine.message.Sentence
import ai.tock.translator.raw
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class StoryHandlerDefinitionTest {

    @Test
    fun `answerWith returning a ConnectorMessage send a final message with this connector message`() {
        val bus = mockk<BotBus>(relaxed = true)
        val connectorType = ConnectorType("a")
        every { bus.targetConnectorType } returns connectorType
        every { bus.translate(any<CharSequence>()) } answers { (args[0] as CharSequence).raw }
        val storyDef = SimpleDef(bus)
        val message = mockk<ConnectorMessage>()
        val genericMessage = GenericMessage(connectorType, connectorMessage = message)
        every { message.connectorType } returns connectorType
        every { message.toConnectorMessage() } returns message
        every { message.toGenericMessage() } returns genericMessage

        storyDef.answerWith("default message") { message }

        verify { bus.end(MessagesList(Sentence(null, mutableListOf(genericMessage)))) }
    }

    @Test
    fun `answerWith returning null send a final message with the default message`() {
        val bus = mockk<BotBus>(relaxed = true)
        val connectorType = ConnectorType("a")
        every { bus.targetConnectorType } returns connectorType
        every { bus.translate(any<CharSequence>()) } answers { (args[0] as CharSequence).raw }
        val storyDef = SimpleDef(bus)

        storyDef.answerWith("default message") { storyDef.connector?.contextId }

        verify { bus.end(MessagesList(Sentence("default message"))) }
    }
}
