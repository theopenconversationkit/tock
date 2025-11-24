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

package ai.tock.bot.admin.story.dump

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.story.StoryDefinitionConfigurationMandatoryEntity
import ai.tock.bot.admin.story.dump.AnswerConfigurationDump.Companion.toDump
import ai.tock.bot.definition.IntentWithoutNamespace

data class StoryDefinitionConfigurationMandatoryEntityDump(
    /**
     * The role of the mandatory entity.
     */
    val role: String,
    /**
     * The type of mandatory entity.
     */
    val entityType: String = role,
    /**
     * The intent used to find the entities.
     */
    val intent: IntentWithoutNamespace,
    /**
     * The answers available.
     */
    val answers: List<AnswerConfigurationDump>,
    /**
     * The type of answer configuration.
     */
    val currentType: AnswerConfigurationType,
) {
    constructor(def: StoryDefinitionConfigurationMandatoryEntity) :
        this(
            def.role,
            def.entityType,
            def.intent,
            toDump(def.answers),
            def.currentType,
        )

    fun toEntity(controller: StoryDefinitionConfigurationDumpController): StoryDefinitionConfigurationMandatoryEntity =
        StoryDefinitionConfigurationMandatoryEntity(
            role,
            entityType,
            controller.checkIntent(intent)!!,
            answers.map { it.toAnswer(currentType, controller) },
            currentType,
        )
}
