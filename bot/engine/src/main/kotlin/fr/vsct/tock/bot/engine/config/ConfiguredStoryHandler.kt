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

package fr.vsct.tock.bot.engine.config

import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType.simple
import fr.vsct.tock.bot.admin.answer.ScriptAnswerConfiguration
import fr.vsct.tock.bot.admin.answer.SimpleAnswerConfiguration
import fr.vsct.tock.bot.admin.story.StoryDefinitionAnswersContainer
import fr.vsct.tock.bot.admin.story.StoryDefinitionConfiguration
import fr.vsct.tock.bot.definition.StoryHandler
import fr.vsct.tock.bot.engine.BotBus
import mu.KotlinLogging

/**
 *
 */
internal class ConfiguredStoryHandler(private val configuration: StoryDefinitionConfiguration) : StoryHandler {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun handle(bus: BotBus) {
        configuration.mandatoryEntities.forEach { entity ->
            if (bus.entityValueDetails(entity.role) == null) {
                entity.send(bus)
                return@handle
            }
        }

        configuration.send(bus)
    }

    private fun StoryDefinitionAnswersContainer.send(bus: BotBus) {
        findCurrentAnswer().apply {
            when (this) {
                null -> bus.fallbackAnswer()
                is SimpleAnswerConfiguration -> bus.handleSimpleAnswer(this)
                is ScriptAnswerConfiguration -> bus.handleScriptAnswer(this@send)
                else -> error("type not supported for now: $this")
            }
        }
    }

    override fun support(bus: BotBus): Double = 1.0

    private fun BotBus.fallbackAnswer() =
            botDefinition.unknownStory.storyHandler.handle(this)

    private fun BotBus.handleSimpleAnswer(simple: SimpleAnswerConfiguration?) {
        if (simple == null) {
            fallbackAnswer()
        } else {
            simple.answers.let {
                it.subList(0, it.size - 1)
                        .forEach { a ->
                            if (a.delay == -1L) send(a.key) else send(a.key, a.delay)
                        }
                it.last().apply {
                    if (delay == -1L) end(key) else end(key, delay)
                }
            }
        }
    }

    private fun BotBus.handleScriptAnswer(container: StoryDefinitionAnswersContainer) {
        container.storyDefinition(botDefinition.botId)
                ?.storyHandler
                ?.handle(this)
                ?: {
                    logger.warn { "no story definition for configured script for $container - use unknown" }
                    handleSimpleAnswer(container.findAnswer(simple) as? SimpleAnswerConfiguration)
                }.invoke()
    }
}