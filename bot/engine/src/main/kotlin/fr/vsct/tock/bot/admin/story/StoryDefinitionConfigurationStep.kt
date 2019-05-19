package fr.vsct.tock.bot.admin.story

import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.SimpleStoryStep
import fr.vsct.tock.bot.definition.StoryStep

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
    override val currentType: AnswerConfigurationType,
    /**
     * The user sentence sample.
     */
    val userSentence: String = "",
    /**
     * The parent step name - if null the parent is the story.
     */
    val parentName: String? = null
) : StoryDefinitionAnswersContainer {

    fun toStoryStep(): StoryStep<*> =
        object : SimpleStoryStep {
            override val name: String get() = this@StoryDefinitionConfigurationStep.name
            override val intent: IntentAware? get() = this@StoryDefinitionConfigurationStep.intent
        }
}