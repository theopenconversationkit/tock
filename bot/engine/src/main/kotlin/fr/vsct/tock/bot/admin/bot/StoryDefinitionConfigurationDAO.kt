/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.admin.bot

import java.time.Instant

/**
 * Manage [StoryDefinitionConfiguration] persistence.
 */
interface StoryDefinitionConfigurationDAO {

    fun getStoryDefinitionById(id: String): StoryDefinitionConfiguration?

    /**
     * Returns the timestamp of the last configured answers update for this bot.
     *
     * @return null if there is no configuration at all
     */
    fun getLastUpdateTimestamp(botId: String): Instant?

    fun getStoryDefinitions(botId: String): List<StoryDefinitionConfiguration>

    fun save(story: StoryDefinitionConfiguration)

    fun delete(story: StoryDefinitionConfiguration)
}