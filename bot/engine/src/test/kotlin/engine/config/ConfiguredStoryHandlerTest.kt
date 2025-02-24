/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.SimpleAnswer
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.indicators.metric.MetricType
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep
import ai.tock.bot.admin.story.StoryDefinitionStepMetric
import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorFeature.CAROUSEL
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCardDescriptor
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotDefinitionTest
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.message.ActionWrappedMessage
import ai.tock.bot.engine.message.MessagesList
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.RawString
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId

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
            every { targetConnectorType } returns ConnectorType("a")
            every { botId } returns PlayerId("botId")
            every { userId } returns PlayerId("userId")
            every { connectorId } returns "appId"
            every { currentAnswerIndex } returns 1
            every { userTimeline } returns UserTimeline(PlayerId("userId"))
            every { botDefinition } returns BotDefinitionTest()
            every { step } returns mockk()
            every { dialog.lastAction } returns null
            every { dialog.stories } returns mutableListOf<Story>()
            every { translate(I18nLabelValue(originalLabel)) } returns RawString("translated label")
            every { translate(RawString("Step 1 not translated")) } returns RawString("Step 1 translated")
            every { underlyingConnector } returns mockk() {
                every { underlyingConnector.hasFeature(CAROUSEL, any()) } returns true
            }
            every {
                underlyingConnector.addSuggestions(
                    RawString("translated label"),
                    capture(suggestionsSlot)
                )
            } returns connectorMessageRetriever
            every { end(messages = capture(messagesSlot), initialDelay = any()) } returns mockk()
            every { story } returns mockk {
                every { definition } returns mockk {
                    every { steps } returns emptySet()
                }
            }
            every { createMetric(any(), any()) } returns mockk()
        }

        val configuration: StoryDefinitionConfiguration = mockk {
            every { mandatoryEntities } returns emptyList()
            every { findCurrentAnswer() } returns simpleAnswerConfiguration
            every { nextIntentsQualifiers } returns emptyList()
            every { findEnabledEndWithStoryId(any()) } returns null
            every { steps } returns emptyList()
            justRun { saveMetric(any()) }
        }

        val nextStepTranslated = listOf(RawString("Step 1 not translated"))
        every { configuration.findNextSteps(bus, configuration) } returns nextStepTranslated

        // When
        val handler = ConfiguredStoryHandler(BotDefinitionWrapper(BotDefinitionTest()), configuration)
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

    @Test
    fun `GIVEN simple answer configuration with 2 media messages WHEN sending answer THEN addSuggestion is called only once`() {

        // Given
        val originalLabel = I18nLabel(
            _id = "id".toId(),
            namespace = "namespace",
            category = "category",
            defaultLabel = "Not translated label",
            i18n = LinkedHashSet()
        )
        val media1 = MediaCardDescriptor(I18nLabelValue(originalLabel), null, null)
        val media2 = MediaCardDescriptor(I18nLabelValue(originalLabel), null, null)
        val simpleAnswerConfiguration = SimpleAnswerConfiguration(
            answers = listOf(
                SimpleAnswer(
                    key = I18nLabelValue(originalLabel),
                    delay = -1,
                    mediaMessage = media1
                ),
                SimpleAnswer(
                    key = I18nLabelValue(originalLabel),
                    delay = -1,
                    mediaMessage = media2
                )
            )
        )

        val messagesSlot = slot<MessagesList>()
        val suggestionsSlot = slot<List<CharSequence>>()
        val connectorMessageRetriever: BotBus.() -> ConnectorMessage? = {
            TestConnectorMessage(suggestionsSlot.captured)
        }

        val connector = mockk<Connector> {
            every {
                addSuggestions(
                    any<ConnectorMessage>(),
                    capture(suggestionsSlot)
                )
            } returns connectorMessageRetriever
            every { toConnectorMessage(any()) } returns { listOf(mockk()) }
            every { hasFeature(CAROUSEL, any()) } returns true
        }

        val bus: BotBus = mockk {
            every { targetConnectorType } returns ConnectorType("a")
            every { botId } returns PlayerId("botId")
            every { userId } returns PlayerId("userId")
            every { connectorId } returns "appId"
            every { currentAnswerIndex } returns 1
            every { userTimeline } returns UserTimeline(PlayerId("userId"))
            every { botDefinition } returns BotDefinitionTest()
            every { dialog.lastAction } returns null
            every { step } returns mockk()
            every { dialog.stories } returns mutableListOf<Story>()
            every { translate(any<I18nLabelValue>()) } returns RawString("translated label")
            every { translate(any<CharSequence>()) } answers { RawString(it.invocation.args[0].toString()) }
            every { underlyingConnector } returns connector

            every { end(messages = capture(messagesSlot), initialDelay = any()) } returns mockk()
            every { send(messages = capture(messagesSlot), initialDelay = any()) } returns mockk()
            every { story } returns mockk {
                every { definition } returns mockk {
                    every { steps } returns emptySet()
                }
            }
            every { createMetric(any(), any()) } returns mockk()
        }

        val configuration: StoryDefinitionConfiguration = mockk {
            every { mandatoryEntities } returns emptyList()
            every { findCurrentAnswer() } returns simpleAnswerConfiguration
            every { nextIntentsQualifiers } returns emptyList()
            every { findEnabledEndWithStoryId(any()) } returns null
            every { steps } returns emptyList()
            justRun { saveMetric(any()) }
        }

        val nextStepTranslated = listOf(RawString("Step 1 not translated"))
        every { configuration.findNextSteps(bus, configuration) } returns nextStepTranslated

        // When
        val handler = ConfiguredStoryHandler(BotDefinitionWrapper(BotDefinitionTest()), configuration)
        handler.handle(bus)

        // Then
        val capturedMessage = messagesSlot.captured.messages.first()
        assertTrue(capturedMessage is ActionWrappedMessage)
        val messageAction = capturedMessage.action
        assertTrue(messageAction is SendSentence)
        val connectorMessage = messageAction.messages.first()
        assertTrue(connectorMessage is TestConnectorMessage)

        verify(exactly = 1) { connector.addSuggestions(any<ConnectorMessage>(), any()) }
    }

    @Test
    fun `GIVEN simple answer configuration with consecutive media cards WHEN sending answer THEN merge consecutive cards to carousels`() {

        // Given card+text+card+card+card+text+text+card+card
        val label0Card = buildLabel("labelAnswer0")
        val label1Text = buildLabel("labelAnswer1")
        val label2Card = buildLabel("labelAnswer2")
        val label3Card = buildLabel("labelAnswer3")
        val label4Card = buildLabel("labelAnswer4")
        val label5Text = buildLabel("labelAnswer5")
        val label6Text = buildLabel("labelAnswer6")
        val label7Card = buildLabel("labelAnswer7")
        val label8Card = buildLabel("labelAnswer8")

        val simpleAnswerConfiguration = SimpleAnswerConfiguration(
            answers = listOf(
                SimpleAnswer(
                    key = I18nLabelValue(label0Card),
                    delay = -1,
                    mediaMessage = MediaCardDescriptor(I18nLabelValue(label0Card), null, null)
                ),
                SimpleAnswer(
                    key = I18nLabelValue(label1Text),
                    delay = -1
                ),
                SimpleAnswer(
                    key = I18nLabelValue(label2Card),
                    delay = -1,
                    mediaMessage = MediaCardDescriptor(
                        I18nLabelValue(label2Card),
                        null,
                        null,
                        fillCarousel = true
                    )
                ),
                SimpleAnswer(
                    key = I18nLabelValue(label3Card),
                    delay = -1,
                    mediaMessage = MediaCardDescriptor(
                        I18nLabelValue(label3Card),
                        null,
                        null,
                        fillCarousel = true
                    )
                ),
                SimpleAnswer(
                    key = I18nLabelValue(label4Card),
                    delay = -1,
                    mediaMessage = MediaCardDescriptor(
                        I18nLabelValue(label4Card),
                        null,
                        null,
                        fillCarousel = true
                    )
                ),
                SimpleAnswer(
                    key = I18nLabelValue(label5Text),
                    delay = -1
                ),
                SimpleAnswer(
                    key = I18nLabelValue(label6Text),
                    delay = -1
                ),
                SimpleAnswer(
                    key = I18nLabelValue(label7Card),
                    delay = -1,
                    mediaMessage = MediaCardDescriptor(
                        I18nLabelValue(label7Card),
                        null,
                        null,
                        fillCarousel = true
                    )
                ),
                SimpleAnswer(
                    key = I18nLabelValue(label8Card),
                    delay = -1,
                    mediaMessage = MediaCardDescriptor(
                        I18nLabelValue(label8Card),
                        null,
                        null,
                        fillCarousel = true
                    )
                )
            )
        )

        val capturedMessages = mutableListOf<MessagesList>()
        val capturedMediaMessages = mutableListOf<MediaMessage>()
        val connector = mockk<Connector> {
            every { toConnectorMessage(capture(capturedMediaMessages)) } returns { listOf(mockk()) }
            every { hasFeature(CAROUSEL, any()) } returns true
        }

        val bus: BotBus = mockk {
            every { targetConnectorType } returns ConnectorType("a")
            every { botId } returns PlayerId("botId")
            every { userId } returns PlayerId("userId")
            every { connectorId } returns "appId"
            every { currentAnswerIndex } returns 1
            every { userTimeline } returns UserTimeline(PlayerId("userId"))
            every { botDefinition } returns BotDefinitionTest()
            every { dialog.lastAction } returns null
            every { step } returns mockk()
            every { dialog.stories } returns mutableListOf<Story>()
            every { translate(any<I18nLabelValue>()) } answers { RawString(it.invocation.args[0].toString()) }
            every { translate(any<CharSequence>()) } answers { RawString(it.invocation.args[0].toString()) }
            every { underlyingConnector } returns connector

            every { end(messages = capture(capturedMessages), initialDelay = any()) } returns mockk()
            every { send(messages = capture(capturedMessages), initialDelay = any()) } returns mockk()
            every { story } returns mockk {
                every { definition } returns mockk {
                    every { steps } returns emptySet()
                }
            }
            every { createMetric(any(), any()) } returns mockk()
        }

        val configuration: StoryDefinitionConfiguration = mockk {
            every { mandatoryEntities } returns emptyList()
            every { findCurrentAnswer() } returns simpleAnswerConfiguration
            every { nextIntentsQualifiers } returns emptyList()
            every { findEnabledEndWithStoryId(any()) } returns null
            every { steps } returns emptyList()
            justRun { saveMetric(any()) }
        }

        every { configuration.findNextSteps(bus, configuration) } returns emptyList() // nextStepTranslated

        // When
        val handler = ConfiguredStoryHandler(BotDefinitionWrapper(BotDefinitionTest()), configuration)
        handler.handle(bus)

        // Then card+text+carousel(card+card+card)+text+text+carousel(card+card)
        // 5 answers
        assertEquals(capturedMessages.size, 6)
        // 3 simple text answers
        assertEquals(
            ((capturedMessages.get(1).messages.first() as ActionWrappedMessage).action as SendSentence).stringText,
            label1Text.defaultLabel
        )
        assertEquals(
            ((capturedMessages.get(3).messages.first() as ActionWrappedMessage).action as SendSentence).stringText,
            label5Text.defaultLabel
        )
        assertEquals(
            ((capturedMessages.get(4).messages.first() as ActionWrappedMessage).action as SendSentence).stringText,
            label6Text.defaultLabel
        )
        // 2 carousels
        assertEquals(capturedMediaMessages.size, 3)
        val capturedCard0 = capturedMediaMessages.get(0)
        assertTrue { capturedCard0 is MediaCard }
        assertEquals((capturedCard0 as MediaCard).title.toString(), label0Card.defaultLabel)
        val capturedCarousel234 = capturedMediaMessages.get(1)
        assertTrue { capturedCarousel234 is MediaCarousel }
        assertEquals((capturedCarousel234 as MediaCarousel).cards.size, 3)
        assertEquals(capturedCarousel234.cards.get(0).title.toString(), label2Card.defaultLabel)
        assertEquals(capturedCarousel234.cards.get(1).title.toString(), label3Card.defaultLabel)
        assertEquals(capturedCarousel234.cards.get(2).title.toString(), label4Card.defaultLabel)
        val capturedCarousel78 = capturedMediaMessages.get(2)
        assertTrue { capturedCarousel78 is MediaCarousel }
        assertEquals((capturedCarousel78 as MediaCarousel).cards.size, 2)
        assertEquals(capturedCarousel78.cards.get(0).title.toString(), label7Card.defaultLabel)
        assertEquals(capturedCarousel78.cards.get(1).title.toString(), label8Card.defaultLabel)
    }

    companion object {
        fun buildLabel(label: String) =
            I18nLabel(
                _id = label.toId(),
                category = "category",
                defaultLabel = "Default $label",
                i18n = LinkedHashSet()
            )
    }

    @Test
    fun `nextIntentQualifiers are taking in account`() {

        val bus: BotBus = mockk(relaxed = true){
            every { botDefinition } returns BotDefinitionTest()
            every { dialog } returns mockk{
                every { stories } returns mutableListOf()
                every { state } returns mockk(relaxed = true){
                    every{ nextActionState } returns mockk{
                        every {intentsQualifiers} returns listOf(NlpIntentQualifier("intent1",0.5), NlpIntentQualifier("intent2",0.5))
                    }
                }
            }
        }

        val configuration: StoryDefinitionConfiguration = mockk{
            every { mandatoryEntities } returns emptyList()
            every { findCurrentAnswer() } returns null
            every { steps } returns emptyList()
            every { findEnabledEndWithStoryId(any()) } returns null
            every { nextIntentsQualifiers } returns listOf(NlpIntentQualifier("intent1",0.5), NlpIntentQualifier("intent2",0.5))
            justRun { saveMetric(any()) }
        }

        val handler = ConfiguredStoryHandler(BotDefinitionWrapper(BotDefinitionTest()), configuration)
        handler.handle(bus)

        assertEquals(bus.dialog.state.nextActionState?.intentsQualifiers, listOf(NlpIntentQualifier("intent1",0.5), NlpIntentQualifier("intent2",0.5)))
    }

    @Test
    fun `GIVEN story without a step WHEN handled THEN save story handled metric`() {

        val capturedMetricTypes = mutableListOf<MetricType>()

        val connector = mockk<Connector> {
            every { toConnectorMessage(any()) } returns { listOf(mockk()) }
            every { hasFeature(any(), any()) } returns true
        }

        val originalLabel = I18nLabel(
            _id = "id".toId(),
            category = "category",
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

        val configuration: StoryDefinitionConfiguration = mockk {
            every { mandatoryEntities } returns emptyList()
            every { findCurrentAnswer() } returns simpleAnswerConfiguration
            every { nextIntentsQualifiers } returns emptyList()
            every { findEnabledEndWithStoryId(any()) } returns null
            every { steps } returns emptyList()
            justRun { saveMetric(any()) }
            justRun { saveMetrics(any()) }
        }

        val bus: BotBus = mockk {
            every { targetConnectorType } returns ConnectorType("a")
            every { botId } returns PlayerId("botId")
            every { userId } returns PlayerId("userId")
            every { userTimeline } returns UserTimeline(userId)
            every { connectorId } returns "appId"
            every { currentAnswerIndex } returns 1
            every { botDefinition } returns BotDefinitionTest()
            every { step } returns null
            every { dialog.stories } returns mutableListOf()
            every { dialog.lastAction } returns null
            every { translate(any()) } answers { RawString(it.invocation.args[0].toString()) }
            every { underlyingConnector } returns connector
            every { end(messages = any(), initialDelay = any()) } returns mockk()
            every { send(messages = any(), initialDelay = any()) } returns mockk()
            every { story } returns mockk {
                every { definition } returns mockk {
                    every { steps } returns emptySet()
                }
            }

            every { createMetric(capture(capturedMetricTypes), any(), any()) } returns mockk()
        }

        every { configuration.findNextSteps(bus, configuration) } returns emptyList()

        // When
        val handler = ConfiguredStoryHandler(BotDefinitionWrapper(BotDefinitionTest()), configuration)
        handler.handle(bus)

        // Then
        assertEquals(1, capturedMetricTypes.size)
        assertEquals(MetricType.STORY_HANDLED, capturedMetricTypes.first())

        verify(exactly = 1) { bus.createMetric(MetricType.STORY_HANDLED, null, null) }
        verify(exactly = 1) { configuration.saveMetric(any()) }
        verify(exactly = 0) { configuration.saveMetrics(any()) }
    }

    @Test
    fun `GIVEN story WHEN a step handled and has metrics THEN save only step metrics`() {

        val capturedMetricTypes = mutableListOf<MetricType>()
        val capturedIndicatorNames = mutableListOf<String>()
        val capturedIndicatorNameValues = mutableListOf<String>()

        val connector = mockk<Connector> {
            every { toConnectorMessage(any()) } returns { listOf(mockk()) }
            every { hasFeature(any(), any()) } returns true
        }

        val metric1 = StoryDefinitionStepMetric("indicator1", "value1")
        val metric2 = StoryDefinitionStepMetric("indicator2", "value2")

        val storyDefinitionConfigurationStep = StoryDefinitionConfigurationStep(
            name = "stepName",
            intent = null,
            targetIntent = null,
            answers = emptyList(),
            currentType = AnswerConfigurationType.simple,
            metrics = listOf(metric1, metric2)
        )

        val originalLabel = I18nLabel(
            _id = "id".toId(),
            category = "category",
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

        val configuration: StoryDefinitionConfiguration = mockk {
            every { mandatoryEntities } returns emptyList()
            every { findCurrentAnswer() } returns simpleAnswerConfiguration
            every { nextIntentsQualifiers } returns emptyList()
            every { findEnabledEndWithStoryId(any()) } returns null
            justRun { saveMetric(any()) }
            justRun { saveMetrics(any()) }
        }

        val bus: BotBus = mockk {
            every { targetConnectorType } returns ConnectorType("a")
            every { botId } returns PlayerId("botId")
            every { userId } returns PlayerId("userId")
            every { userTimeline } returns UserTimeline(userId)
            every { connectorId } returns "appId"
            every { currentAnswerIndex } returns 1
            every { botDefinition } returns BotDefinitionTest()
            every { step } returns storyDefinitionConfigurationStep.toStoryStep(configuration)
            every { dialog.stories } returns mutableListOf()
            every { dialog.lastAction } returns null
            every { translate(any()) } answers { RawString(it.invocation.args[0].toString()) }
            every { underlyingConnector } returns connector
            every { end(messages = any(), initialDelay = any()) } returns mockk()
            every { send(messages = any(), initialDelay = any()) } returns mockk()
            every { story } returns mockk {
                every { definition } returns mockk {
                    every { steps } returns emptySet()
                }
            }
            every { intent } returns null
            every { createMetric(capture(capturedMetricTypes), capture(capturedIndicatorNames), capture(capturedIndicatorNameValues)) } returns mockk()
        }

        every { configuration.findNextSteps(bus, configuration) } returns emptyList()

        // When
        val handler = ConfiguredStoryHandler(BotDefinitionWrapper(BotDefinitionTest()), configuration)
        handler.handle(bus)

        // Then
        assertEquals(2, capturedMetricTypes.size)
        assertEquals(2, capturedIndicatorNames.size)
        assertEquals(2, capturedIndicatorNameValues.size)
        assertEquals(MetricType.QUESTION_REPLIED, capturedMetricTypes.first())
        assertEquals(listOf(metric1.indicatorName, metric2.indicatorName), capturedIndicatorNames)
        assertEquals(listOf(metric1.indicatorValueName, metric2.indicatorValueName), capturedIndicatorNameValues)

        verify(exactly = 0) { configuration.saveMetric(any()) }
        verify(exactly = 1) { configuration.saveMetrics(any()) }

    }

}
