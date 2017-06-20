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

import com.github.salomonbrys.kodein.instance
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Sorts
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.admin.user.UserReportDAO
import fr.vsct.tock.bot.admin.user.UserReportQuery
import fr.vsct.tock.bot.admin.user.UserReportQueryResult
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import mu.KotlinLogging
import org.litote.kmongo.MongoOperator.and
import org.litote.kmongo.MongoOperator.gt
import org.litote.kmongo.MongoOperator.limit
import org.litote.kmongo.MongoOperator.lt
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.MongoOperator.or
import org.litote.kmongo.MongoOperator.sort
import org.litote.kmongo.aggregate
import org.litote.kmongo.count
import org.litote.kmongo.createIndex
import org.litote.kmongo.deleteMany
import org.litote.kmongo.deleteOne
import org.litote.kmongo.find
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.save
import java.lang.Exception
import java.time.Instant
import java.time.Instant.now

/**
 *
 */
internal object UserTimelineMongoDAO : UserTimelineDAO, UserReportDAO, DialogReportDAO {

    private val logger = KotlinLogging.logger {}

    private val botConfiguration: BotApplicationConfigurationDAO by injector.instance()

    private val userTimelineCol = database.getCollection<UserTimelineCol>("user_timeline")
    private val dialogCol = database.getCollection<DialogCol>("dialog")

    init {
        userTimelineCol.createIndex("{playerId:1}", IndexOptions().unique(true))
        dialogCol.createIndex("{playerIds:1}")
        userTimelineCol.createIndex("{lastUpdateDate:1}")
        dialogCol.createIndex("{lastUpdateDate:1}")
    }

    override fun save(userTimeline: UserTimeline) {
        val oldTimeline = userTimelineCol.findOneById(userTimeline.playerId.id)
        userTimelineCol.save(UserTimelineCol(userTimeline, oldTimeline))
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

    override fun remove(playerId: PlayerId) {
        dialogCol.deleteMany("{playerIds:${playerId.json}}")
        userTimelineCol.deleteOne("{playerId:${playerId.json}}")
    }

    override fun loadWithoutDialogs(userId: PlayerId): UserTimeline {
        val timeline = userTimelineCol.findOneById(userId.id)
        return if (timeline == null) {
            logger.debug { "no timeline for user $userId" }
            UserTimeline(userId)
        } else {
            timeline.toUserTimeline()
        }
    }

    private fun loadLastValidDialogCol(userId: PlayerId): DialogCol? {
        return dialogCol.aggregate<DialogCol>(
                pipeline = """[
                                            {${match}:{playerIds:${userId.json}, lastUpdateDate : {${gt} : ${Instant.now().minusSeconds(60 * 60 * 24).json}}}},
                                            {${sort}:{lastUpdateDate:-1}},
                                            {${limit}:1}
                                           ]"""
        ).firstOrNull()
    }

    private fun loadLastValidDialog(userId: PlayerId, storyDefinitionProvider: (String) -> StoryDefinition): Dialog? {
        return try {
            return loadLastValidDialogCol(userId)?.toDialog(storyDefinitionProvider)
        } catch(e: Exception) {
            logger.error(e)
            null
        }
    }

    override fun search(query: UserReportQuery): UserReportQueryResult {
        with(query) {
            val applicationsIds =
                    botConfiguration
                            .getConfigurationsByNamespaceAndNlpModel(query.namespace, query.nlpModel)
                            .map { it.applicationId }
                            .distinct()
            val filter =
                    listOfNotNull(
                            "'applicationIds':{\$in:${applicationsIds.filter { it.isNotEmpty() }.json}}",
                            if (name.isNullOrBlank()) null else "'userPreferences.lastName':/${name!!.trim()}/i",
                            if (from == null) null else "'lastUpdateDate':{$gt:${from!!.json}}",
                            if (to == null) null else "'lastUpdateDate':{$lt:${to!!.json}}",
                            if (flags.isEmpty()) null
                            else flags.flatMap {
                                "userState.flags.${it.key}".let { key ->
                                    listOf(
                                            "{'$key.value':${it.value.json}}",
                                            "{$or:[{'$key.expirationDate':{$gt:${now().json}}},{'$key.expirationDate':null}]}"
                                    )
                                }
                            }.joinToString(",", "$and:[", "]")

                    ).joinToString(",", "{$and:[", "]}") {
                        "{$it}"
                    }
            logger.debug("user search query: $filter")
            val count = userTimelineCol.count(filter)
            if (count > start) {
                val list = userTimelineCol.find(filter)
                        .skip(start.toInt()).limit(size).sort(Sorts.descending("lastUpdateDate")).toList()
                return UserReportQueryResult(count, start, start + size, list.map { it.toUserReport() })
            } else {
                return UserReportQueryResult(0, 0, 0, emptyList())
            }
        }
    }

    override fun lastDialog(playerId: PlayerId): DialogReport {
        return loadLastValidDialogCol(playerId)?.toDialogReport() ?: DialogReport()
    }
}