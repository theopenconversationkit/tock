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

import fr.vsct.tock.bot.definition.BotDefinitionBase
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.StoryDefinitionBase
import fr.vsct.tock.bot.definition.StoryHandlerBase
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.translator.UserInterfaceType
import fr.vsct.tock.translator.UserInterfaceType.voiceAssistant

val secondaryIntent = Intent("secondary")

class BotDefinitionTest
    : BotDefinitionBase(
        "test",
        "namespace",
        stories = enumValues<TestStoryDefinition>().toList(),
        unknownStory = TestStoryDefinition.unknown
)

enum class StepTest : StoryStep { s1, s2, s3 }

abstract class AbstractStoryHandler : StoryHandlerBase() {
    var registeredBus: BotBus? = null

    override fun action(bus: BotBus) {
        registeredBus = bus
    }
}

enum class TestStoryDefinition(
        override val storyHandler: AbstractStoryHandler,
        override val otherStarterIntents: Set<IntentAware> = emptySet(),
        override val otherIntents: Set<IntentAware> = emptySet(),
        override val stepsArray: Array<out StoryStep> = enumValues<StepTest>(),
        override val unsupportedUserInterface: UserInterfaceType? = null
) : StoryDefinitionBase {

    test(StoryHandlerTest, otherIntents = setOf(secondaryIntent)),
    test2(StoryHandler2Test),
    voice_not_supported(StoryHandlerVoiceNotSupported, unsupportedUserInterface = voiceAssistant),
    unknown(StoryHandlerUnknown);

    val registeredBus: BotBus? get() = storyHandler.registeredBus
}

object StoryHandlerTest : AbstractStoryHandler()

object StoryHandler2Test : AbstractStoryHandler()

object StoryHandlerVoiceNotSupported : AbstractStoryHandler()

object StoryHandlerUnknown : AbstractStoryHandler()

