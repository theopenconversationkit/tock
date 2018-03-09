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

package fr.vsct.tock.bot.definition


import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.BotDefinitionTest
import fr.vsct.tock.bot.engine.BotEngineTest
import fr.vsct.tock.bot.engine.StoryHandler2Test
import fr.vsct.tock.bot.engine.StoryHandlerUnknown
import fr.vsct.tock.bot.engine.StoryHandlerVoiceNotSupported
import fr.vsct.tock.bot.engine.TestStoryDefinition.test
import fr.vsct.tock.bot.engine.TestStoryDefinition.test2
import fr.vsct.tock.bot.engine.TockBotBus
import fr.vsct.tock.translator.UserInterfaceType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 *
 */
class StoryHandlerBaseTest : BotEngineTest() {

    val bus: BotBus by lazy {
        fillTimeline()
        TockBotBus(connectorController, userTimeline, dialog, userAction, connectorData, BotDefinitionTest())
    }

    @Test
    fun handleAndSwitchStory_shouldCreateANewStory_IfStoryHandlerFound() {
        assertEquals(test, bus.story.definition)
        StoryHandler2Test.handleAndSwitchStory(bus)
        assertEquals(test2, bus.story.definition)
    }

    @Test
    fun handle_shouldUseUnknownStoryHandler_IfNotSupportedInterface() {
        userAction.state.userInterface = UserInterfaceType.voiceAssistant
        StoryHandlerVoiceNotSupported.handle(bus)
        assertEquals(bus, StoryHandlerUnknown.registeredBus)
    }

    @Test
    fun handle_shouldUseNotUnknownStoryHandler_IfSupportedInterface() {
        userAction.state.userInterface = UserInterfaceType.textChat
        StoryHandlerVoiceNotSupported.handle(bus)
        assertNotEquals(bus, StoryHandlerUnknown.registeredBus)
    }

    @Test
    fun `i18nKeyFromLabel() does not throw exception WHEN mainIntentName is null and StoryHandlerBase implementation has no class name`() {
        val handler = object : SimpleStoryHandlerBase() {
            override fun action(bus: BotBus) {
            }
        }
        assertEquals("test", handler.i18nKeyFromLabel("test").toString())
    }


}