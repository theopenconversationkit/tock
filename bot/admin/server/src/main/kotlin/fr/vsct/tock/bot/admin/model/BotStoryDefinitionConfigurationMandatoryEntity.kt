package fr.vsct.tock.bot.admin.model

import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.admin.story.StoryDefinitionConfiguration
import fr.vsct.tock.bot.admin.story.StoryDefinitionConfigurationMandatoryEntity
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.nlp.front.shared.config.EntityDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition

data class BotStoryDefinitionConfigurationMandatoryEntity(
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
            e.intent,
            e.answers.mapAnswers(),
            e.currentType,
            story.category
        )
}