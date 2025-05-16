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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationMandatoryEntity
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition

data class BotStoryDefinitionConfigurationMandatoryEntity(
    /**
     * The role of the mandatory entity.
     */
    val role: String,
    /**
     * The type of mandatory entity.
     */
    val entityType: String,
    /**
     * The intent used to find the entities.
     */
    val intent: IntentWithoutNamespace,
    /**
     * The answers available.
     */
    val answers: List<BotAnswerConfiguration>,
    /**
     * The type of answer configuration.
     */
    val currentType: AnswerConfigurationType,
    /**
     * The category of the answers.
     */
    val category: String,
    /**
     * Entity defined by the entity role.
     */
    val entity: EntityDefinition? = null,
    /**
     * Intent defined by the intent name.
     */
    val intentDefinition: IntentDefinition? = null
) {

    constructor(story: StoryDefinitionConfiguration, e: StoryDefinitionConfigurationMandatoryEntity, readOnly: Boolean = false) :
        this(
            e.role,
            e.entityType,
            e.intent,
            e.answers.mapAnswers(story.userSentenceLocale, readOnly),
            e.currentType,
            story.category
        )
}
