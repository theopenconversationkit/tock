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
import fr.vsct.tock.bot.engine.StoryDefinition2Test
import fr.vsct.tock.bot.engine.StoryDefinitionTest
import fr.vsct.tock.bot.engine.StoryHandler2Test
import fr.vsct.tock.bot.engine.StoryHandlerVoiceNotSupported
import fr.vsct.tock.bot.engine.StoryHandlerVoiceUnknown
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
        TockBotBus(connectorController, userTimeline, dialog, userAction, BotDefinitionTest())
    }

    @Test
    fun handleAndSwitchStory_shouldCreateANewStory_IfStoryHandlerFound() {
        assertEquals(StoryDefinitionTest, bus.story.definition)
        StoryHandler2Test.handleAndSwitchStory(bus)
        assertEquals(StoryDefinition2Test, bus.story.definition)
    }

    @Test
    fun handle_shouldUseUnknownStoryHandler_IfNotSupportedInterface() {
        userAction.state.userInterface = UserInterfaceType.voiceAssistant
        StoryHandlerVoiceNotSupported.handle(bus)
        assertEquals(bus, StoryHandlerVoiceUnknown.registeredBus)
    }

    @Test
    fun handle_shouldUseNotUnknownStoryHandler_IfSupportedInterface() {
        userAction.state.userInterface = UserInterfaceType.textChat
        StoryHandlerVoiceNotSupported.handle(bus)
        assertNotEquals(bus, StoryHandlerVoiceUnknown.registeredBus)
    }


}