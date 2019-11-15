package ai.tock.bot.admin.model

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationMandatoryEntity
import ai.tock.bot.definition.Intent
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition

data class BotStoryDefinitionConfigurationMandatoryEntity(
    /**
     * The role of the mandatory entity.
     */
    val role: String,
    /**
     * The type of mandatory entity.
     */
    val entityType: String,
    /**
     * The intent used to find the entities.
     */
    val intent: Intent,
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
     * Entity defined by the entity role.
     */
    val entity: EntityDefinition? = null,
    /**
     * Intent defined by the intent name.
     */
    val intentDefinition: IntentDefinition? = null
) {

    constructor(story: StoryDefinitionConfiguration, e: StoryDefinitionConfigurationMandatoryEntity) :
        this(
            e.role,
            e.entityType,
            e.intent,
            e.answers.mapAnswers(),
            e.currentType,
            story.category
        )
}