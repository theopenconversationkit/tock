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

package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfigurationDAO
import mu.KotlinLogging
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.find
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.save

/**
 *
 */
object StoryDefinitionConfigurationMongoDAO : StoryDefinitionConfigurationDAO {

    private val logger = KotlinLogging.logger {}

    private val col = MongoBotConfiguration.database.getCollection<StoryDefinitionConfiguration>("story_configuration")

    init {
        col.ensureIndex("{botId:1}")
    }

    override fun getStoryDefinitionById(id: String): StoryDefinitionConfiguration? {
        return col.findOneById(id)
    }

    override fun getStoryDefinitions(botId: String): List<StoryDefinitionConfiguration> {
        return col.find("{'botId':${botId.json}}").toList()
    }

    override fun save(story: StoryDefinitionConfiguration) {
        col.save(story)
    }

    override fun delete(story: StoryDefinitionConfiguration) {
        col.deleteOneById(story._id)
    }
}