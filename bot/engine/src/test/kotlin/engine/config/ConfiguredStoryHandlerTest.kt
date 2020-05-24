/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.answer.SimpleAnswer
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotDefinitionTest
import ai.tock.bot.engine.StepTest
import ai.tock.bot.engine.TestStoryDefinition
import ai.tock.bot.engine.TestStoryDefinition.test
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.message.ActionWrappedMessage
import ai.tock.bot.engine.message.MessagesList
import ai.tock.bot.engine.user.PlayerId
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.RawString
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfiguredStoryHandlerTest {

    data class TestConnectorMessage(
        val suggestions: List<CharSequence>,
        override val connectorType: ConnectorType = ConnectorType.none
    ) : ConnectorMessage

    @Test
    fun `GIVEN simple answer configuration WHEN sending answer THEN translate suggestions`() {

        // Given
        val originalLabel = I18nLabel(
            _id = "id".toId(),
            namespace = "namespace",
            category = "category",
            defaultLabel = "Not translated label",
            i18n = LinkedHashSet()
        )
        val simpleAnswerConfiguration = SimpleAnswerConfiguration(
            answers = listOf(
                SimpleAnswer(
                    key = I18nLabelValue(originalLabel),
                    delay = -1
                )
            )
        )

        val messagesSlot = slot<MessagesList>()
        val suggestionsSlot = slot<List<CharSequence>>()
        val connectorMessageRetriever: BotBus.() -> ConnectorMessage? = {
            TestConnectorMessage(suggestionsSlot.captured)
        }

        val bus: BotBus = mockk {
            every { botId } returns PlayerId("botId")
            every { userId } returns PlayerId("userId")
            every { applicationId } returns "appId"
            every { currentAnswerIndex } returns 1
            every { botDefinition } returns BotDefinitionTest()
            every { step } returns mockk()
            every { translate(I18nLabelValue(originalLabel)) } returns RawString("translated label")
            every { translate(RawString("Step 1 not translated")) } returns RawString("Step 1 translated")
            every {
                underlyingConnector.addSuggestions(
                    RawString("translated label"),
                    capture(suggestionsSlot)
                )
            } returns connectorMessageRetriever
            every { end(messages = capture(messagesSlot), initialDelay = any()) } returns mockk()
        }

        val configuration: StoryDefinitionConfiguration = mockk {
            every { findEnabledFeature("appId") } returns null
            every { mandatoryEntities } returns emptyList()
            every { findCurrentAnswer() } returns simpleAnswerConfiguration

        }

        val nextStepTranslated = listOf(RawString("Step 1 not translated"))
        every { configuration.findNextSteps(bus, configuration) } returns nextStepTranslated

        // When
        val handler = ConfiguredStoryHandler(configuration)
        handler.handle(bus)

        // Then
        val capturedMessage = messagesSlot.captured.messages.first()
        assertTrue(capturedMessage is ActionWrappedMessage)
        val messageAction = capturedMessage.action
        assertTrue(messageAction is SendSentence)
        val connectorMessage = messageAction.messages.first()
        assertTrue(connectorMessage is TestConnectorMessage)

        assertEquals("Step 1 translated", connectorMessage.suggestions.first().toString())
    }
}