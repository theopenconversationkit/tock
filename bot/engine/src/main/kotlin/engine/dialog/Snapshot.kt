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

package ai.tock.bot.engine.dialog

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.engine.config.ConfiguredStoryDefinition
import ai.tock.nlp.api.client.model.Entity
import java.time.Instant
import java.time.Instant.now

/**
 * A "snapshot" is a readonly view of the state in the dialog, usually after a bot reply.
 */
data class Snapshot(
    val storyDefinitionId: String?,
    val intentName: String?,
    val step: String?,
    val entityValues: List<EntityValue>,
    val storyType: AnswerConfigurationType?,
    val storyName: String? = storyDefinitionId,
    val date: Instant = now()
) {

    constructor(dialog: Dialog) : this(
        dialog.currentStory?.definition?.id,
        dialog.state.currentIntent?.name,
        dialog.currentStory?.step,
        dialog.state.entityValues.values.mapNotNull { it.value },
        (dialog.currentStory?.definition as? ConfiguredStoryDefinition)?.answerType ?: AnswerConfigurationType.builtin,
        (dialog.currentStory?.definition as? ConfiguredStoryDefinition)?.name ?: dialog.currentStory?.definition?.id
    )

    /**
     * Does this value exist in the snapshot?
     */
    fun hasValue(entity: Entity): Boolean = getValue(entity) != null

    /**
     * Returns the value if it exists.
     */
    fun getValue(entity: Entity): EntityValue? = entityValues.firstOrNull { it.entity == entity }
}
