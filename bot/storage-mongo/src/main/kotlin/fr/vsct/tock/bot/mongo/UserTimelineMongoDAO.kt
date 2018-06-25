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
import com.mongodb.client.model.ReplaceOptions
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.admin.dialog.DialogReportQuery
import fr.vsct.tock.bot.admin.dialog.DialogReportQueryResult
import fr.vsct.tock.bot.admin.user.UserReportDAO
import fr.vsct.tock.bot.admin.user.UserReportQuery
import fr.vsct.tock.bot.admin.user.UserReportQueryResult
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.Snapshot
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.bot.mongo.ClientIdCol_.Companion.UserIds
import fr.vsct.tock.bot.mongo.DialogCol_.Companion.PlayerIds
import fr.vsct.tock.bot.mongo.DialogCol_.Companion.Stories
import fr.vsct.tock.bot.mongo.DialogCol_.Companion._id
import fr.vsct.tock.bot.mongo.DialogTextCol_.Companion.Date
import fr.vsct.tock.bot.mongo.DialogTextCol_.Companion.DialogId
import fr.vsct.tock.bot.mongo.DialogTextCol_.Companion.Text
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import fr.vsct.tock.bot.mongo.UserTimelineCol_.Companion.ApplicationIds
import fr.vsct.tock.bot.mongo.UserTimelineCol_.Companion.LastUpdateDate
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.AnyValueWrapper
import fr.vsct.tock.shared.longProperty
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.MongoOperator.and
import org.litote.kmongo.MongoOperator.gt
import org.litote.kmongo.MongoOperator.or
import org.litote.kmongo.MongoOperator.type
import org.litote.kmongo.`in`
import org.litote.kmongo.addEachToSet
import org.litote.kmongo.aggregate
import org.litote.kmongo.and
import org.litote.kmongo.bson
import org.litote.kmongo.descending
import org.litote.kmongo.descendingSort
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.gt
import org.litote.kmongo.json
import org.litote.kmongo.limit
import org.litote.kmongo.lt
import org.litote.kmongo.match
import org.litote.kmongo.regex
import org.litote.kmongo.replaceOneWithFilter
import org.litote.kmongo.save
import org.litote.kmongo.sort
import org.litote.kmongo.toId
import org.litote.kmongo.updateOneById
import org.litote.kmongo.upsert
import java.lang.Exception
import java.time.Instant
import java.time.Instant.now
import java.util.concurrent.TimeUnit.DAYS

/**
 *
 */
internal object UserTimelineMongoDAO : UserTimelineDAO, UserReportDAO, DialogReportDAO {

    //wrapper to workaround the 1024 chars limit for String indexes
    private fun textKey(text: String): String =
        if (text.length > 512) text.substring(0, Math.min(512, text.length)) else text

    private val logger = KotlinLogging.logger {}

    private val botConfiguration: BotApplicationConfigurationDAO by injector.instance()
    private val executor: Executor by injector.instance()

    private val userTimelineCol = database.getCollection<UserTimelineCol>("user_timeline")
    private val dialogCol = database.getCollection<DialogCol>("dialog")
    private val dialogTextCol = database.getCollection<DialogTextCol>("dialog_text")
    private val clientIdCol = database.getCollection<ClientIdCol>("client_id")
    private val connectorMessageCol = database.getCollection<ConnectorMessageCol>("connector_message")
    private val snapshotCol = database.getCollection<SnapshotCol>("dialog_snapshot")

    init {
        userTimelineCol.ensureUniqueIndex(UserTimelineCol_.PlayerId.id)
        userTimelineCol.ensureIndex(LastUpdateDate)
        dialogCol.ensureIndex(DialogCol_.PlayerIds.id)
        dialogCol.ensureIndex(DialogCol_.PlayerIds.clientId)
        dialogCol.ensureIndex(
            DialogCol_.LastUpdateDate,
            indexOptions = IndexOptions().expireAfter(longProperty("tock_bot_dialog_index_ttl_days", 7), DAYS)
        )
        dialogTextCol.ensureIndex(Text)
        dialogTextCol.ensureUniqueIndex(Text, DialogId)
        dialogTextCol.ensureIndex(
            Date,
            indexOptions = IndexOptions().expireAfter(longProperty("tock_bot_dialog_index_ttl_days", 7), DAYS)
        )
        connectorMessageCol.ensureIndex(
            "{date:1}",
            IndexOptions().expireAfter(longProperty("tock_bot_dialog_index_ttl_days", 7), DAYS)
        )
        connectorMessageCol.ensureIndex("{'_id.dialogId':1}")
        snapshotCol.ensureIndex(
            SnapshotCol_.LastUpdateDate,
            indexOptions = IndexOptions().expireAfter(longProperty("tock_bot_dialog_index_ttl_days", 7), DAYS)
        )
    }

