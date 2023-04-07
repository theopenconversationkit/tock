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
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import ai.tock.shared.injector
import ai.tock.shared.provide
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.toId

/**
 * Service that manage the scenario functionality
 */
object StoryService {


    private val logger: KLogger = KotlinLogging.logger {}
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    private val applicationDefinitionDAO: ApplicationDefinitionDAO get() = injector.provide()

    /**
     * Get a [StoryDefinitionConfiguration]
     * @param namespace : the namespace
     * @param botId : the id of the bot
     * @param storyId : functional id of story to delete
     */
    fun getStoryByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String
    ): StoryDefinitionConfiguration? = storyDefinitionDAO
        .getStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, botId, storyId)

    /**
     * Update the activation feature of story
     * @param namespace : the namespace
     * @param botId : the id of the bot
     * @param feature : feature to add to the story
     */
    fun updateActivateStoryFeatureByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String,
        feature: StoryDefinitionConfigurationFeature
    ): Boolean {
        val story = storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, botId, storyId)
        if (story != null) {
            val botConf = BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
            if (botConf != null) {
                storyDefinitionDAO.save(
                    story.copy(features = story.features.filterNot { it.isStoryActivationFeature() } + feature))
            }
        }
        return false
    }

    /**
     * Delete a [StoryDefinitionConfiguration]
     * @param namespace : the namespace
     * @param storyDefinitionConfigurationId : technical id of story to delete
     */
    fun deleteStoryByNamespaceAndStoryDefinitionConfigurationId(
        namespace: String,
        storyDefinitionConfigurationId: String
    ): Boolean {
        val story = storyDefinitionDAO.getStoryDefinitionById(storyDefinitionConfigurationId.toId())
        if (story != null) {
            val botConf = BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
            if (botConf != null) {
                storyDefinitionDAO.delete(story)
            }
        }
        return false
    }

    /**
     * Delete a [StoryDefinitionConfiguration]
     * @param namespace : the namespace
     * @param botId : the id of the bot
     * @param storyId : functional id of story to delete
     */
    fun deleteStoryByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String
    ): Boolean {
        val story = storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, botId, storyId)
        if (story != null) {
            val botConf = BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
            if (botConf != null) {
                storyDefinitionDAO.delete(story)
            }
        }
        return false
    }

    // FIXME : Migrate all story methods here
}