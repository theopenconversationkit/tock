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

package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.AnswerConfigurationType.builtin
import ai.tock.bot.admin.answer.BuiltInAnswerConfiguration
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.shared.defaultNamespace
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

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
    val intent: IntentWithoutNamespace,
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
     * The user sentence sample locale.
     */
    val userSentenceLocale: Locale? = null,
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
            storyDefinition.mainIntent().intentWithoutNamespace(),
            builtin,
            listOf(BuiltInAnswerConfiguration(storyDefinition.javaClass.kotlin.qualifiedName)),
            namespace = botDefinition.namespace,
            configurationName = configurationName,
            steps = storyDefinition.steps.map { StoryDefinitionConfigurationStep(it) }
        )

    override fun findNextSteps(bus: BotBus, story: StoryDefinitionConfiguration): List<CharSequence> =
        steps.map { it.userSentenceLabel ?: it.userSentence }

    internal fun findFeatures(applicationId: String?): List<StoryDefinitionConfigurationFeature> =
            when {
                features.isEmpty() -> emptyList()
                applicationId == null -> features.filter { it.botApplicationConfigurationId == null }
                else -> {
                    val app = BotRepository.getConfigurationByApplicationId(applicationId)
                    features.filter {
                                        it.botApplicationConfigurationId == null
                                                || it.botApplicationConfigurationId == app?._id
                                                || it.botApplicationConfigurationId == app?.targetConfigurationId
                    }
                }
            }

    internal fun isDisabled(applicationId: String?): Boolean =
            findFeatures(applicationId).let {
                when {
                    it.isEmpty() -> false
                    else -> it.any { it.switchToStoryId == null && !it.enabled }
                }
            }

    internal fun findEnabledFeature(applicationId: String?): StoryDefinitionConfigurationFeature? =
            findFeatures(applicationId).find { it.enabled }

    @Transient
    internal val mainIntent:Intent = intent.intent(namespace)

    private fun findDefaultEnabledFeature(): StoryDefinitionConfigurationFeature? = findEnabledFeature(null)
}