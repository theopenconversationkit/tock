/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.api.service

import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.api.model.configuration.ClientConfiguration
import ai.tock.bot.api.model.configuration.StepConfiguration
import ai.tock.bot.definition.BotDefinitionBase
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.SimpleStoryDefinition
import ai.tock.bot.definition.SimpleStoryHandlerBase
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.BotBus
import ai.tock.shared.error
import mu.KotlinLogging

internal class FallbackStoryHandler(
    private val defaultUnknown: StoryDefinition,
    private val handler: BotApiHandler
) : SimpleStoryHandlerBase() {

    private val logger = KotlinLogging.logger {}

    override fun action(bus: BotBus) {
        try {
            handler.send(bus)
        } catch (e: Exception) {
            logger.error(e)
            defaultUnknown.storyHandler.handle(bus)
        }
    }
}

internal class FallbackStoryDefinition(
    defaultUnknown: StoryDefinition,
    handler: BotApiHandler
) : SimpleStoryDefinition(
    defaultUnknown.id,
    FallbackStoryHandler(defaultUnknown, handler),
    defaultUnknown.intents
)

internal class ApiStep(s: StepConfiguration) : StoryStep<StoryHandlerDefinition> {
    override val name: String = s.name
    override val intent: IntentAware = Intent(s.mainIntent)
    override val otherStarterIntents: Set<IntentAware> = s.otherStarterIntents.map { Intent(it) }.toSet()
    override val secondaryIntents: Set<IntentAware> = s.secondaryIntents.map { Intent(it) }.toSet()
}

internal class BotApiDefinition(
    configuration: BotConfiguration,
    clientConfiguration: ClientConfiguration?,
    handler: BotApiHandler
) :
    BotDefinitionBase(
        configuration.botId,
        configuration.namespace,
        clientConfiguration
            ?.stories
            ?.filter { it.mainIntent != Intent.unknown.name }
            ?.map { s ->
                SimpleStoryDefinition(
                    s.name,
                    FallbackStoryHandler(defaultUnknownStory, handler),
                    setOf(Intent(s.mainIntent)) + s.otherStarterIntents.map { Intent(it) },
                    setOf(Intent(s.mainIntent)) + s.otherStarterIntents.map { Intent(it) } + s.secondaryIntents.map {
                        Intent(
                            it
                        )
                    },
                    s.steps.map { ApiStep(it) }.toSet()
                )
            } ?: emptyList(),
        configuration.nlpModel,
        FallbackStoryDefinition(defaultUnknownStory, handler)
    ) {

    override fun findIntent(intent: String, applicationId: String): Intent =
        super.findIntent(intent, applicationId).let {
            if (it.wrap(Intent.unknown)) {
                Intent(intent)
            } else {
                it
            }
        }
}
