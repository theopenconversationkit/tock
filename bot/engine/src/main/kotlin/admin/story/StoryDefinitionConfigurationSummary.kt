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

package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.definition.IntentWithoutNamespace
import org.litote.kmongo.Id
import java.time.ZonedDateTime

/**
 * Summary of [StoryDefinitionConfiguration] with :
 * - botId
 * - description
 * - intent
 * - lastEdited
 */
data class StoryDefinitionConfigurationSummaryExtended(
    @Suppress("ktlint:standard:property-naming")
    override val _id: Id<StoryDefinitionConfiguration>,
    override val storyId: String,
    val botId: String,
    override val intent: IntentWithoutNamespace,
    override val currentType: AnswerConfigurationType,
    override val name: String = storyId,
    override val category: String = "default",
    val description: String = "",
    val lastEdited: ZonedDateTime? = null,
) : StoryDefinitionConfigurationSummaryBase

/**
 * Summary of [StoryDefinitionConfiguration] with :
 * - isMetricStory
 */
data class StoryDefinitionConfigurationSummaryMinimumMetrics(
    @Suppress("ktlint:standard:property-naming")
    override val _id: Id<StoryDefinitionConfiguration>,
    override val storyId: String,
    override val intent: IntentWithoutNamespace,
    override val currentType: AnswerConfigurationType,
    override val name: String = storyId,
    override val category: String = "default",
    val metricStory: Boolean,
) : StoryDefinitionConfigurationSummaryBase

interface StoryDefinitionConfigurationSummaryBase {
    @Suppress("ktlint:standard:property-naming")
    val _id: Id<StoryDefinitionConfiguration>
    val storyId: String
    val intent: IntentWithoutNamespace
    val currentType: AnswerConfigurationType
    val name: String
    val category: String
}
