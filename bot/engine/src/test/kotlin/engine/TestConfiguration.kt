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

import ai.tock.bot.definition.BotDefinitionBase
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.SimpleStoryDefinition
import ai.tock.bot.definition.SimpleStoryHandlerBase
import ai.tock.bot.definition.SimpleStoryStep
import ai.tock.bot.definition.StoryDefinitionExtended
import ai.tock.bot.definition.StoryTag
import ai.tock.bot.definition.story
import ai.tock.bot.definition.storyWithSteps
import ai.tock.translator.UserInterfaceType
import ai.tock.translator.UserInterfaceType.voiceAssistant
import ai.tock.translator.raw

val secondaryIntent = Intent("secondary")
val enableStory = story("enable") {}
val disableStory = story("disable") {}

class BotDefinitionTest :
    BotDefinitionBase(
        "test",
        "namespace",
        stories = enumValues<TestStoryDefinition>().toList() + otherStory + testWithoutStep + builtInStories + disableBotTaggedStory,
        unknownStory = TestStoryDefinition.unknown,
        botEnabledStory = enableStory,
        botDisabledStory = disableStory
    )

enum class StepTest : SimpleStoryStep {
    s1,
    s2,
    s3,
    s4 {
        override val secondaryIntents: Set<IntentAware> = setOf(Intent("s4_secondary"))
    }
}

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
    override val unsupportedUserInterface: UserInterfaceType? = null,
    override val tags: Set<StoryTag> = emptySet()
) : StoryDefinitionExtended {

    test(StoryHandlerTest, secondaryIntents = setOf(secondaryIntent)),
    story_with_other_starter(StoryHandlerTest, setOf(secondaryIntent)),
    test2(StoryHandler2Test),
    voice_not_supported(StoryHandlerVoiceNotSupported, unsupportedUserInterface = voiceAssistant),
    withoutStep(StoryHandlerWithoutStep, stepsArray = emptyArray()),
    unknown(StoryHandlerUnknown),
    withAskAgainTag(StoryHandlerWithoutStep, stepsArray = emptyArray(), tags = setOf<StoryTag>(StoryTag.ASK_AGAIN));

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

val testWithoutStep = story("withoutStep") {
    end("withoutStep")
}

// stories in order to make BotDefinitionWrapperTest ok
val builtInStories = listOf(
    story("input_story") { end("input_story") },
    story("target") { end("target") }
)

val disableBotTaggedStory = SimpleStoryDefinition(
    id = "tagged_story",
    storyHandler = StoryHandlerTest,
    starterIntents = setOf(Intent("disable_bot")),
    tags = setOf(StoryTag.DISABLE)
)