    override fun save(userTimeline: UserTimeline) {
        logger.debug { "start to save timeline $userTimeline" }
        val oldTimeline = userTimelineCol.findOneById(userTimeline.playerId.id)
        logger.debug { "load old timeline $userTimeline" }
        val newTimeline = UserTimelineCol(userTimeline, oldTimeline)
        logger.debug { "create new timeline $userTimeline" }
        userTimelineCol.save(newTimeline)
        logger.debug { "timeline saved $userTimeline" }
        for (dialog in userTimeline.dialogs) {
            //TODO if dialog updated
            val dialogToSave = DialogCol(dialog, newTimeline)
            logger.debug { "dialog to save created $userTimeline" }
            dialogCol.save(dialogToSave)
            logger.debug { "dialog saved $userTimeline" }
        }
        executor.executeBlocking {
            if (userTimeline.playerId.clientId != null) {
                clientIdCol.updateOneById(
                    userTimeline.playerId.clientId!!,
                    addEachToSet(
                        UserIds,
                        listOf(userTimeline.playerId.id)
                    ),
                    upsert()
                )
            }
            for (dialog in userTimeline.dialogs) {
                addSnapshot(dialog)

                dialog.allActions().forEach {
                    when (it) {
                        is SendSentenceWithNotLoadedMessage -> if (it.loaded && it.messages.isNotEmpty()) {
                            saveConnectorMessage(it.toActionId(), dialog.id, it.messages)
                        }
                        is SendSentence -> if (it.messages.isNotEmpty()) {
                            saveConnectorMessage(it.toActionId(), dialog.id, it.messages)
                        }
                        else -> {/*do nothing*/
                        }
                    }
                }
            }
            val dialog = userTimeline.currentDialog()
            if (dialog != null) {
                dialog.allActions().lastOrNull { it.playerId.type == PlayerType.user }
                    ?.let { action ->
                        if (action is SendSentence && action.stringText != null) {
                            val text = textKey(action.stringText!!)
                            dialogTextCol.replaceOneWithFilter(
                                and(Text eq text, DialogId eq dialog.id),
                                DialogTextCol(text, dialog.id),
                                ReplaceOptions().upsert(true)
                            )
                        }
                    }
            }
        }
        logger.debug { "end saving timeline $userTimeline" }
    }

    private fun saveConnectorMessage(actionId: Id<Action>, dialogId: Id<Dialog>, messages: List<ConnectorMessage>) {
        connectorMessageCol.save(
            ConnectorMessageCol(
                ConnectorMessageColId(actionId, dialogId),
                messages.map { AnyValueWrapper(it) })
        )
    }

    internal fun loadConnectorMessage(actionId: Id<Action>, dialogId: Id<Dialog>): List<ConnectorMessage> {
        return try {
            connectorMessageCol.findOneById(ConnectorMessageColId(actionId, dialogId))
                ?.messages
                ?.mapNotNull { it?.value as? ConnectorMessage }
                    ?: emptyList()
        } catch (e: Exception) {
            logger.error(e)
            emptyList()
        }
    }

    override fun loadWithLastValidDialog(
        userId: PlayerId,
        priorUserId: PlayerId?,
        storyDefinitionProvider: (String) -> StoryDefinition
    ): UserTimeline {
        val timeline = loadWithoutDialogs(userId)

        loadLastValidDialog(userId, storyDefinitionProvider)?.apply { timeline.dialogs.add(this) }

        if (priorUserId != null) {
            //link timeline
            timeline.temporaryIds.add(priorUserId.id)

            //copy state
            userTimelineCol.findOneById(priorUserId.id)
                ?.apply {
                    toUserTimeline().userState.flags.forEach {
                        timeline.userState.flags.putIfAbsent(it.key, it.value)
                    }
                }

            //copy dialog
            loadLastValidDialog(priorUserId, storyDefinitionProvider)
                ?.apply {
                    timeline.dialogs.add(
                        copy(
                            playerIds + userId
                        )
                    )
                }
        }

        logger.trace { "timeline for user $userId : $timeline" }
        return timeline
    }

    override fun remove(playerId: PlayerId) {
        dialogCol.deleteMany(PlayerIds.id eq playerId.id)
        userTimelineCol.deleteOne(UserTimelineCol_.PlayerId.id eq playerId.id)
        MongoUserLock.deleteLock(playerId.id)
    }

    override fun removeClient(clientId: String) {
        clientIdCol.findOneById(clientId)?.userIds?.forEach { remove(PlayerId(it)) }
    }

