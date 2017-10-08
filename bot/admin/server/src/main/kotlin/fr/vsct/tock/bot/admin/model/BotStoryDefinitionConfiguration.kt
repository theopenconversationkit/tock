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

package fr.vsct.tock.bot.admin.model

import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.admin.answer.SimpleAnswerConfiguration
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration
import fr.vsct.tock.bot.definition.Intent

/**
 *
 */
data class BotStoryDefinitionConfiguration(
        val storyId: String,
        val botId: String,
        val intent: Intent,
        val currentType: AnswerConfigurationType,
        val answers: List<BotSimpleAnswerConfiguration>,
        val _id: String? = null) {

    constructor(story: StoryDefinitionConfiguration) : this(
            story.storyId,
            story.botId,
            story.intent,
            story.currentType,
            story.answers.map { BotSimpleAnswerConfiguration(it as SimpleAnswerConfiguration) },
            story._id
    )
}