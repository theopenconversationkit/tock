/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.admin.bot

import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.admin.answer.ScriptAnswerConfiguration
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.StoryDefinition
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 * A [StoryDefinition] defined at runtime.
 */
data class StoryDefinitionConfiguration(
    /**
     * The story definition identifier.
     */
    val storyId: String,
    /**
     * The bot identifier.
     */
    val botId: String,
    /**
     * The target intent.
     */
    val intent: Intent,
    /**
     * The type of answer configuration.
     */
    val currentType: AnswerConfigurationType,
    /**
     * The answers available.
     */
    val answers: List<AnswerConfiguration>,
    /**
     * The version of the story.
     */
    val version: Int = 0,
    /**
     * The configuration identifier.
     */
    val _id: Id<StoryDefinitionConfiguration> = newId()
) {

    internal fun findCurrentAnswer(): AnswerConfiguration? =
        findAnswer(currentType)

    internal fun findAnswer(type: AnswerConfigurationType): AnswerConfiguration? =
        answers.firstOrNull { it.answerType == type }

    internal fun storyDefinition(): StoryDefinition? =
        (findCurrentAnswer() as? ScriptAnswerConfiguration)
            ?.findBestVersion(BotVersion.getCurrentBotVersion(botId))
            ?.storyDefinition
}