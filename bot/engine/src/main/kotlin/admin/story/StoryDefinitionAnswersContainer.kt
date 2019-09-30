package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.ScriptAnswerConfiguration
import ai.tock.bot.admin.bot.BotVersion
import ai.tock.bot.definition.StoryDefinition

/**
 * Contains list of [AnswerConfiguration].
 */
internal interface StoryDefinitionAnswersContainer {

    /**
     * The answers available.
     */
    val answers: List<AnswerConfiguration>
    /**
     * The type of answer configuration.
     */
    val currentType: AnswerConfigurationType

    fun findCurrentAnswer(): AnswerConfiguration? =
        findAnswer(currentType)

    /**
     * Is there a current answer ?
     */
    fun hasCurrentAnwser(): Boolean = findCurrentAnswer()?.hasAnswer() == true

    fun findAnswer(type: AnswerConfigurationType): AnswerConfiguration? =
        answers.firstOrNull { it.answerType == type }

    fun storyDefinition(botId: String): StoryDefinition? =
        (findCurrentAnswer() as? ScriptAnswerConfiguration)
            ?.findBestVersion(BotVersion.getCurrentBotVersion(botId))
            ?.storyDefinition

    fun findNextSteps(story: StoryDefinitionConfiguration): List<String> = emptyList()
}