/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.TestStoryDefinition.test
import fr.vsct.tock.bot.engine.TestStoryDefinition.test2
import fr.vsct.tock.bot.engine.TestStoryDefinition.withoutStep
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionPriority
import fr.vsct.tock.bot.engine.action.ActionPriority.urgent
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.translator.I18nLabelValue
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class BotBusTest : BotEngineTest() {

    @Test
    fun withSignificance_hasToUpdateActionSignificance() {
        bus.withPriority(ActionPriority.urgent).end()

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
    }

    @Test
    fun `default delay is used WHEN multiple setneces are sent`() {
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
}