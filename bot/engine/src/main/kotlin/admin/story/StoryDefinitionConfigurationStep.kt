package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.AnswerConfigurationType.builtin
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.SimpleStoryStep
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep

/**
 * A [StoryStep] configuration in a [StoryDefinitionConfiguration].
 */
data class StoryDefinitionConfigurationStep(
    /**
     * The name of the step.
     */
    val name: String,
    /**
     * The intent used to reach the step - mandatory if an answer is set, or if there is a [targetIntent].
     */
    val intent: Intent?,
    /**
     * The optional intent to switch to when the step is reached.
     */
    val targetIntent: Intent?,
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
     * The children of the steps
     */
    val children: List<StoryDefinitionConfigurationStep> = emptyList(),
    /**
     * The level of the step.
     */
    val level: Int = 0
) : StoryDefinitionAnswersContainer {

    internal class Step(
        override val name: String,
        override val intent: IntentAware?,
        val configuration: StoryDefinitionConfigurationStep
    ) : SimpleStoryStep {
        constructor(s: StoryDefinitionConfigurationStep) : this(s.name, s.intent, s)

        override fun equals(other: Any?): Boolean = name == (other as? Step)?.name

        override fun hashCode(): Int = name.hashCode()

        override val children: Set<StoryStep<StoryHandlerDefinition>>
            get() = configuration.children.map { it.toStoryStep() }.toSet()
    }

    constructor(step: StoryStep<*>) :
        this(
            step.name,
            step.intent?.wrappedIntent(),
            null,
            emptyList(),
            builtin
        )

    fun toStoryStep(): StoryStep<StoryHandlerDefinition> = Step(this)

    override fun findNextSteps(story: StoryDefinitionConfiguration): List<String> =
        children.map { it.userSentence }
}