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

package ai.tock.bot.api.client

import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware

private fun defaultUnknownStory() = unknownStory { end("Sorry I didn't understand") }

/**
 * Create a story addressing [Intent.unknown] intent.
 * @param handler The handler for the story.
 */
fun unknownStory(
    handler: suspend (ClientBus).() -> Unit
) = ClientStoryDefinition(Intent.unknown, handler = newStoryHandler(handler))

/**
 * Creates a [definition for a new bot][ClientBotDefinition] in bot Api mode
 * @param apiKey the api key for the bot configuration, as found in  Tock Studio
 * @param stories List of [stories][ClientStoryDefinition] supported by the bot.
 * @param unknownStory the story to trigger when the `unknown` intent is selected
 */
fun newBot(
    apiKey: String,
    stories: List<ClientStoryDefinition>,
    unknownStory: ClientStoryDefinition = defaultUnknownStory()
): ClientBotDefinition = ClientBotDefinition(apiKey, stories, unknownStory)

/**
 * Creates a new [bot][ClientBotDefinition] in bot Api mode
 * @param apiKey the api key for the bot configuration, as found in  Tock Studio
 * @param stories List of [stories][ClientStoryDefinition] supported by the bot.
 */
fun newBot(
    apiKey: String,
    vararg stories: ClientStoryDefinition
): ClientBotDefinition =
    newBot(
        apiKey,
        stories.toList(),
        stories.find { it.wrap(Intent.unknown) } ?: defaultUnknownStory()
    )

/**
 * Creates a new [story][ClientStoryDefinition] in bot Api mode
 * @param mainIntent [String] The main intent name.
 * @param otherStarterIntents other intents that triggers the story
 * @param secondaryIntents other intents available in the story scope when it is triggered
 * @param steps List of [story steps][ClientStep]
 * @param storyId default is [mainIntent] name
 * @param handler lamdba handler for the story
 */
fun newStory(
    mainIntent: String,
    otherStarterIntents: Set<IntentAware> = emptySet(),
    secondaryIntents: Set<IntentAware> = emptySet(),
    steps: List<ClientStep> = emptyList(),
    storyId: String = mainIntent,
    handler: suspend (ClientBus).() -> Unit
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
 * Creates a new story [ClientStoryDefinition] in bot Api mode
 * @param mainIntent [IntentAware] The main intent name.
 * @param otherStarterIntents other intents that triggers the story
 * @param secondaryIntents other intents available in the story scope when it is triggered
 * @param steps List of steps stories [ClientStep]
 * @param storyId default is [mainIntent] with wrappedIntent name
 * @param handler lamdba handler for the story
 */
fun newStory(
    mainIntent: IntentAware,
    otherStarterIntents: Set<IntentAware> = emptySet(),
    secondaryIntents: Set<IntentAware> = emptySet(),
    steps: List<ClientStep> = emptyList(),
    storyId: String = mainIntent.wrappedIntent().name,
    handler: suspend (ClientBus).() -> Unit
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
 * @param handler lamdba handler for the story
 */
fun newStoryHandler(handler: suspend (ClientBus).() -> Unit): ClientStoryHandler =
    object : ClientStoryHandler {
        override suspend fun handle(bus: ClientBus) {
            handler(bus)
        }
    }
