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

import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.definition.StoryHandler

/**
 *
 */
internal class ConfigurationBasedStoryDefinition(configuration: StoryDefinitionConfiguration)
    : StoryDefinition {

    val answerType: AnswerConfigurationType = configuration.currentType
    override val id: String = configuration._id!!
    override val starterIntents: Set<Intent> = setOf(configuration.intent)
    override val intents: Set<Intent> = starterIntents
    override val storyHandler: StoryHandler = ConfigurationBasedStoryHandler(configuration)
}