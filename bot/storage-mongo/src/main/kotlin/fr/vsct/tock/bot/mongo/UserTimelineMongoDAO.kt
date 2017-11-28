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
import com.mongodb.client.model.UpdateOptions
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.admin.dialog.DialogReportQuery
import fr.vsct.tock.bot.admin.dialog.DialogReportQueryResult
import fr.vsct.tock.bot.admin.user.UserReportDAO
import fr.vsct.tock.bot.admin.user.UserReportQuery
import fr.vsct.tock.bot.admin.user.UserReportQueryResult
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.longProperty
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.MongoOperator.addToSet
import org.litote.kmongo.MongoOperator.and
import org.litote.kmongo.MongoOperator.each
import org.litote.kmongo.MongoOperator.gt
import org.litote.kmongo.MongoOperator.limit
import org.litote.kmongo.MongoOperator.lt
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.MongoOperator.or
import org.litote.kmongo.MongoOperator.sort
import org.litote.kmongo.MongoOperator.type
import org.litote.kmongo.aggregate
import org.litote.kmongo.count
import org.litote.kmongo.deleteMany
import org.litote.kmongo.deleteOne
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.find
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.replaceOne
import org.litote.kmongo.save
import org.litote.kmongo.updateOneById
import java.lang.Exception
import java.time.Instant
import java.time.Instant.now
import java.util.concurrent.TimeUnit.DAYS

/**
 *
 */
internal object UserTimelineMongoDAO : UserTimelineDAO, UserReportDAO, DialogReportDAO {

    //wrapper to workaround the 1024 chars limit for String indexes
    private fun textKey(text: String): String
            = if (text.length > 512) text.substring(0, Math.min(512, text.length)) else text

    private val logger = KotlinLogging.logger {}

    private val botConfiguration: BotApplicationConfigurationDAO by injector.instance()
    private val executor: Executor by injector.instance()

    private val userTimelineCol = database.getCollection<UserTimelineCol>("user_timeline")
    private val dialogCol = database.getCollection<DialogCol>("dialog")
    private val dialogTextCol = database.getCollection<DialogTextCol>("dialog_text")
    private val clientIdCol = database.getCollection<ClientIdCol>("client_id")

    init {
        //TODO remove these in 0.8.0
        try {
            userTimelineCol.dropIndex("playerId_1")
            dialogCol.dropIndex("playerIds_1")
        } catch (e: Exception) {
            //ignore
        }
        //end TODO

        userTimelineCol.ensureIndex("{'playerId.id':1}", IndexOptions().unique(true))
        userTimelineCol.ensureIndex("{lastUpdateDate:1}")
        dialogCol.ensureIndex("{'playerIds.id':1}")
        dialogCol.ensureIndex("{'playerIds.clientId':1}")
        dialogCol.ensureIndex("{lastUpdateDate:1}", IndexOptions().expireAfter(longProperty("tock_bot_dialog_index_ttl_days", 7), DAYS))
        dialogTextCol.ensureIndex("{text:1}")
        dialogTextCol.ensureIndex("{text:1, dialogId:1}", IndexOptions().unique(true))
        dialogTextCol.ensureIndex("{date:1}", IndexOptions().expireAfter(longProperty("tock_bot_dialog_index_ttl_days", 7), DAYS))
    }

