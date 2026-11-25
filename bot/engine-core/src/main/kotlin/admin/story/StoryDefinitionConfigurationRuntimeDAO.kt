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

import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import org.litote.kmongo.Id

/**
 * The part of [StoryDefinitionConfigurationDAO] that the bots use at runtime.
 *
 * A storage that only serves the bots (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [StoryDefinitionConfigurationDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface StoryDefinitionConfigurationRuntimeDAO {
    fun listenChanges(listener: () -> Unit)

    fun getStoryDefinitionByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String,
    ): StoryDefinitionConfiguration?

    fun getStoryDefinitionsByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ): List<StoryDefinitionConfiguration>

    fun delete(story: StoryDefinitionConfiguration)

    fun createBuiltInStoriesIfNotExist(stories: List<StoryDefinitionConfiguration>)

    companion object {
        /**
         * The bound [StoryDefinitionConfigurationRuntimeDAO], or the bound [StoryDefinitionConfigurationDAO] adapted to it.
         */
        fun fromInjector(): StoryDefinitionConfigurationRuntimeDAO =
            injector.provideOrDefault<StoryDefinitionConfigurationRuntimeDAO> { injector.provide<StoryDefinitionConfigurationDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [StoryDefinitionConfigurationRuntimeDAO].
 */
fun StoryDefinitionConfigurationDAO.asRuntimeDAO(): StoryDefinitionConfigurationRuntimeDAO = StoryDefinitionConfigurationRuntimeDAOAdapter(this)

private class StoryDefinitionConfigurationRuntimeDAOAdapter(
    private val dao: StoryDefinitionConfigurationDAO,
) : StoryDefinitionConfigurationRuntimeDAO {
    override fun listenChanges(listener: () -> Unit) = dao.listenChanges(listener)

    override fun getStoryDefinitionByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String,
    ): StoryDefinitionConfiguration? = dao.getStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, botId, storyId)

    override fun getStoryDefinitionsByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ): List<StoryDefinitionConfiguration> = dao.getStoryDefinitionsByNamespaceAndBotId(namespace, botId)

    override fun delete(story: StoryDefinitionConfiguration) = dao.delete(story)

    override fun createBuiltInStoriesIfNotExist(stories: List<StoryDefinitionConfiguration>) = dao.createBuiltInStoriesIfNotExist(stories)
}
