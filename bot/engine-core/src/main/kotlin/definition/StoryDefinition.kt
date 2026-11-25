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
 * The definition of a "Story".
 * A story holds a list of actions of the same domain.
 * The story provides a set of starter intents.
 * When these intents are detected, The story is started.
 *
 * Story definitions should usually not directly extend this class,
 * but instead extend [SimpleStoryHandlerBase] or [StoryDefinitionBase].
 */
interface StoryDefinition : IntentAware {
    /**
     * An unique identifier for a given bot.
     */
    val id: String

    /**
     * One or more intents that start the story.
     * Usually, you don't have the same starter intent in two different story definition.
     */
    val starterIntents: Set<Intent>

    /**
     * The complete list of intents supported by the story.
     */
    val intents: Set<Intent>

    /**
     * The story definition tags that specify different story types or roles.
     */
    val tags: Set<StoryTag> get() = emptySet()

    /**
     * Does this story is tagged with specified [tag]?
     */
    fun hasTag(tag: StoryTag): Boolean = tags.contains(tag)

    /**
     * The story handler of the story.
     */
    val storyHandler: StoryHandler

    /**
     * The root steps of the story.
     */
    val steps: Set<StoryStepDef>

    /**
     * True if the story handle metrics and is not a main tracked story
     */
    val metricStory: Boolean
        get() = false

    /**
     * When this story does not support all [UserInterfaceType]s.
     */
    val unsupportedUserInterfaces: Set<UserInterfaceType>

    /**
     * Is the specified intent is a starter intent?
     */
    fun isStarterIntent(intent: Intent) = starterIntents.contains(intent)

    /**
     * Is the specified intent is supported by this story?
     */
    fun supportIntent(intent: Intent) = intents.contains(intent)

    /**
     * The "referent" intent for this story.
     */
    fun mainIntent(): Intent = starterIntents.first()

    /**
     * Implementation for [IntentAware].
     */
    override fun wrappedIntent(): Intent = mainIntent()

    /**
     * Returns all steps of the story.
     */
    fun allSteps(): Set<StoryStepDef> = mutableSetOf<StoryStepDef>().apply { steps.forEach { allStep(this, it) } }

    private fun allStep(
        result: MutableSet<StoryStepDef>,
        step: StoryStepDef,
    ) {
        result.add(step)
        step.children.forEach { allStep(result, it) }
    }
}