    override fun save(userTimeline: UserTimeline) {
        logger.debug { "start to save timeline $userTimeline" }
        val oldTimeline = userTimelineCol.findOneById(userTimeline.playerId.id)
        logger.debug { "load old timeline $userTimeline" }
        val newTimeline = UserTimelineCol(userTimeline, oldTimeline)
        logger.debug { "create new timeline $userTimeline" }
        userTimelineCol.save(newTimeline)
        logger.debug { "timeline saved $userTimeline" }
        if (userTimeline.playerId.clientId != null) {
            clientIdCol.updateOneById(
                    userTimeline.playerId.clientId!!,
                    "{ $addToSet: {userIds: { $each : [ ${userTimeline.playerId.id.json} ] } } }",
                    UpdateOptions().upsert(true))
        }
        for (dialog in userTimeline.dialogs) {
            //TODO if dialog updated
            val dialogToSave = DialogCol(dialog, newTimeline)
            logger.debug { "dialog to save created $userTimeline" }
            dialogCol.save(dialogToSave)
            logger.debug { "dialog saved $userTimeline" }
        }
        val dialog = userTimeline.currentDialog()
        if (dialog != null) {
            executor.executeBlocking {
                dialog.allActions().lastOrNull { it.playerId.type == PlayerType.user }
                        ?.let { action ->
                            if (action is SendSentence && action.stringText != null) {
                                val text = textKey(action.stringText!!)
                                dialogTextCol.replaceOne(
                                        "{text:${text.json}, dialogId:${dialog.id.json}}",
                                        DialogTextCol(text, dialog.id),
                                        UpdateOptions().upsert(true))
                            }
                        }
            }
        }
        logger.debug { "end saving timeline $userTimeline" }
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
        dialogCol.deleteMany("{'playerIds.id':${playerId.id.json}}")
        userTimelineCol.deleteOne("{'playerId.id':${playerId.id.json}}")
        MongoUserLock.deleteLock(playerId.id)
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
                pipeline = """[{${match}:{'playerIds.id':${userId.id.json}, lastUpdateDate : {${gt} : ${Instant.now().minusSeconds(60 * 60 * 24).json}}}},
                               {${sort}:{lastUpdateDate:-1}},
                               {${limit}:1}
                              ]"""
        ).firstOrNull()
    }

    private fun loadLastValidDialog(userId: PlayerId, storyDefinitionProvider: (String) -> StoryDefinition): Dialog? {
        return try {
            return loadLastValidDialogCol(userId)?.toDialog(storyDefinitionProvider)
        } catch (e: Exception) {
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
                                    listOfNotNull(
                                            if (it.value == null) null else "{'$key.value':${it.value!!.json}}",
                                            "{$or:[{'$key.expirationDate':{$gt:${now().json}}},{'$key.expirationDate':{$type:10}}]}"
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

    override fun search(query: DialogReportQuery): DialogReportQueryResult {
        with(query) {
            val applicationsIds =
                    botConfiguration
                            .getConfigurationsByNamespaceAndNlpModel(query.namespace, query.nlpModel)
                            .map { it.applicationId }
                            .distinct()
            val dialogIds = if (query.text.isNullOrBlank()) {
                emptySet()
            } else {
                if (query.exactMatch) {
                    dialogTextCol.find("{text:${textKey(query.text!!.trim()).json}}").toList().map { it.dialogId }.toSet()
                } else {
                    dialogTextCol.find("{text:/${textKey(query.text!!.trim())}/i}").toList().map { it.dialogId }.toSet()
                }
            }
            if (dialogIds.isEmpty() && !query.text.isNullOrBlank()) {
                return DialogReportQueryResult(0, 0, 0, emptyList())
            }
            val filter =
                    listOfNotNull(
                            "'applicationIds':{\$in:${applicationsIds.filter { it.isNotEmpty() }.json}}",
                            if (query.playerId == null) null else "'playerIds.id':${query.playerId!!.id.json}",
                            if (query.dialogId == null) null else "'_id':${query.dialogId!!.json}",
                            if (dialogIds.isEmpty()) null else "'_id':{\$in:${dialogIds.json}}",
                            if (from == null) null else "'lastUpdateDate':{$gt:${from!!.json}}",
                            if (to == null) null else "'lastUpdateDate':{$lt:${to!!.json}}",
                            if (query.intentName.isNullOrBlank()) null else "'stories.currentIntent.name':${query.intentName!!.json}"
                    ).joinToString(",", "{$and:[", "]}") {
                        "{$it}"
                    }
            logger.debug("dialog search query: $filter")
            val count = dialogCol.count(filter)
            if (count > start) {
                val list = dialogCol.find(filter)
                        .skip(start.toInt()).limit(size).sort(Sorts.descending("lastUpdateDate")).toList()
                return DialogReportQueryResult(count, start, start + size, list.map { it.toDialogReport() })
            } else {
                return DialogReportQueryResult(0, 0, 0, emptyList())
            }
        }
    }

    override fun getDialog(id: Id<Dialog>): DialogReport? {
        return dialogCol.findOneById(id)?.toDialogReport()
    }

    override fun getClientDialogs(
            clientId: String,
            storyDefinitionProvider: (String) -> StoryDefinition): List<Dialog> {
        val ids = clientIdCol.findOneById(clientId)?.userIds ?: emptySet()
        return if (ids.isEmpty()) {
            emptyList()
        } else {
            dialogCol
                    .find("{'playerIds.id': { \$in : ${ids.json} } }")
                    .sortedByDescending { it.lastUpdateDate }
                    .map { it.toDialog(storyDefinitionProvider) }
                    .toList()
        }
    }

    override fun getDialogsUpdatedFrom(from: Instant, storyDefinitionProvider: (String) -> StoryDefinition): List<Dialog> {
        return dialogCol
                .find("{'lastUpdateDate':{$gt:${from.json}}}")
                .map { it.toDialog(storyDefinitionProvider) }
                .toList()
    }

    override fun getDialogsUpdatedFromTo(from: Instant, to: Instant, storyDefinitionProvider: (String) -> StoryDefinition): List<Dialog> {

        val filter = listOf("'lastUpdateDate':{$gt:${from!!.json}}",
                "'lastUpdateDate':{$lt:${to!!.json}}")
                .joinToString(",", "{$and:[", "]}") {
                    "{$it}"
                }
        return dialogCol
                .find(filter)
                .map { it.toDialog(storyDefinitionProvider) }
                .toList()
    }
}
