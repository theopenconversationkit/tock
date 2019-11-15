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

package ai.tock.bot.engine

import ai.tock.bot.definition.BotDefinitionBase
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.SimpleStoryHandlerBase
import ai.tock.bot.definition.SimpleStoryStep
import ai.tock.bot.definition.StoryDefinitionExtended
import ai.tock.bot.definition.storyWithSteps
import ai.tock.translator.UserInterfaceType
import ai.tock.translator.UserInterfaceType.voiceAssistant
import ai.tock.translator.raw

val secondaryIntent = Intent("secondary")

class BotDefinitionTest
    : BotDefinitionBase(
        "test",
        "namespace",
        stories = enumValues<TestStoryDefinition>().toList() + otherStory,
        unknownStory = TestStoryDefinition.unknown
)

enum class StepTest : SimpleStoryStep { s1, s2, s3 }

abstract class AbstractStoryHandler : SimpleStoryHandlerBase() {
    var registeredBus: BotBus? = null

    override fun action(bus: BotBus) {
        registeredBus = bus
        bus.end(this::class.simpleName!!.raw)
    }
}

enum class TestStoryDefinition(
        override val storyHandler: AbstractStoryHandler,
        override val otherStarterIntents: Set<IntentAware> = emptySet(),
        override val secondaryIntents: Set<IntentAware> = emptySet(),
        override val stepsArray: Array<StepTest> = enumValues(),
        override val unsupportedUserInterface: UserInterfaceType? = null
) : StoryDefinitionExtended {

    test(StoryHandlerTest, secondaryIntents = setOf(secondaryIntent)),
    test2(StoryHandler2Test),
    voice_not_supported(StoryHandlerVoiceNotSupported, unsupportedUserInterface = voiceAssistant),
    withoutStep(StoryHandlerWithoutStep, stepsArray = emptyArray()),
    unknown(StoryHandlerUnknown);

    val registeredBus: BotBus? get() = storyHandler.registeredBus
}

object StoryHandlerTest : AbstractStoryHandler()

object StoryHandler2Test : AbstractStoryHandler()

object StoryHandlerVoiceNotSupported : AbstractStoryHandler()

object StoryHandlerWithoutStep : AbstractStoryHandler()

object StoryHandlerUnknown : AbstractStoryHandler()

val otherStory = storyWithSteps<StepTest>("other") {
    end("other")
}
