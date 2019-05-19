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

    private data class Step(override val name: String, override val intent: IntentAware) : SimpleStoryStep {
        constructor(s: StoryDefinitionConfigurationStep) : this(s.name, s.intent)
    }

    fun toStoryStep(): StoryStep<*> = Step(this)

    override fun findNextSteps(story: StoryDefinitionConfiguration): List<String> =
        story.steps.filter { it.parentName == name }.map { it.userSentence }
}