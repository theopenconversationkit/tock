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

import com.mongodb.client.model.IndexOptions
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import mu.KotlinLogging
import org.litote.kmongo.MongoOperator.gt
import org.litote.kmongo.MongoOperator.limit
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.MongoOperator.sort
import org.litote.kmongo.aggregate
import org.litote.kmongo.createIndex
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.save
import java.lang.Exception
import java.time.Instant

/**
 *
 */
object UserTimelineMongoDAO : UserTimelineDAO {

    private val logger = KotlinLogging.logger {}

    private val userTimelineCol = database.getCollection<UserTimelineCol>()
    private val dialogCol = database.getCollection<DialogCol>()

    init {
        userTimelineCol.createIndex("{playerId:1}", IndexOptions().unique(true))
        dialogCol.createIndex("{playerIds:1}")
        userTimelineCol.createIndex("{lastUpdateDate:1}")
        dialogCol.createIndex("{lastSentenceDate:1}")
    }

    override fun save(userTimeline: UserTimeline) {
        userTimelineCol.save(UserTimelineCol(userTimeline))
        val dialog = userTimeline.currentDialog()
        if (dialog != null) {
            dialogCol.save(DialogCol(dialog))
        }
    }


    override fun loadWithLastValidDialog(userId: PlayerId, storyDefinitionProvider: (String) -> StoryDefinition): UserTimeline {
        val timeline = loadWithoutDialogs(userId)

        //TODO not only the last one
        val dialog = loadLastValidDialog(userId, storyDefinitionProvider)
        if (dialog != null) {
            timeline.dialogs.add(dialog)
        }
        logger.trace { "timeline for user $userId : $timeline" }
        return timeline
    }

    private fun loadWithoutDialogs(userId: PlayerId): UserTimeline {
        val timeline = userTimelineCol.findOneById(userId.id)
        return if (timeline == null) {
            logger.debug { "no timeline for user $userId" }
            UserTimeline(userId)
        } else {
            timeline.toUserTimeline()
        }
    }

    private fun loadLastValidDialog(userId: PlayerId, storyDefinitionProvider: (String) -> StoryDefinition): Dialog? {
        return try {
            val dialog = dialogCol.aggregate<DialogCol>(
                    pipeline = """[
                                            {${match}:{userIds:${userId.json}, lastSentenceDate : {${gt} : ${Instant.now().minusSeconds(60 * 60 * 24).json}}}},
                                            {${sort}:{lastSentenceDate:-1}},
                                            {${limit}:1}
                                           ]"""
            ).firstOrNull()
            return dialog?.toDialog(storyDefinitionProvider)
        } catch(e: Exception) {
            logger.error(e.message, e)
            null
        }
    }
}