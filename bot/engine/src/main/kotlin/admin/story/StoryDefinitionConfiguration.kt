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

package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.AnswerConfigurationType.builtin
import ai.tock.bot.admin.answer.BuiltInAnswerConfiguration
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.BotRepository
import ai.tock.shared.defaultNamespace
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 * A [StoryDefinition] defined at runtime.
 */
data class StoryDefinitionConfiguration(
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
    val intent: Intent,
    /**
     * The type of answer configuration.
     */
    override val currentType: AnswerConfigurationType,
    /**
     * The answers available.
     */
    override val answers: List<AnswerConfiguration>,
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
    val mandatoryEntities: List<StoryDefinitionConfigurationMandatoryEntity> = emptyList(),
    /**
     * The optional steps.
     */
    val steps: List<StoryDefinitionConfigurationStep> = emptyList(),
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
     * The configuration name if any.
     */
    val configurationName: String? = null,
    /**
     * The optional supported
     */
    val features: List<StoryDefinitionConfigurationFeature> = emptyList(),
    /**
     * The configuration identifier.
     */
    val _id: Id<StoryDefinitionConfiguration> = newId()
) : StoryDefinitionAnswersContainer {

    constructor(botDefinition: BotDefinition, storyDefinition: StoryDefinition, configurationName: String?) :
        this(
            storyDefinition.id,
            botDefinition.botId,
            storyDefinition.mainIntent().wrappedIntent(),
            builtin,
            listOf(BuiltInAnswerConfiguration(storyDefinition.javaClass.kotlin.qualifiedName)),
            namespace = botDefinition.namespace,
            configurationName = configurationName,
            steps = storyDefinition.steps.map { StoryDefinitionConfigurationStep(it) }
        )

    override fun findNextSteps(story: StoryDefinitionConfiguration): List<String> =
        steps.map { it.userSentence }

    internal fun hasOnlyDisabledFeature(applicationId: String?): Boolean =
        when {
            features.isEmpty() -> false
            applicationId == null -> features.none { it.botApplicationConfigurationId == null && it.enabled }
            else -> {
                val app = BotRepository.getConfigurationByApplicationId(applicationId)
                features.none {
                    it.enabled &&
                        (
                            it.botApplicationConfigurationId == null
                                || it.botApplicationConfigurationId == app?._id
                                || it.botApplicationConfigurationId == app?.targetConfigurationId
                            )
                }
            }
        }

    internal fun findEnabledFeature(applicationId: String?): StoryDefinitionConfigurationFeature? =
        when {
            features.isEmpty() -> null
            applicationId == null -> findDefaultEnabledFeature()
            else -> BotRepository.getConfigurationByApplicationId(applicationId)?.let { conf ->
                features.find {
                    it.enabled &&
                        (it.botApplicationConfigurationId == conf._id || it.botApplicationConfigurationId == conf.targetConfigurationId)
                }
            } ?: findDefaultEnabledFeature()
        }

    private fun findDefaultEnabledFeature(): StoryDefinitionConfigurationFeature? = features.find { it.botApplicationConfigurationId == null && it.enabled }
}