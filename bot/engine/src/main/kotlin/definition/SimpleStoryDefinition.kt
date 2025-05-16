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

import ai.tock.translator.UserInterfaceType

/**
 * Simple implementation of [StoryDefinition].
 */
open class SimpleStoryDefinition(
    override val id: String,
    override val storyHandler: StoryHandler,
    override val starterIntents: Set<Intent>,
    /**
     * starter intents + other intents supported by the story.
     */
    override val intents: Set<Intent> = starterIntents,
    override val steps: Set<StoryStep<StoryHandlerDefinition>> = emptySet(),
    override val unsupportedUserInterfaces: Set<UserInterfaceType> = emptySet(),
    override val tags: Set<StoryTag> = emptySet()
) :
    StoryDefinition {

    constructor(
        id: String,
        storyHandler: StoryHandler,
        steps: Array<out StoryStep<StoryHandlerDefinition>> = emptyArray(),
        starterIntents: Set<IntentAware>,
        intents: Set<IntentAware> = starterIntents,
        unsupportedUserInterfaces: Set<UserInterfaceType> = emptySet()
    ) :
        this(
            id = id,
            storyHandler = storyHandler,
            starterIntents = starterIntents.map { it.wrappedIntent() }.toSet(),
            intents = intents.map { it.wrappedIntent() }.toSet(),
            steps = steps.toSet(),
            unsupportedUserInterfaces = unsupportedUserInterfaces
        )
}
