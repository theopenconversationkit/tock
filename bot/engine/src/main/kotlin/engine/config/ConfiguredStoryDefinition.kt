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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep.Step
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandler
import ai.tock.bot.definition.StoryStep
import ai.tock.translator.UserInterfaceType

/**
 *
 */
class ConfiguredStoryDefinition(val configuration: StoryDefinitionConfiguration) : StoryDefinition {

    val answerType: AnswerConfigurationType = configuration.currentType

    override val id: String = configuration._id.toString()

    override val starterIntents: Set<Intent> =
        setOf(configuration.mainIntent) + (configuration.storyDefinition(configuration.botId)?.starterIntents
            ?: emptySet())

    override val storyHandler: StoryHandler = ConfiguredStoryHandler(configuration)

    override val steps: Set<StoryStep<*>> =
        (configuration.storyDefinition(configuration.botId)?.steps ?: emptySet()) +
            configuration.steps.map { it.toStoryStep(configuration) }

    override val intents: Set<Intent> =
        starterIntents +
            (configuration.storyDefinition(configuration.botId)?.intents ?: emptySet()) +
            configuration.mandatoryEntities.map { it.intent.intent(configuration.namespace) } +
            allSteps()
                .filterIsInstance<Step>()
                .filter { it.configuration.findCurrentAnswer() != null || it.configuration.targetIntent != null }
                .mapNotNull { it.intent?.wrappedIntent() }

    override val unsupportedUserInterfaces: Set<UserInterfaceType> =
        configuration.storyDefinition(configuration.botId)?.unsupportedUserInterfaces ?: emptySet()

}