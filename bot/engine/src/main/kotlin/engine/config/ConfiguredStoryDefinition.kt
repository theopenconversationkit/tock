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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.bot.BotApplicationConfigurationKey
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep.Step
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandler
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.definition.StoryTag
import ai.tock.translator.UserInterfaceType

/**
 * Implementation of StoryDefinition for StoryDefinitionConfiguration.
 */
internal class ConfiguredStoryDefinition(
    definition: BotDefinitionWrapper,
    val configuration: StoryDefinitionConfiguration,
    botApplicationConfigurationKey: BotApplicationConfigurationKey? = null,
    val configurationStoryHandler: BotConfigurationStoryHandler? = null
) : StoryDefinition {

    val answerType: AnswerConfigurationType = configuration.currentType

    override val id: String = configuration._id.toString()

    override val tags: Set<StoryTag> = configuration.tags

    override val metricStory = configuration.metricStory

    val storyId: String = configuration.storyId

    /**
     * The name of the story.
     */
    val name: String = configuration.name

    fun isDisabled(applicationId: String?): Boolean = configuration.isDisabled(applicationId)

    fun findEnabledStorySwitchId(applicationId: String?): String? =
        configuration.findEnabledStorySwitchId(applicationId)

    override val starterIntents: Set<Intent> =
        setOf(configuration.mainIntent) +
            (configuration.storyDefinition(definition, configuration)?.starterIntents ?: emptySet())

    override val storyHandler: StoryHandler = ConfiguredStoryHandler(definition, configuration, configurationStoryHandler)

    override val steps: Set<StoryStepDef> =
        (configuration.storyDefinition(definition, configuration)?.steps ?: emptySet()) +
            configuration.findSteps(botApplicationConfigurationKey).map { it.toStoryStep(configuration) }

    override val intents: Set<Intent> =
        starterIntents +
            (configuration.storyDefinition(definition, configuration)?.intents ?: emptySet()) +
            configuration.mandatoryEntities.map { it.intent.intent(configuration.namespace) } +
            allSteps()
                .filterIsInstance<Step>()
                .filter { it.configuration.findCurrentAnswer() != null || it.configuration.targetIntent != null }
                .mapNotNull { it.intent?.wrappedIntent() }

    override val unsupportedUserInterfaces: Set<UserInterfaceType> =
        configuration.storyDefinition(definition, configuration)?.unsupportedUserInterfaces ?: emptySet()

    override fun toString(): String {
        return "story[$id]"
    }

    override fun equals(other: Any?): Boolean {
        return (other as? ConfiguredStoryDefinition)?.id == id
    }

    override fun hashCode(): Int = id.hashCode()
}
