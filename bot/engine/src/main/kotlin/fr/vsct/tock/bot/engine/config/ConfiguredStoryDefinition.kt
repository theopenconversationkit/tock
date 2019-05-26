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
import fr.vsct.tock.bot.admin.story.StoryDefinitionConfiguration
import fr.vsct.tock.bot.admin.story.StoryDefinitionConfigurationStep
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.definition.StoryHandler
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.translator.UserInterfaceType

/**
 *
 */
internal class ConfiguredStoryDefinition(val configuration: StoryDefinitionConfiguration) : StoryDefinition {

    val answerType: AnswerConfigurationType = configuration.currentType

    override val id: String = configuration._id.toString()

    override val starterIntents: Set<Intent> =
        setOf(configuration.intent) + (configuration.storyDefinition(configuration.botId)?.starterIntents
            ?: emptySet())

    override val storyHandler: StoryHandler = ConfiguredStoryHandler(configuration)

    override val steps: Set<StoryStep<*>> =
        (configuration.storyDefinition(configuration.botId)?.steps ?: emptySet()) +
            configuration.steps.map { it.toStoryStep() }

    override val intents: Set<Intent> =
        starterIntents +
            (configuration.storyDefinition(configuration.botId)?.intents ?: emptySet()) +
            configuration.mandatoryEntities.map { it.intent } +
            allSteps().mapNotNull { it.intent?.wrappedIntent() }

    override val unsupportedUserInterfaces: Set<UserInterfaceType> =
        configuration.storyDefinition(configuration.botId)?.unsupportedUserInterfaces ?: emptySet()

    private fun allSteps(): Set<StoryStep<*>> =
        mutableSetOf<StoryStep<*>>().apply { configuration.steps.forEach { allStep(this, it) } }

    private fun allStep(result: MutableSet<StoryStep<*>>, step: StoryDefinitionConfigurationStep) {
        result.add(step.toStoryStep())
        step.children.forEach { allStep(result, it) }
    }

}