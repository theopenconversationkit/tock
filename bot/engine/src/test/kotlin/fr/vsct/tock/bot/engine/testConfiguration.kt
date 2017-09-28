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
import fr.vsct.tock.bot.definition.StoryDefinitionBase
import fr.vsct.tock.bot.definition.StoryHandlerBase
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.translator.UserInterfaceType

val testIntent = Intent("test")
val test2Intent = Intent("test2")
val voiceNotSupportedIntent = Intent("voiceNotSupported")
val unknownIntent = Intent("unknown")
val secondaryIntent = Intent("secondary")
val notInStoryIntent = Intent("not_in_story")

class BotDefinitionTest
    : BotDefinitionBase(
        "test",
        "namespace",
        stories = listOf(StoryDefinitionTest, StoryDefinition2Test, StoryDefinitionVoiceNotSupported, StoryDefinitionUnknown),
        unknownStory = StoryDefinitionUnknown
)

enum class StepTest : StoryStep { s1, s2, s3 }

abstract class AbstractStoryHandler : StoryHandlerBase() {
    var registeredBus: BotBus? = null

    override fun action(bus: BotBus) {
        registeredBus = bus
    }
}


object StoryDefinitionTest : StoryDefinitionBase(
        "storyDef1",
        StoryHandlerTest,
        StepTest.values(),
        setOf(testIntent),
        setOf(testIntent, secondaryIntent)) {
    val registeredBus: BotBus? get() = (storyHandler as StoryHandlerTest).registeredBus
}

object StoryHandlerTest : AbstractStoryHandler()

object StoryDefinition2Test : StoryDefinitionBase(
        "storyDef2",
        StoryHandler2Test,
        StepTest.values(),
        setOf(test2Intent),
        setOf(test2Intent)) {
}

object StoryHandler2Test : AbstractStoryHandler()

object StoryDefinitionVoiceNotSupported : StoryDefinitionBase(
        "storyVoiceNotSupported",
        StoryHandlerVoiceNotSupported,
        StepTest.values(),
        setOf(voiceNotSupportedIntent),
        setOf(voiceNotSupportedIntent),
        setOf(UserInterfaceType.voiceAssistant)) {
}

object StoryHandlerVoiceNotSupported : AbstractStoryHandler()

object StoryDefinitionUnknown : StoryDefinitionBase(
        "storyVoiceUnknown",
        StoryHandlerVoiceUnknown,
        StepTest.values(),
        setOf(unknownIntent),
        setOf(unknownIntent)) {
}

object StoryHandlerVoiceUnknown : AbstractStoryHandler()