    override fun loadWithoutDialogs(userId: PlayerId): UserTimeline {
        val timeline = userTimelineCol.findOneById(userId.id)?.copy(playerId = userId)
        return if (timeline == null) {
            logger.debug { "no timeline for user $userId" }
            UserTimeline(userId)
        } else {
            timeline.toUserTimeline()
        }
    }

    private fun loadLastValidDialogCol(userId: PlayerId): DialogCol? {
        return dialogCol.aggregate<DialogCol>(
            match(
                PlayerIds.id eq userId.id, LastUpdateDate gt now().minusSeconds(60 * 60 * 24)
            ),
            sort(
                descending(LastUpdateDate)
            ),
            limit(1)
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
                and(
                    ApplicationIds `in` applicationsIds.filter { it.isNotEmpty() },
                    if (name.isNullOrBlank()) null
                    else UserTimelineCol_.UserPreferences.lastName.regex(name!!.trim(), "i"),
                    if (from == null) null else LastUpdateDate gt from,
                    if (to == null) null else LastUpdateDate lt to,
                    if (flags.isEmpty()) null
                    else flags.flatMap {
                        "userState.flags.${it.key}".let { key ->
                            listOfNotNull(
                                if (it.value == null) null else "{'$key.value':${it.value!!.json}}",
                                "{$or:[{'$key.expirationDate':{$gt:${now().json}}},{'$key.expirationDate':{$type:10}}]}"
                            )
                        }
                    }.joinToString(",", "{$and:[", "]}").bson
                )
            logger.debug("user search query: $filter")
            val count = userTimelineCol.count(filter)
            if (count > start) {
                val list = userTimelineCol.find(filter)
                    .skip(start.toInt()).limit(size).descendingSort(LastUpdateDate).toList()
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
                    dialogTextCol.find(Text eq textKey(query.text!!.trim())).toList().map { it.dialogId }
                        .toSet()
                } else {
                    dialogTextCol
                        .find(Text.regex(textKey(query.text!!.trim()), "i"))
                        .toList()
                        .map { it.dialogId }
                        .toSet()
                }
            }
            if (dialogIds.isEmpty() && !query.text.isNullOrBlank()) {
                return DialogReportQueryResult(0, 0, 0, emptyList())
            }
            val filter = and(
                DialogCol_.ApplicationIds `in` applicationsIds.filter { it.isNotEmpty() },
                if (query.playerId == null) null else PlayerIds.id eq query.playerId!!.id,
                if (query.dialogId == null) null else _id eq query.dialogId!!.toId(),
                if (dialogIds.isEmpty()) null else _id `in` dialogIds,
                if (from == null) null else LastUpdateDate gt from,
                if (to == null) null else LastUpdateDate lt to,
                if (query.intentName.isNullOrBlank()) null else Stories.currentIntent.name_ eq query.intentName
            )
            logger.debug("dialog search query: $filter")
            val count = dialogCol.count(filter)
            if (count > start) {
                val list = dialogCol.find(filter)
                    .skip(start.toInt()).limit(size).descendingSort(LastUpdateDate).toList()
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
        storyDefinitionProvider: (String) -> StoryDefinition
    ): List<Dialog> {
        val ids = clientIdCol.findOneById(clientId)?.userIds ?: emptySet()
        return if (ids.isEmpty()) {
            emptyList()
        } else {
            dialogCol
                .find(PlayerIds.id `in` ids)
                .descendingSort(LastUpdateDate)
                .map { it.toDialog(storyDefinitionProvider) }
                .toList()
        }
    }

    override fun getDialogsUpdatedFrom(
        from: Instant,
        storyDefinitionProvider: (String) -> StoryDefinition
    ): List<Dialog> {
        return dialogCol
            .find(LastUpdateDate gt from)
            .map { it.toDialog(storyDefinitionProvider) }
            .toList()
    }

    private fun addSnapshot(dialog: Dialog) {
        val snapshot = Snapshot(
            dialog.state.currentIntent?.name,
            dialog.state.entityValues.values.mapNotNull { it.value })
        val existingSnapshot = snapshotCol.findOneById(dialog.id)
        if (existingSnapshot == null) {
            snapshotCol.insertOne(SnapshotCol(dialog.id, listOf(snapshot)))
        } else {
            snapshotCol.save(
                existingSnapshot.copy(
                    snapshots = existingSnapshot.snapshots + snapshot,
                    lastUpdateDate = now()
                )
            )
        }
    }

    override fun getSnapshots(dialogId: Id<Dialog>): List<Snapshot> {
        return snapshotCol.findOneById(dialogId)?.snapshots ?: emptyList()
    }
}
