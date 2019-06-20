/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.api.service

import fr.vsct.tock.bot.admin.bot.BotConfiguration
import fr.vsct.tock.bot.definition.BotDefinitionBase
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.SimpleStoryDefinition
import fr.vsct.tock.bot.definition.SimpleStoryHandlerBase
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.shared.error
import mu.KotlinLogging

internal class FallbackStoryHandler(
    configuration: BotConfiguration,
    val defaultUnknown: StoryDefinition
) : SimpleStoryHandlerBase() {

    private val logger = KotlinLogging.logger {}
    private val handler = BotApiHandler(configuration.apiKey, configuration.webhookUrl)

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
    configuration: BotConfiguration,
    defaultUnknown: StoryDefinition
) : SimpleStoryDefinition(
    defaultUnknown.id,
    FallbackStoryHandler(configuration, defaultUnknown),
    defaultUnknown.intents
)

internal class BotApiDefinition(configuration: BotConfiguration) :
    BotDefinitionBase(
        configuration.botId,
        configuration.namespace,
        emptyList(),
        configuration.nlpModel,
        FallbackStoryDefinition(configuration, defaultUnknownStory)
    ) {

    override fun findIntent(intent: String): Intent = Intent(intent)
}