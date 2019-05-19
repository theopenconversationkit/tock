package fr.vsct.tock.bot.admin.model

import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.admin.story.StoryDefinitionConfiguration
import fr.vsct.tock.bot.admin.story.StoryDefinitionConfigurationStep
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition

data class BotStoryDefinitionConfigurationStep(
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
    val userSentence: String = "",
    /**
     * The parent step name - if null the parent is the story.
     */
    val parentName: String? = null,
    /**
     * Intent defined by the intent name.
     */
    val intentDefinition: IntentDefinition? = null) {

    constructor(story: StoryDefinitionConfiguration, e: StoryDefinitionConfigurationStep) :
        this(
            e.name,
            e.intent,
            e.answers.mapAnswers(),
            e.currentType,
            story.category,
            e.userSentence
        )
}