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
import ai.tock.bot.definition.EntityStepSelection
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.SimpleStoryStep
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.BotBus
import ai.tock.translator.I18nLabelValue

/**
 * A [StoryStep] configuration in a [StoryDefinitionConfiguration].
 */
data class StoryDefinitionConfigurationStep(
    /**
     * The name of the step.
     */
    val name: String,
    /**
     * The intent used to reach the step - mandatory if an answer is set, or if there is a [targetIntent]
     * - and if there is no [entity] set.
     */
    val intent: IntentWithoutNamespace?,
    /**
     * The optional intent to switch to when the step is reached.
     */
    val targetIntent: IntentWithoutNamespace?,
    /**
     * The answers available.
     */
    override val answers: List<AnswerConfiguration>,
    /**
     * The type of answer configuration.
     */
    override val currentType: AnswerConfigurationType,
    /**
     * The user sentence sample.
     */
    @Deprecated("use userSentenceLabel")
    val userSentence: String = "",
    val userSentenceLabel: I18nLabelValue? = null,
    /**
     * The children of the steps
     */
    val children: List<StoryDefinitionConfigurationStep> = emptyList(),
    /**
     * The level of the step.
     */
    val level: Int = 0,
    /**
     * Entity selection.
     */
    val entity: EntityStepSelection? = null,
    /**
     * The step metrics.
     */
    val metrics: List<StoryDefinitionStepMetric> = emptyList()
) : StoryDefinitionAnswersContainer {

    internal class Step(
        override val name: String,
        override val intent: IntentAware?,
        val configuration: StoryDefinitionConfigurationStep,
        private val storyConfiguration: StoryDefinitionConfiguration
    ) : SimpleStoryStep {
        constructor(s: StoryDefinitionConfigurationStep, conf: StoryDefinitionConfiguration) :
            this(
                s.name.takeUnless { it.isBlank() || !it.startsWith("##") }
                    ?: "${s.intent?.name}${(s.entity?.value ?: s.entity?.entityRole)?.let { "_$it" }}_${s.level}",
                s.intent?.intent(conf.namespace),
                s,
                conf
            )

        override fun equals(other: Any?): Boolean = name == (other as? Step)?.name

        override fun hashCode(): Int = name.hashCode()

        override val children: Set<StoryStep<StoryHandlerDefinition>>
            get() = configuration.children.map { it.toStoryStep(storyConfiguration) }.toSet()

        override val hasNoChildren: Boolean get() = children.isEmpty()

        override val entityStepSelection: EntityStepSelection? = configuration.entity

        override val metrics: List<StoryDefinitionStepMetric>
            get() = configuration.metrics
    }

    val hasNoChildren: Boolean get() = children.isEmpty()

    constructor(step: StoryStep<*>) :
        this(
            step.name,
            step.intent?.intentWithoutNamespace(),
            null,
            emptyList(),
            builtin
        )

    fun toStoryStep(story: StoryDefinitionConfiguration): StoryStep<StoryHandlerDefinition> = Step(this, story)

    override fun findNextSteps(bus: BotBus, story: StoryDefinitionConfiguration): List<CharSequence> =
        children.map { it.userSentenceLabel ?: it.userSentence }
}
