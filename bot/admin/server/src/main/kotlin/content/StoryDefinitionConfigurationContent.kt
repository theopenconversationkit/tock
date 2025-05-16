/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

package ai.tock.bot.admin.content

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.model.BotConfiguredAnswer
import ai.tock.bot.admin.model.BotConfiguredSteps
import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.model.BotStoryDefinitionConfigurationMandatoryEntity
import ai.tock.bot.admin.model.BotStoryDefinitionConfigurationStep
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.StoryTag
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale
import ai.tock.nlp.front.shared.config.ApplicationDefinition

data class StoryDefinitionConfigurationContent(
        /**
         * The story definition identifier.
         */
        val storyId: String,

        /**
         * The target main intent.
         */
        val intent: IntentWithoutNamespace = IntentWithoutNamespace(storyId),
        /**
         * The type of answer configuration.
         */
        val currentType: AnswerConfigurationType = AnswerConfigurationType.simple,
        /**
         * The answers available.
         */
        val answers: List<AnswerConfigurationContent>,
        /**
         * The version of the story.
         */
        val version: Int = 0,
        /**
         * The mandatory entities.
         */
        val mandatoryEntities: List<BotStoryDefinitionConfigurationMandatoryEntity> = emptyList(),
        /**
         * The optional steps.
         */
        val steps: List<StoryDefinitionConfigurationStepContent> = emptyList(),
        /**
         * The name of the story.
         */
        val name: String = storyId,
        /**
         * The category of the story.
         */
        val category: String = "default",
        /**
         * The description of the story.
         */
        val description: String = "",
        /**
         * The user sentence sample.
         */
        val userSentence: String = "",
        /**
         * The user sentence sample locale.
         */
        val userSentenceLocale: Locale? = null,
        /**
         * The configuration name if any.
         */
        val configurationName: String? = null,
        /**
         * Current features.
         */
        val features: List<StoryDefinitionConfigurationFeature> = emptyList(),
        /**
         * The configuration identifier.
         */
        val _id: Id<StoryDefinitionConfiguration> = newId(),
        /**
         * The story definition tags that specify different story types or roles.
         */
        val tags: Set<StoryTag> = emptySet(),
        /**
         * Answers by bot application configuration
         */
        val configuredAnswers: List<BotConfiguredAnswer> = emptyList(),
        /**
         * Steps by bot application configuration
         */
        val configuredSteps: List<BotConfiguredSteps> = emptyList(),

        /**
         * To filter/re-qualify next intents
         */
        val nextIntentsQualifiers: List<NlpIntentQualifier> = emptyList()
) {
    fun toBotStoryDefinitionConfiguration(namespace: String, applicationId:Id<ApplicationDefinition>, botId: String, locale: Locale): BotStoryDefinitionConfiguration =
            BotStoryDefinitionConfiguration(
                    storyId = storyId,
                    botId = botId,
                    intent = intent,
                    currentType = currentType,
                    namespace = namespace,
                    answers = answers.map { it.toBotAnswerConfiguration(namespace, locale) },
                    mandatoryEntities = mandatoryEntities,
                    steps = steps.map { it.toBotStoryDefinitionConfigurationStep(namespace, applicationId, category, locale) },
                    name = name,
                    category = category,
                    description = description,
                    userSentence = userSentence,
                    userSentenceLocale = userSentenceLocale ?: locale,
                    configurationName = configurationName,
                    features = features,
                    tags = tags,
                    configuredAnswers = configuredAnswers,
                    configuredSteps = configuredSteps,
                    nextIntentsQualifiers = nextIntentsQualifiers,
            )
}
