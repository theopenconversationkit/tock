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

package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.AnswerConfigurationType.builtin
import ai.tock.bot.admin.answer.BuiltInAnswerConfiguration
import ai.tock.bot.admin.answer.DedicatedAnswerConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationKey
import ai.tock.bot.admin.indicators.metric.Metric
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryTag
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.nlp.api.client.model.NlpIntentQualifier
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
     * Current features.
     */
    val features: List<StoryDefinitionConfigurationFeature> = emptyList(),
    /**
     * The configuration identifier.
     */
    val _id: Id<StoryDefinitionConfiguration> = newId(),
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
    val configuredSteps: List<StoryDefinitionConfigurationByBotStep> = emptyList(),
    /**
     * To filter/re-qualify next intents
     */
    val nextIntentsQualifiers: List<NlpIntentQualifier> = emptyList(),

    /**
     * True if the story handle metrics and is not a main tracked story
     */
    val metricStory: Boolean = false

) : StoryDefinitionAnswersContainer {

    constructor(botDefinition: BotDefinition, storyDefinition: StoryDefinition, configurationName: String?) :
        this(
            storyId = storyDefinition.id,
            tags = storyDefinition.tags,
            botId = botDefinition.botId,
            intent = storyDefinition.mainIntent().intentWithoutNamespace(),
            currentType = builtin,
            answers = listOf(BuiltInAnswerConfiguration(storyDefinition.javaClass.kotlin.qualifiedName)),
            namespace = botDefinition.namespace,
            configurationName = configurationName,
            steps = storyDefinition.steps.map { StoryDefinitionConfigurationStep(it) }
        )

    override fun findNextSteps(bus: BotBus, story: StoryDefinitionConfiguration): List<CharSequence> =
        findSteps(BotApplicationConfigurationKey(bus)).map { it.userSentenceLabel ?: it.userSentence }

    internal fun findSteps(key: BotApplicationConfigurationKey?): List<StoryDefinitionConfigurationStep> =
        (
            key?.let {
                val configurationName =
                    BotRepository.getConfigurationByApplicationId(key)?.name
                configuredSteps.firstOrNull { it.botConfiguration == configurationName }?.steps
            } ?: steps
            )

    private fun findFeatures(applicationId: String?): List<StoryDefinitionConfigurationFeature> =
        when {
            features.isEmpty() -> emptyList()
            applicationId == null -> features.filter { it.botApplicationConfigurationId == null }
            else -> {
                val app = BotRepository.getConfigurationByApplicationId(
                    BotApplicationConfigurationKey(
                        applicationId = applicationId,
                        namespace = namespace,
                        botId = botId
                    )
                )
                features.filter { it.supportConfiguration(app) }
            }
        }

    /**
     * Save one [Metric]
     * @param metric a [Metric] to save
     */
    fun saveMetric(metric: Metric) = BotRepository.saveMetric(metric)

    /**
     * Save many [Metric]
     * @param metrics a set of [Metric] to save
     */
    fun saveMetrics(metrics: List<Metric>) = BotRepository.saveMetrics(metrics)

    internal fun isDisabled(applicationId: String?): Boolean =
        findFeatures(applicationId).let {
            when {
                it.isEmpty() -> false
                else -> it.any { f -> f.switchToStoryId == null && f.endWithStoryId == null && !f.enabled }
            }
        }

    internal fun findEnabledStorySwitchId(applicationId: String?): String? {
        val features = findEnabledFeatures(applicationId)

        // search first for dedicated features
        val dedicatedFeature = applicationId.getApp()?.let { app ->
            features.find { feature ->
                feature.switchToStoryId != null && feature.supportDedicatedConfiguration(app)
            }
        }

        return dedicatedFeature?.switchToStoryId ?: features.find { it.switchToStoryId != null }?.switchToStoryId
    }

     fun findEnabledEndWithStoryId(applicationId: String?): String? {
        val features = findEnabledFeatures(applicationId)

        // search first for dedicated features
        val dedicatedFeature = applicationId.getApp()?.let { app ->
            features.find { feature ->
                feature.endWithStoryId != null && feature.supportDedicatedConfiguration(app)
            }
        }

        return dedicatedFeature?.endWithStoryId ?: features.find { it.endWithStoryId != null }?.endWithStoryId
    }

    private fun String?.getApp(): BotApplicationConfiguration? = this?.let {
        BotRepository.getConfigurationByApplicationId(
            BotApplicationConfigurationKey(
                applicationId = this,
                namespace = namespace,
                botId = botId
            )
        )
    }

    private fun findEnabledFeatures(applicationId: String?): List<StoryDefinitionConfigurationFeature> =
        findFeatures(applicationId).filter { it.enabled }

    @Transient
    internal val mainIntent: Intent = intent.intent(namespace)
}
