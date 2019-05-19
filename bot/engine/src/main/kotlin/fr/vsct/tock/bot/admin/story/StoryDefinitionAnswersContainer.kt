package fr.vsct.tock.bot.admin.story

import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.admin.answer.ScriptAnswerConfiguration
import fr.vsct.tock.bot.admin.bot.BotVersion
import fr.vsct.tock.bot.definition.StoryDefinition

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

    fun findAnswer(type: AnswerConfigurationType): AnswerConfiguration? =
        answers.firstOrNull { it.answerType == type }

    fun storyDefinition(botId: String): StoryDefinition? =
        (findCurrentAnswer() as? ScriptAnswerConfiguration)
            ?.findBestVersion(BotVersion.getCurrentBotVersion(botId))
            ?.storyDefinition

    fun findNextSteps(story: StoryDefinitionConfiguration): List<String> = emptyList()
}