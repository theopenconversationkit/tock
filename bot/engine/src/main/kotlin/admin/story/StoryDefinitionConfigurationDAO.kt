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

package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfigurationType
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

    fun getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(namespace: String, botId: String, intent: String): StoryDefinitionConfiguration?

    fun getStoryDefinitionByNamespaceAndBotIdAndTypeAndIntent(namespace: String, botId: String, type: AnswerConfigurationType, intent: String): StoryDefinitionConfiguration?

    fun getStoryDefinitionByNamespaceAndBotIdAndTypeAndStoryId(namespace: String, botId: String, type: AnswerConfigurationType, storyId: String): StoryDefinitionConfiguration?

    fun getStoryDefinitionsByNamespaceAndBotId(namespace: String, botId: String): List<StoryDefinitionConfiguration>

    fun save(story: StoryDefinitionConfiguration)

    fun delete(story: StoryDefinitionConfiguration)

    /**
     * Create the built-in stories if they don't exist yet.
     */
    fun createBuiltInStoriesIfNotExist(stories: List<StoryDefinitionConfiguration>)
}