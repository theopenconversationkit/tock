/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.admin.model

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.BuiltInAnswerConfiguration
import ai.tock.bot.admin.answer.ScriptAnswerConfiguration
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.StoryTag
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

internal fun List<AnswerConfiguration>.mapAnswers(): List<BotAnswerConfiguration> =
    map {
        when (it) {
            is SimpleAnswerConfiguration -> BotSimpleAnswerConfiguration(it)
            is ScriptAnswerConfiguration -> BotScriptAnswerConfiguration(it)
            is BuiltInAnswerConfiguration -> BotBuiltinAnswerConfiguration(it)
            else -> error("unsupported conf $it")
        }
    }

/**
 * Story created/updated with Story Builder
 */
data class BotStoryDefinitionConfiguration(
    val storyId: String,
    val botId: String,
    val intent: IntentWithoutNamespace,
    val currentType: AnswerConfigurationType,
    val namespace: String,
    val answers: List<BotAnswerConfiguration>,
    val mandatoryEntities: List<BotStoryDefinitionConfigurationMandatoryEntity> = emptyList(),
    val steps: List<BotStoryDefinitionConfigurationStep> = emptyList(),
    val name: String = storyId,
    val category: String = "default",
    val description: String = "",
    /**
     * The user sentence sample.
     */
    val userSentence: String = "",
    val userSentenceLocale: Locale,
    val configurationName: String? = null,
    val features: List<StoryDefinitionConfigurationFeature> = emptyList(),
    val tags: List<StoryTag> = emptyList(),
    val configuredAnswers: List<BotConfiguredAnswer> = emptyList(),
    val configuredSteps: List<BotConfiguredSteps> = emptyList(),
    val _id: Id<StoryDefinitionConfiguration> = newId()
) {

    constructor(story: StoryDefinitionConfiguration, userLocale: Locale) : this(
        story.storyId,
        story.botId,
        story.intent,
        story.currentType,
        story.namespace,
        story.answers.mapAnswers(),
        story.mandatoryEntities.map { BotStoryDefinitionConfigurationMandatoryEntity(story, it) },
        story.steps.map { BotStoryDefinitionConfigurationStep(story, it) },
        story.name,
        story.category,
        story.description,
        story.userSentence,
        story.userSentenceLocale ?: userLocale,
        story.configurationName,
        story.features,
        story.tags,
        story.configuredAnswers.map { BotConfiguredAnswer(it) },
        story.configuredSteps.mapSteps(story),
        story._id
    )

}
