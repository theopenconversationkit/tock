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

import fr.vsct.tock.bot.admin.answer.SimpleAnswerConfiguration
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration
import fr.vsct.tock.bot.definition.StoryHandler
import fr.vsct.tock.bot.engine.BotBus

/**
 *
 */
internal class ConfiguredStoryHandler(val configuration: StoryDefinitionConfiguration)
    : StoryHandler {

    override fun handle(bus: BotBus) {
        configuration.findCurrentAnswer().apply {
            when (this) {
                is SimpleAnswerConfiguration -> bus.handleSimpleAnswer(this)
                else -> error("type not supported for now: $configuration")
            }
        }
    }

    private fun BotBus.handleSimpleAnswer(simple: SimpleAnswerConfiguration) {
        simple.answers.let {
            it.subList(0, it.size - 1)
                    .forEach {
                        send(it.key, it.delay)
                    }
            it.last().apply {
                end(key, delay)
            }
        }
    }
}