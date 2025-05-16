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

import org.litote.kmongo.Id

/**
 * Manage [StoryDefinitionConfiguration] persistence.
 */
interface StoryDefinitionConfigurationDAO {

    /**
     * Listen changes on story definitions.
     */
    fun listenChanges(listener: () -> Unit)

    fun getStoryDefinitionById(id: Id<StoryDefinitionConfiguration>): StoryDefinitionConfiguration?

    fun getRuntimeStorySettings(namespace: String, botId: String): List<StoryDefinitionConfiguration>

    fun getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(
        namespace: String,
        botId: String,
        intent: String
    ): StoryDefinitionConfiguration?

    fun getConfiguredStoriesDefinitionByNamespaceAndBotIdAndIntent(
        namespace: String,
        botId: String,
        intentNames: List<String>
    ): List<StoryDefinitionConfiguration>

    fun getStoryDefinitionByNamespaceAndBotIdAndIntent(
        namespace: String,
        botId: String,
        intent: String
    ): StoryDefinitionConfiguration?

    fun getStoryDefinitionByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String
    ): StoryDefinitionConfiguration?

    fun getStoryDefinitionsByNamespaceAndBotId(namespace: String, botId: String): List<StoryDefinitionConfiguration>
    fun getStoryDefinitionsByNamespaceAndBotIdWithFileAttached(namespace: String, botId: String): List<StoryDefinitionConfiguration>
    fun save(story: StoryDefinitionConfiguration)

    fun delete(story: StoryDefinitionConfiguration)

    fun deleteByNamespaceAndBotId(namespace: String, botId: String)

    /**
     * Search [StoryDefinitionConfigurationSummaryMinimumMetrics] implementations
     * @param request [StoryDefinitionConfigurationMinimalSummaryRequest] search request
     * @return list of [StoryDefinitionConfigurationSummaryMinimumMetrics]
     */
    fun searchStoryDefinitionSummaries(request: StoryDefinitionConfigurationMinimalSummaryRequest): List<StoryDefinitionConfigurationSummaryMinimumMetrics>

    /**
     * Search [StoryDefinitionConfigurationSummaryExtended]
     * @param request [StoryDefinitionConfigurationExtendedSummaryRequest] search request
     * @return list of [StoryDefinitionConfigurationSummaryExtended]
     */
    fun searchStoryDefinitionSummariesExtended(request: StoryDefinitionConfigurationExtendedSummaryRequest): List<StoryDefinitionConfigurationSummaryExtended>

    /**
     * Create the built-in stories if they don't exist yet.
     */
    fun createBuiltInStoriesIfNotExist(stories: List<StoryDefinitionConfiguration>)
}
