package fr.vsct.tock.bot.admin.story

import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.definition.Intent

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