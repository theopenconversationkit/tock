package fr.vsct.tock.bot.admin.story

import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.definition.Intent

/**
 * A [StoryStep] configuration in a [StoryDefinitionConfiguration].
 */
data class StoryDefinitionConfigurationStep(
        /**
         * The name of the step.
         */
        val name: String,
        /**
         * The intent used to reach the step
         */
        val intent: Intent,
        /**
         * The answers available.
         */
        override val answers: List<AnswerConfiguration>,
        /**
         * The type of answer configuration.
         */
        override val currentType: AnswerConfigurationType
) : StoryDefinitionAnswersContainer