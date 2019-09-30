package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.definition.Intent

/**
 * A required entity in a [StoryDefinitionConfiguration].
 */
data class StoryDefinitionConfigurationMandatoryEntity(
        /**
         * The role of the mandatory entity.
         */
        val role: String,
        /**
         * The intent used to find the entities.
         */
        val intent: Intent,
        /**
         * The answers available.
         */
        override val answers: List<AnswerConfiguration>,
        /**
         * The type of answer configuration.
         */
        override val currentType: AnswerConfigurationType) : StoryDefinitionAnswersContainer