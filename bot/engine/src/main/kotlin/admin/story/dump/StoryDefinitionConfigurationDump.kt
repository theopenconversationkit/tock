/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.definition.SimpleIntentName
import ai.tock.shared.defaultNamespace
import java.util.Locale

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
    val intent: SimpleIntentName,
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
    val features: List<StoryDefinitionConfigurationFeatureDump> = emptyList()
) {

    constructor(def: StoryDefinitionConfiguration) :
        this(
            def.storyId,
            def.botId,
            def.intent,
            def.currentType,
            AnswerConfigurationDump.toDump(def.answers),
            def.version,
            def.namespace,
            def.mandatoryEntities.map { StoryDefinitionConfigurationMandatoryEntityDump(it) },
            def.steps.map { StoryDefinitionConfigurationStepDump(it, def.namespace, def.category) },
            def.name,
            def.category,
            def.description,
            def.userSentence,
            def.userSentenceLocale,
            def.configurationName,
            def.features.map { StoryDefinitionConfigurationFeatureDump(it) }
        )

    fun toStoryDefinitionConfiguration(controller: StoryDefinitionConfigurationDumpController): StoryDefinitionConfiguration =
        StoryDefinitionConfiguration(
            storyId,
            controller.botId,
            controller.checkIntent(intent)!!,
            currentType,
            answers.map { it.toAnswer(currentType, controller) },
            version,
            controller.targetNamespace,
            mandatoryEntities.map { it.toEntity(controller) },
            steps.map { it.toStep(controller) },
            name,
            category,
            description,
            userSentence,
            userSentenceLocale,
            null,
            features.mapNotNull { it.toFeature(controller) }
        )
}