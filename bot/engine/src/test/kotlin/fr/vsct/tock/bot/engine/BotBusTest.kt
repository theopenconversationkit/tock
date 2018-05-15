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
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionPriority
import fr.vsct.tock.bot.engine.action.ActionPriority.urgent
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.user.UserPreferences
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class BotBusTest : BotEngineTest() {

    val bus: BotBus by lazy {
        fillTimeline()
        TockBotBus(connectorController, userTimeline, dialog, userAction, connectorData, BotDefinitionTest())
    }

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
}