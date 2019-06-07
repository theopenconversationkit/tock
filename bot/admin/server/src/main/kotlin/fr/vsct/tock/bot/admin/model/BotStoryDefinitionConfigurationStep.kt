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
    val intent: Intent?,
    /**
     * The optional intent to switch to when the step is reached.
     */
    val targetIntent: Intent?,
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
     * The children of the steps
     */
    val children: List<BotStoryDefinitionConfigurationStep> = emptyList(),
    /**
     * The level of the step.
     */
    val level: Int = 0,
    /**
     * Intent defined by the intent name.
     */
    val intentDefinition: IntentDefinition? = null,
    /**
     * Target Intent defined by the intent name.
     */
    val targetIntentDefinition: IntentDefinition? = null
) {

    constructor(story: StoryDefinitionConfiguration, e: StoryDefinitionConfigurationStep) :
        this(
            e.name,
            e.intent,
            e.targetIntent,
            e.answers.mapAnswers(),
            e.currentType,
            story.category,
            e.userSentence,
            e.children.map { BotStoryDefinitionConfigurationStep(story, it) },
            e.level
        )
}