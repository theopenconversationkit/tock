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

package ai.tock.bot.engine

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotAnswerInterceptor
import ai.tock.bot.engine.BotRepository.registerBotAnswerInterceptor
import ai.tock.bot.engine.TestStoryDefinition.story_with_other_starter
import ai.tock.bot.engine.TestStoryDefinition.test
import ai.tock.bot.engine.TestStoryDefinition.test2
import ai.tock.bot.engine.TestStoryDefinition.withoutStep
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionPriority.urgent
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.translator.I18nLabelValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 *
 */
class BotBusTest : BotEngineTest() {

    @BeforeTest
    fun init() {
        BotRepository.botAnswerInterceptors.clear()
    }

    @Test
    fun withSignificance_hasToUpdateActionSignificance() {
        bus.withPriority(urgent).end()

        every { connector.loadProfile(any(), any()) } returns UserPreferences()
        every { connector.connectorType } returns ConnectorType("test")

        verify { connector.send(match { param -> param is Action && param.metadata.priority == urgent }, any(), any()) }
    }

    @Test
    fun step_shouldReturnsAStep_whenTheStepIsDefinedInTheStoryDefinition() {
        userAction = action(Choice("test", StepTest.s1))
        bot.handle(userAction, userTimeline, connectorController, connectorData)
        assertEquals(StepTest.s1, registeredBus!!.step)
    }

    @Test
    fun step_shouldReturnsNull_whenTheStepIsNotDefinedInTheStoryDefinition() {
        userAction = action(Choice("test", mapOf(SendChoice.STEP_PARAMETER to "not defined")))
        bot.handle(userAction, userTimeline, connectorController, connectorData)
        assertNull(registeredBus!!.step)
    }

    @Test
    fun reloadProfile_shouldRemoveFirstNameAndLastNamePreferencesValues_whenConnectorLoadProfileReturnsNull() {
        every { connector.loadProfile(any(), any()) } returns null
        bus.userPreferences.firstName = "firstName"
        bus.userPreferences.lastName = "lastName"
        bus.reloadProfile()
        assertNull(bus.userPreferences.firstName)
        assertNull(bus.userPreferences.lastName)
    }

    @Test
    fun `reloadProfile should use new user locale when connectorLoadProfile returns valid value`() {
        every { connector.loadProfile(any(), any()) } returns UserPreferences(locale = Locale.FRENCH)
        assertEquals(Locale.ENGLISH, bus.userLocale)
        bus.reloadProfile()
        assertEquals(Locale.FRENCH, bus.userLocale)
    }

    @Test
    fun `handleAndSwitchStory switch story and run the new handler`() {
        assertEquals(test, bus.story.definition)
        bus.handleAndSwitchStory(test2)
        assertEquals(test2, bus.story.definition)
        verify {
            connector.send(
                match<SendSentence> {
                    it.text.toString() == "StoryHandler2Test"
                },
                any()
            )
        }
    }

    @Test
    fun `switchStory switch story and keep the step if relevant`() {
        bus.switchStory(withoutStep)
        bus.step = StepTest.s1
        bus.switchStory(test2)
        assertEquals(test2, bus.story.definition)
        assertEquals(StepTest.s1, bus.step)
    }

    @Test
    fun `switchStory switch story and does not keep the step if not relevant`() {
        assertEquals(test, bus.story.definition)
        bus.step = StepTest.s1
        bus.switchStory(withoutStep)
        assertEquals(withoutStep, bus.story.definition)
        assertNull(bus.step)
        assertEquals(withoutStep, bus.dialog.stories.last().definition)
    }

    @Test
    fun `default delay is used WHEN multiple sentences are sent`() {
        val actionsList = mutableListOf<Long>()
        every { connector.send(any(), any(), capture(actionsList)) } returns Unit
        bus.send("test")
        bus.send("test2")
        bus.end("test3")
        assertEquals(listOf(0L, 1000L, 2000L), actionsList)
    }

    @Test
    fun `bus context value is automatically casted`() {
        bus.setBusContextValue("a", Locale.CANADA)
        val v: Locale? = bus.getBusContextValue("a")
        assertEquals(Locale.CANADA, v)
    }

    @Test
    fun `i18nKey returns a I18nLabelValue with the right key and category`() {
        val v = bus.i18nKey("a", "b")
        assertEquals(
            I18nLabelValue(
                "a",
                "app",
                "test",
                "b"
            ),
            v
        )
    }

    @Test
    fun `GIVEN no botAnswerInterceptor configured WHEN bot handle THEN connector send no altered message`() {
        bot.handle(userAction, userTimeline, connectorController, connectorData)
        verify {
            connector.send(
                match { param -> param is SendSentence && param.stringText == "StoryHandlerTest" },
                any(),
                any()
            )
        }
    }

