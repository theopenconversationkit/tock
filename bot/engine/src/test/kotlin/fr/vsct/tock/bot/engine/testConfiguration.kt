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

val testIntent = Intent("test")

class BotDefinitionTest
    : BotDefinitionBase(
        "test",
        "namespace",
        stories = listOf(StoryDefinitionTest())
)

enum class StepTest : StoryStep {s1, s2, s3 }

class StoryDefinitionTest : StoryDefinitionBase(
        "storyDef1",
        StoryHandlerTest(),
        StepTest.values(),
        setOf(testIntent)) {
    val registeredBus: BotBus? get() = (storyHandler as StoryHandlerTest).registeredBus
}

class StoryHandlerTest : StoryHandlerBase() {

    var registeredBus: BotBus? = null

    override fun action(bus: BotBus) {
        registeredBus = bus
    }
}

