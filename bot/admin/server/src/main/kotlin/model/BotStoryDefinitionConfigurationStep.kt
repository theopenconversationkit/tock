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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep
import ai.tock.bot.admin.story.StoryDefinitionStepMetric
import ai.tock.bot.definition.EntityStepSelection
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.Translator

data class BotStoryDefinitionConfigurationStep(
    /**
     * The name of the step.
     */
    val name: String,
    /**
     * The intent used to reach the step
     */
    val intent: IntentWithoutNamespace?,
    /**
     * The optional intent to switch to when the step is reached.
     */
    val targetIntent: IntentWithoutNamespace?,
    /**
     * The answers available.
     */
    val answers: List<BotAnswerConfiguration>,
    /**
     * The type of answer configuration.
     */
    val currentType: AnswerConfigurationType,
    /**
     * The category of the answers.
     */
    val category: String,
    /**
     * The user sentence sample.
     */
    val userSentence: I18nLabel,
    /**
     * The children of the steps
     */
    val children: List<BotStoryDefinitionConfigurationStep> = emptyList(),
    /**
     * The level of the step.
     */
    val level: Int = 0,
    /**
     * Entity selection.
     */
    val entity: EntityStepSelection?,
    /**
     * The step metrics.
     */
    val metrics: List<StoryDefinitionStepMetric> = emptyList(),
    /**
     * Intent defined by the intent name.
     */
    val intentDefinition: IntentDefinition? = null,
    /**
     * Target Intent defined by the intent name.
     */
    val targetIntentDefinition: IntentDefinition? = null,
) {
    constructor(story: StoryDefinitionConfiguration, e: StoryDefinitionConfigurationStep, readOnly: Boolean = false) :
        this(
            e.name.takeUnless { it.isBlank() } ?: "${e.intent?.name}_${e.level}",
            e.intent,
            e.targetIntent,
            e.answers.mapAnswers(story.userSentenceLocale, readOnly),
            e.currentType,
            story.category,
            (
                e.userSentenceLabel
                    ?: I18nLabelValue(
                        I18nKeyProvider.generateKey(story.namespace, story.category, e.userSentence),
                        story.namespace,
                        story.category,
                        e.userSentence,
                    )
            )
                .let { Translator.saveIfNotExist(it, readOnly) },
            e.children.map { BotStoryDefinitionConfigurationStep(story, it, readOnly) },
            e.level,
            e.entity,
            e.metrics,
        )

    fun hasMetrics(): Boolean {
        return metrics.isNotEmpty() || children.any { it.hasMetrics() }
    }
}
