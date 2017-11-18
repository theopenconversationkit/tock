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
import fr.vsct.tock.bot.definition.Intent
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 *
 */
data class StoryDefinitionConfiguration(
        val storyId: String,
        val botId: String,
        val intent: Intent,
        val currentType: AnswerConfigurationType,
        val answers: List<AnswerConfiguration>,
        val _id: Id<StoryDefinitionConfiguration> = newId()) {

    fun findCurrentAnswer(): AnswerConfiguration {
        return answers.first { it.answerType == currentType }
    }
}