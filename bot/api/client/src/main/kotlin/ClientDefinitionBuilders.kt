/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.bot.api.client

import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware

private fun defaultUnknownStory() = unknownStory { end("Sorry I didn't understand") }

/**
 * Create a story addressing [Intent.unknown] intent.
 */
fun unknownStory(
    /**
     * The handler for the story.
     */
    handler: (ClientBus).() -> Unit) = ClientStoryDefinition(Intent.unknown, handler = newStoryHandler(handler))

/**
 * Creates a new bot.
 */
fun newBot(
    apiKey: String,
    /**
     * List of stories supported by the bot.
     */
    stories: List<ClientStoryDefinition>,
    unknownStory: ClientStoryDefinition = defaultUnknownStory()
): ClientBotDefinition = ClientBotDefinition(apiKey, stories, unknownStory)

/**
 * Creates a new bot.
 */
fun newBot(
    apiKey: String,
    /**
     * List of stories supported by the bot.
     */
    vararg stories: ClientStoryDefinition
): ClientBotDefinition =
    newBot(
        apiKey,
        stories.toList(),
        stories.find { it.wrap(Intent.unknown) } ?: defaultUnknownStory()
    )

/**
 * Creates a new story.
 */
fun newStory(
    /**
     * The main intent.
     */
    mainIntent: String,
    otherStarterIntents: Set<IntentAware> = emptySet(),
    secondaryIntents: Set<IntentAware> = emptySet(),
    steps: List<ClientStep> = emptyList(),
    storyId: String = mainIntent,
    /**
     * The handler for the story.
     */
    handler: (ClientBus).() -> Unit
): ClientStoryDefinition =
    ClientStoryDefinition(
        Intent(mainIntent),
        otherStarterIntents,
        secondaryIntents,
        steps,
        storyId,
        newStoryHandler(handler)
    )

/**
 * Creates a new story.
 */
fun newStory(
    /**
     * The main intent.
     */
    mainIntent: IntentAware,
    otherStarterIntents: Set<IntentAware> = emptySet(),
    secondaryIntents: Set<IntentAware> = emptySet(),
    steps: List<ClientStep> = emptyList(),
    storyId: String = mainIntent.wrappedIntent().name,
    /**
     * The handler for the story.
     */
    handler: (ClientBus).() -> Unit
): ClientStoryDefinition =
    ClientStoryDefinition(
        mainIntent,
        otherStarterIntents,
        secondaryIntents,
        steps,
        storyId,
        newStoryHandler(handler)
    )

/**
 * Creates a new [ClientStoryHandler].
 */
fun newStoryHandler(handler: (ClientBus).() -> Unit): ClientStoryHandler =
    object : ClientStoryHandler {
        override fun handle(bus: ClientBus) {
            handler(bus)
        }
    }