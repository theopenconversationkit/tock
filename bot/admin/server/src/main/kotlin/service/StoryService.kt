/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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
package ai.tock.bot.admin.service

import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.bot.bean.TickStory

/**
 * Service that manage the scenario functionality
 */
interface StoryService {

    /**
     * Get a tick story
     * @param namespace : the namespace
     * @param botId : the id of the bot
     * @param storyId : functional id of story to delete
     */
    fun getStoryByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String
    ): StoryDefinitionConfiguration?

    /**
     * Create a new tick story
     * @param namespace : the namespace
     * @param tickStory : the tick story to create
     */
    fun createTickStory(
        namespace: String,
        tickStory: TickStory
    )

    /**
     * Update the activation feature of story
     * @param namespace : the namespace
     * @param botId : the id of the bot
     * @param features : features to add to the story
     */
    fun updateActivateStoryFeatureByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String,
        feature: StoryDefinitionConfigurationFeature
    ): Boolean

    /**
     * Delete a tick story
     * @param namespace : the namespace
     * @param storyDefinitionConfigurationId : technical id of story to delete
     */
    fun deleteStoryByNamespaceAndStoryDefinitionConfigurationId(
        namespace: String,
        storyDefinitionConfigurationId: String
    ): Boolean

    /**
     * Delete a tick story
     * @param namespace : the namespace
     * @param botId : the id of the bot
     * @param storyId : functional id of story to delete
     */
    fun deleteStoryByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String
    ): Boolean

    // FIXME : Migrate all story methods here
}