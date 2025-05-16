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

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.model.BotStoryDefinitionConfigurationStep
import ai.tock.bot.admin.model.CreateI18nLabelRequest
import ai.tock.bot.definition.EntityStepSelection
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import org.litote.kmongo.Id
import java.util.Locale

class StoryDefinitionConfigurationStepContent(
        /**
         * The intent used to reach the step
         */
        val intent: IntentWithoutNamespace?,
        /**
         * The optional intent to switch to when the step is reached.
         */
        val targetIntent: IntentWithoutNamespace? = null,
        /**
         * The answers available.
         */
        val answers: List<AnswerConfigurationContent> = emptyList(),
        /**
         * The type of answer configuration.
         */
        val currentType: AnswerConfigurationType = AnswerConfigurationType.simple,
        /**
         * The user sentence sample.
         */
        val userSentence: String,
        /**
         * The children of the steps
         */
        val children: List<StoryDefinitionConfigurationStepContent> = emptyList(),
        /**
         * The level of the step.
         */
        val level: Int = 0,
        /**
         * Entity selection.
         */
        val entity: EntityStepSelection? = null,
) {

    fun toBotStoryDefinitionConfigurationStep(namespace: String, applicationId: Id<ApplicationDefinition>, category: String, locale: Locale): BotStoryDefinitionConfigurationStep =

            BotStoryDefinitionConfigurationStep(
                    name = "${intent?.name}_${level}",
                    intent = intent,
                    intentDefinition = intent?.let { BotAdminService.createOrGetIntent(namespace, it.name, applicationId, category) },
                    targetIntent = targetIntent,
                    targetIntentDefinition = targetIntent?.let { BotAdminService.createOrGetIntent(namespace, it.name, applicationId, category) },
                    answers = answers.map { it.toBotAnswerConfiguration(namespace, locale) },
                    currentType = currentType,
                    category = category,
                    userSentence = BotAdminService.createI18nRequest(
                            namespace,
                            CreateI18nLabelRequest(userSentence, locale, AnswerConfigurationType.builtin.name)
                    ),
                    children = children.map { it.toBotStoryDefinitionConfigurationStep(namespace, applicationId, category, locale) },
                    level = level,
                    entity = entity,
            )
}
