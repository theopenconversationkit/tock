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
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration_.Companion.BotId
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import fr.vsct.tock.bot.mongo.StoryDefinitionConfigurationHistoryCol_.Companion.Conf
import fr.vsct.tock.bot.mongo.StoryDefinitionConfigurationHistoryCol_.Companion.Date
import mu.KotlinLogging
import org.bson.Document
import org.litote.kmongo.Data
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.path
import org.litote.kmongo.projection
import org.litote.kmongo.save
import org.litote.kmongo.withDocumentClass
import java.time.Instant

/**
 *
 */
object StoryDefinitionConfigurationMongoDAO : StoryDefinitionConfigurationDAO {

    private val logger = KotlinLogging.logger {}

    @Data
    data class StoryDefinitionConfigurationHistoryCol(
        val conf: StoryDefinitionConfiguration,
        val deleted: Boolean = false,
        val date: Instant = Instant.now()
    )

    private val col = database.getCollection<StoryDefinitionConfiguration>("story_configuration")
    private val historyCol =
        database.getCollection<StoryDefinitionConfigurationHistoryCol>("story_configuration_history")

    init {
        col.ensureIndex(BotId)
        historyCol.ensureIndex(Conf.botId)
        historyCol.ensureIndex(Date)
    }

    override fun getStoryDefinitionById(id: String): StoryDefinitionConfiguration? {
        return col.findOneById(id)
    }

    override fun getLastUpdateTimestamp(botId: String): Instant? =
        historyCol
            .withDocumentClass<Document>()
            .find(Conf.botId eq botId)
            .ascendingSort(Date)
            .projection(Date)
            .limit(1)
            .firstOrNull()
            ?.getDate(Date.path())
            ?.toInstant()

    override fun getStoryDefinitions(botId: String): List<StoryDefinitionConfiguration> {
        return col.find(BotId eq botId).toList()
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