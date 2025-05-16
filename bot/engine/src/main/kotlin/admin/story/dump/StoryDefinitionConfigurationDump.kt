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

package ai.tock.bot.admin.story.dump

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.DedicatedAnswerConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationByBotStep
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.StoryTag
import ai.tock.shared.defaultNamespace
import java.util.Locale

/**
 * Object used for exporting/importing story definitions
 */
data class StoryDefinitionConfigurationDump(
    /**
     * The story definition identifier.
     */
    val storyId: String,
    /**
     * The bot identifier.
     */
    val botId: String,
    /**
     * The target main intent.
     */
    val intent: IntentWithoutNamespace,
    /**
     * The type of answer configuration.
     */
    val currentType: AnswerConfigurationType,
    /**
     * The answers available.
     */
    val answers: List<AnswerConfigurationDump>,
    /**
     * The version of the story.
     */
    val version: Int = 0,
    /**
     * The namespace of the story.
     */
    val namespace: String = defaultNamespace,
    /**
     * The mandatory entities.
     */
    val mandatoryEntities: List<StoryDefinitionConfigurationMandatoryEntityDump> = emptyList(),
    /**
     * The optional steps.
     */
    val steps: List<StoryDefinitionConfigurationStepDump> = emptyList(),
    /**
     * The name of the story.
     */
    val name: String = storyId,
    /**
     * The category of the story.
     */
    val category: String = "default",
    /**
     * The description of the story.
     */
    val description: String = "",
    /**
     * The user sentence sample.
     */
    val userSentence: String = "",
    /**
     * The user sentence sample locale.
     */
    val userSentenceLocale: Locale? = null,
    /**
     * The configuration name if any.
     */
    val configurationName: String? = null,
    /**
     * The optional features supported
     */
    val features: List<StoryDefinitionConfigurationFeatureDump> = emptyList(),
    /**
     * The story definition tags that specify different story types or roles.
     */
    val tags: Set<StoryTag> = emptySet(),
    /**
     * Answers by bot application configuration
     */
    val configuredAnswers: List<DedicatedAnswerConfiguration> = emptyList(),
    /**
     * Steps by bot application configuration
     */
    val configuredSteps: List<StoryDefinitionConfigurationByBotStep> = emptyList()
) {

    constructor(def: StoryDefinitionConfiguration) :
        this(
            storyId = def.storyId,
            botId = def.botId,
            intent = def.intent,
            currentType = def.currentType,
            answers = AnswerConfigurationDump.toDump(def.answers),
            version = def.version,
            namespace = def.namespace,
            mandatoryEntities = def.mandatoryEntities.map { StoryDefinitionConfigurationMandatoryEntityDump(it) },
            steps = def.steps.map { StoryDefinitionConfigurationStepDump(it, def.namespace, def.category) },
            name = def.name,
            category = def.category,
            description = def.description,
            userSentence = def.userSentence,
            userSentenceLocale = def.userSentenceLocale,
            configurationName = def.configurationName,
            features = def.features.map { StoryDefinitionConfigurationFeatureDump(it) },
            tags = def.tags,
            configuredAnswers = def.configuredAnswers,
            configuredSteps = def.configuredSteps
        )

    fun toStoryDefinitionConfiguration(controller: StoryDefinitionConfigurationDumpController): StoryDefinitionConfiguration =
        StoryDefinitionConfiguration(
            storyId = storyId,
            botId = controller.botId,
            intent = controller.checkIntent(intent)!!,
            currentType = currentType,
            answers = answers.map { it.toAnswer(currentType, controller) },
            version = version,
            namespace = controller.targetNamespace,
            mandatoryEntities = mandatoryEntities.map { it.toEntity(controller) },
            steps = steps.map { it.toStep(controller) },
            name = name,
            category = category,
            description = description,
            userSentence = userSentence,
            userSentenceLocale = userSentenceLocale,
            configurationName = null,
            features = features.mapNotNull { it.toFeature(controller) },
            tags = tags,
            configuredAnswers = configuredAnswers,
            configuredSteps = configuredSteps
        )
}
