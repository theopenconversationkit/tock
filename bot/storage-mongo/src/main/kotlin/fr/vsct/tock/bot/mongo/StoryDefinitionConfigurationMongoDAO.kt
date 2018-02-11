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
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import mu.KotlinLogging
import org.bson.Document
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.find
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.projection
import org.litote.kmongo.save
import org.litote.kmongo.sort
import org.litote.kmongo.withDocumentClass
import java.time.Instant

/**
 *
 */
internal object StoryDefinitionConfigurationMongoDAO : StoryDefinitionConfigurationDAO {

    private val logger = KotlinLogging.logger {}

    private data class StoryDefinitionConfigurationHistoryCol(
        val conf: StoryDefinitionConfiguration,
        val deleted: Boolean = false,
        val date: Instant = Instant.now()
    )

    private val col = database.getCollection<StoryDefinitionConfiguration>("story_configuration")
    private val historyCol =
        database.getCollection<StoryDefinitionConfigurationHistoryCol>("story_configuration_history")

    init {
        col.ensureIndex("{botId:1}")
        historyCol.ensureIndex("{'conf.botId':1}")
        historyCol.ensureIndex("{date:1}")
    }

    override fun getStoryDefinitionById(id: String): StoryDefinitionConfiguration? {
        return col.findOneById(id)
    }

    override fun getLastUpdateTimestamp(botId: String): Instant? =
        historyCol
            .withDocumentClass<Document>()
            .find("{'conf.botId':${botId.json}}")
            .sort("{date:1}")
            .projection("{date:1}")
            .limit(1)
            .firstOrNull()
            ?.getDate("date")
            ?.toInstant()

    override fun getStoryDefinitions(botId: String): List<StoryDefinitionConfiguration> {
        return col.find("{'botId':${botId.json}}").toList()
    }

    override fun save(story: StoryDefinitionConfiguration) {
        val previous = col.findOneById(story._id)
        val toSave =
            if (previous != null) {
                story.copy(version = previous.version + 1)
            } else {
                story
            }
        historyCol.save(StoryDefinitionConfigurationHistoryCol(toSave))
        col.save(toSave)
    }

    override fun delete(story: StoryDefinitionConfiguration) {
        val previous = col.findOneById(story._id)
        if (previous != null) {
            historyCol.save(StoryDefinitionConfigurationHistoryCol(previous, true))
        }
        col.deleteOneById(story._id)
    }
}