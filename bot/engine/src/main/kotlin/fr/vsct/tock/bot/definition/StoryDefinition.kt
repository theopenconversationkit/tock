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

/**
 * The definition of a "Story".
 * A story holds a list of actions of the same domain.
 * The story provides a set of starter intents.
 * When theses intents are detected, The story is started.
 *
 * Story definitions should usually not directly extend this class, but instead extend [StoryDefinitionBase].
 */
interface StoryDefinition {

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
     * The story handler of the story.
     */
    val storyHandler: StoryHandler

    /**
     * The steps of the story.
     */
    val steps: Set<StoryStep>

    fun isStarterIntent(intent: Intent) = starterIntents.contains(intent)

    fun supportIntent(intent: Intent) = intents.contains(intent)

    fun mainIntent() : Intent = starterIntents.first()

}