    @Test
    fun `GIVEN botAnswerInterceptor configured WHEN bot handle THEN connector send a new message`() {
        registerBotAnswerInterceptor(SimpleBotAnswerInterceptor())
        bot.handle(userAction, userTimeline, connectorController, connectorData)
        verify {
            connector.send(
                match { param -> param is SendSentence && param.stringText == "new response" },
                any(),
                any()
            )
        }
    }

    class SimpleBotAnswerInterceptor : BotAnswerInterceptor {
        override fun handle(action: Action, bus: BotBus): Action {
            return Sentence("new response").toAction(PlayerId(""), "applicationId", PlayerId(""))
        }
    }

    @Test
    fun `send with custom messages is ok`() {
        val messageProvider1: ConnectorMessage = mockk()
        every { messageProvider1.connectorType } returns ConnectorType("1")
        val messageProvider2: ConnectorMessage = mockk()
        every { messageProvider2.connectorType } returns ConnectorType("2")
        bus.send {
            bus.withMessage(messageProvider1)
            bus.withMessage(messageProvider2)
        }

        verify { connector.send(any(), any(), any()) }
    }

    @Test
    fun `send with text is ok`() {
        val messageProvider1: ConnectorMessage = mockk()
        every { messageProvider1.connectorType } returns ConnectorType("1")
        val messageProvider2: ConnectorMessage = mockk()
        every { messageProvider2.connectorType } returns ConnectorType("2")
        bus.send {
            "message"
        }

        verify { connector.send(and(ofType<SendSentence>(), match { (it as SendSentence).stringText == "message" }), any(), any()) }
    }

    @Test
    fun `switchStory set the switch story key to true`() {
        bus.switchStory(test2)
        assertTrue(bus.hasCurrentSwitchStoryProcess)
    }

    @Test
    fun `handleAndSwitchStory remove the switch story key`() {
        bus.handleAndSwitchStory(test2)
        assertFalse(bus.hasCurrentSwitchStoryProcess)
    }

    @Test
    fun `switchStory set starterIntent to mainIntent in currentStory of dialog by default`() {
        bus.switchStory(story_with_other_starter)
        assertTrue(story_with_other_starter.wrap(bus.dialog.currentStory!!.starterIntent))
    }

    @Test
    fun `switchStory set starterIntent in currentStory of dialog if specified`() {
        bus.switchStory(story_with_other_starter, secondaryIntent)
        assertTrue(secondaryIntent.wrap(bus.dialog.currentStory!!.starterIntent))
    }

    @Test
    fun `handleAndSwitchStory set starterIntent to mainIntent in currentStory of dialog by default`() {
        bus.handleAndSwitchStory(story_with_other_starter)
        assertTrue(story_with_other_starter.wrap(bus.dialog.currentStory!!.starterIntent))
    }

    @Test
    fun `handleAndSwitchStory set starterIntent in currentStory of dialog if specified`() {
        bus.handleAndSwitchStory(story_with_other_starter, secondaryIntent)
        assertTrue(secondaryIntent.wrap(bus.dialog.currentStory!!.starterIntent))
    }

    @Test
    fun `switchStory set a new story only once`() {
        bus.switchStory(test2)
        assertEquals(test, bus.dialog.stories[0].definition)
        assertEquals(test2, bus.dialog.stories[1].definition)
        assertEquals(2, bus.dialog.stories.size)
    }

    @Test
    fun `GIVEN sendChoice with step WHEN switchStory THEN step of send choice is not forced`() {
        userAction = action(Choice("test", StepTest.s1))
        bus.step = StepTest.s2
        bus.handleAndSwitchStory(test2)
        assertEquals(test, bus.dialog.stories[0].definition)
        assertEquals(test2, bus.dialog.stories[1].definition)
        assertEquals(StepTest.s2, bus.step)
    }

    @Test
    fun `GIVEN all stories aren't metricStory WHEN getTrackedStoryId THEN the tracked story is the last one in dialog`() {
        bus.switchStory(test2)
        mockkObject(test)
        mockkObject(test2)
        every { test.metricStory } returns false
        every { test2.metricStory } returns false

        val trackedStoryId = bus.getTrackedStoryId()

        assertEquals(test2.id, trackedStoryId)
    }

    @Test
    fun `GIVEN only the first story isn't a metricStory WHEN getTrackedStoryId THEN the tracked story is the first one in dialog`() {
        bus.switchStory(test2)
        mockkObject(test)
        mockkObject(test2)
        every { test.metricStory } returns false
        every { test2.metricStory } returns true

        val trackedStoryId = bus.getTrackedStoryId()

        assertEquals(test.id, trackedStoryId)
    }

    @Test
    fun `GIVEN all stories are metricStory WHEN getTrackedStoryId THEN the tracked story is the last one in dialog`() {
        bus.switchStory(test2)
        mockkObject(test)
        mockkObject(test2)
        every { test.metricStory } returns true
        every { test2.metricStory } returns true

        val trackedStoryId = bus.getTrackedStoryId()

        assertEquals(bus.story.definition.id, trackedStoryId)
    }
}
