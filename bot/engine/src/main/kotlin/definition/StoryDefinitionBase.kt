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

package ai.tock.bot.definition

import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.engine.BotBus
import ai.tock.translator.UserInterfaceType

/**
 * Default [StoryDefinition] implementation.
 */
open class StoryDefinitionBase(
        val name: String,
        override val storyHandler: StoryHandler = {} as SimpleStoryHandlerBase,
        otherStarterIntents: Set<IntentAware> = emptySet(),
        secondaryIntents: Set<IntentAware> = emptySet(),
        stepsList: List<StoryStep<out StoryHandlerDefinition>> = emptyList(),
        unsupportedUserInterface: UserInterfaceType? = null,
        override val tags: Set<StoryTag> = emptySet(),
        val storyDefinitionConfiguration: StoryDefinitionConfiguration? = null,
) : StoryDefinition {

    override val steps: Set<StoryStep<out StoryHandlerDefinition>> =
        stepsList.onEach {
            if (it.intent == null) {
                stepToIntentRepository[it] = this@StoryDefinitionBase
            }
        }.toSet()

    override val unsupportedUserInterfaces: Set<UserInterfaceType> = listOfNotNull(unsupportedUserInterface).toSet()

    override val id: String get() = name
    override val starterIntents: Set<Intent> =
        setOf(Intent(name)) + otherStarterIntents.map { it.wrappedIntent() }.toSet()
    override val intents: Set<Intent> =
        setOf(Intent(name)) + (otherStarterIntents + secondaryIntents).map { it.wrappedIntent() }.toSet()

    open fun handle(bus: BotBus) = storyHandler.handle(bus)

    override fun toString(): String = "Story[$name]"
}
