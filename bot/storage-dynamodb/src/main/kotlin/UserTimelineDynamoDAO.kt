/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.storage.dynamodb

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.ArchivedEntityValue
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.Snapshot
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.engine.user.UserTimelineRuntimeDAO
import ai.tock.bot.mongo.ArchivedEntityValuesCol
import ai.tock.bot.mongo.ConnectorMessageCol
import ai.tock.bot.mongo.ConnectorMessageColId
import ai.tock.bot.mongo.DialogCol
import ai.tock.bot.mongo.NlpStatsCol
import ai.tock.bot.mongo.NlpStatsColId
import ai.tock.bot.mongo.SendSentenceNotYetLoaded
import ai.tock.bot.mongo.UserTimelineCol
import ai.tock.shared.booleanProperty
import ai.tock.shared.intProperty
import ai.tock.shared.jackson.AnyValueWrapper
import ai.tock.shared.longProperty
import ai.tock.storage.dynamodb.DynamoStore.GSI1
import ai.tock.storage.dynamodb.DynamoStore.GSI1_PK
import ai.tock.storage.dynamodb.DynamoStore.GSI1_SK
import ai.tock.storage.dynamodb.DynamoStore.runtimeTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.Put
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit.DAYS
import java.util.UUID

/**
 * Tier 2 (bot runtime) pattern. Everything that belongs to a dialog lives in the partition `DLG#<dialogId>`:
 *
 * | sk          | content                                                         |
 * |-------------|-----------------------------------------------------------------|
 * | `CM#<id>`   | connector messages of an action ([ConnectorMessageCol])          |
 * | `D#<n>`     | chunk n of the dialog document, when it exceeds one item         |
 * | `H`         | header + dialog document ([DialogCol]) when it fits in one item  |
 * | `NS#<id>`   | NLP stats of an action ([NlpStatsCol])                           |
 * | `SN#<date>` | one snapshot per turn (append only, instead of a growing array)  |
 *
 * so loading a dialog is one query, and removing it is one query plus batch deletes.
 * The Mongo indexes on arrays (playerIds, temporaryIds, client user ids) become pointer items,
 * because a DynamoDB index cannot index the elements of a set.
 *
 * Not ported in the spike: dialog_text, dialog flow stats, evaluations cleanup, user lock cleanup,
 * and the [ai.tock.bot.admin.user.UserReportDAO] / [ai.tock.bot.admin.dialog.DialogReportDAO] interfaces.
 */
object UserTimelineDynamoDAO : UserTimelineRuntimeDAO {
    private val logger = KotlinLogging.logger {}

    private val addNamespaceToTimelineId: Boolean = booleanProperty("tock_bot_add_namespace_to_timeline_id", false)
    private val maxActionsByDialog = intProperty("tock_bot_max_actions_by_dialog", 1000)
    private val dialogMaxValidityInSeconds = longProperty("tock_bot_dialog_max_validity_in_seconds", 60 * 60 * 24)
    private val dialogTtlDays = longProperty("tock_bot_dialog_index_ttl_days", 7)
    private val timelineTtlDays = longProperty("tock_bot_timeline_index_ttl_days", 365)

    /** Above this size (gzip BSON), the dialog document is split into chunks. The DynamoDB item limit is 400 KB. */
    internal var maxDocumentBytesInHeader = 350_000
    private const val CHUNK_BYTES = 350_000

    private const val HEADER = "H"
    private const val NS = "ns"
    private const val LAST_UPDATE = "lastUpdate"
    private const val CHUNKS = "chunks"
    private const val PLAYER_IDS = "playerIds"
    private const val GROUP_ID = "groupId"
    private const val DIALOG_ID = "dialogId"

    private fun timelineId(
        userId: String,
        namespace: String,
    ): String = if (addNamespaceToTimelineId) "_${namespace}_$userId" else userId

    private fun timelinePk(timelineId: String) = "TL#$timelineId"

    private fun dialogPk(dialogId: Id<Dialog>) = "DLG#$dialogId"

    private fun playerDialogPk(
        namespace: String,
        playerId: String,
    ) = "PD#$namespace#$playerId"

    private fun groupDialogPk(
        namespace: String,
        groupId: String,
    ) = "GD#$namespace#$groupId"

    private fun temporaryIdPk(
        namespace: String,
        temporaryId: String,
    ) = "TMP#$namespace#$temporaryId"

    private fun clientPk(clientId: String) = "CL#$clientId"

    private fun ttl(
        from: Instant,
        days: Long,
    ): AttributeValue = n(from.plus(days, DAYS).epochSecond)

    // ---------------------------------------------------------------- timeline

    private fun loadTimelineCol(timelineId: String): Pair<UserTimelineCol, String?>? =
        DynamoStore.getItem(runtimeTable, timelinePk(timelineId), "TL")?.let {
            DocumentCodec.decode<UserTimelineCol>(it.bytes(DOC)) to it.str(NS)
        }

    private fun saveTimelineCol(col: UserTimelineCol) {
        val namespace = col.namespace ?: ""
        DynamoStore.putItem(
            runtimeTable,
            DynamoStore.key(timelinePk(col._id.toString()), "TL") +
                mapOf(DOC to b(DocumentCodec.encode(col)), NS to s(namespace), TTL to ttl(col.lastUpdateDate, timelineTtlDays)),
        )
        if (col.temporaryIds.isNotEmpty()) {
            DynamoStore.batchWrite(
                runtimeTable,
                puts =
                    col.temporaryIds.map {
                        DynamoStore.key(temporaryIdPk(namespace, it), col._id.toString()) + (TTL to ttl(col.lastUpdateDate, timelineTtlDays))
                    },
            )
        }
    }

    override suspend fun save(
        userTimeline: UserTimeline,
        namespace: String,
    ) = io { save(userTimeline, namespace, null) }

    override suspend fun save(
        userTimeline: UserTimeline,
        botDefinition: BotDefinition,
        asynchronousProcess: Boolean,
    ) = io { save(userTimeline, botDefinition.namespace, botDefinition) }

    /**
     * Same steps as the Mongo DAO, except that the "asynchronous" part is always synchronous:
     * on Lambda or Cloud Run (request billing), the process is frozen as soon as the response is sent.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun save(
        userTimeline: UserTimeline,
        namespace: String,
        botDefinition: BotDefinition?,
    ) {
        val timelineId = timelineId(userTimeline.playerId.id, namespace)
        val oldTimeline = loadTimelineCol(timelineId)?.first
        val newTimeline = UserTimelineCol(timelineId, namespace, userTimeline, oldTimeline)
        saveTimelineCol(newTimeline)

        val lastDialog = userTimeline.currentDialog
        if (lastDialog != null && lastDialog.actionsSize > maxActionsByDialog) {
            userTimeline.dialogs.add(Dialog.initFromDialog(lastDialog))
        }
        for (dialog in userTimeline.dialogs) {
            try {
                saveDialogCol(DialogCol(dialog, newTimeline))
            } catch (e: Exception) {
                logger.error(e) { "Dialog save failure: ${dialog.id}" }
            }
        }

        userTimeline.playerId.clientId?.let { clientId ->
            DynamoStore.putItem(runtimeTable, DynamoStore.key(clientPk(clientId), userTimeline.playerId.id))
        }
        for (dialog in userTimeline.dialogs) {
            addSnapshot(dialog)
            addArchivedValues(dialog)
            saveActionsSideData(dialog)
        }
    }

    // ---------------------------------------------------------------- dialog

    private fun saveDialogCol(dialog: DialogCol) {
        val namespace = dialog.namespace ?: ""
        val pk = dialogPk(dialog._id)
        val bytes = DocumentCodec.encode(dialog)
        val ttl = ttl(dialog.lastUpdateDate, dialogTtlDays)
        val userIds = dialog.playerIds.filter { it.type != PlayerType.bot }.map { it.id }
        val header =
            DynamoStore.key(pk, HEADER) +
                mapOf(
                    NS to s(namespace),
                    LAST_UPDATE to n(dialog.lastUpdateDate.toEpochMilli()),
                    TTL to ttl,
                    GSI1_PK to s("NSD#$namespace"),
                    GSI1_SK to s("${sortableDate(dialog.lastUpdateDate)}#${dialog._id}"),
                ) +
                (if (userIds.isEmpty()) emptyMap() else mapOf(PLAYER_IDS to AttributeValue.fromSs(userIds))) +
                (dialog.groupId?.let { mapOf(GROUP_ID to s(it)) } ?: emptyMap())

        if (bytes.size <= maxDocumentBytesInHeader) {
            DynamoStore.putItem(runtimeTable, header + (DOC to b(bytes)) + (CHUNKS to n(0)))
        } else {
            // chunks + header in one transaction (atomic for readers), up to 100 items / 4 MB
            val chunks = bytes.toList().chunked(CHUNK_BYTES).map { it.toByteArray() }
            check(chunks.size < 99) { "dialog ${dialog._id} too large: ${bytes.size} bytes" }
            val items =
                chunks.mapIndexed { index, chunk ->
                    DynamoStore.key(pk, "D#%04d".format(index)) + mapOf(DOC to b(chunk), TTL to ttl)
                } + (header + (CHUNKS to n(chunks.size)))
            DynamoStore.client
                .transactWriteItems { request ->
                    request
                        .transactItems(
                            items.map {
                                TransactWriteItem
                                    .builder()
                                    .put(
                                        Put
                                            .builder()
                                            .tableName(runtimeTable)
                                            .item(it)
                                            .build(),
                                    ).build()
                            },
                        ).returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                }.also { DynamoStore.recordWrite(it.consumedCapacity()) }
        }

        // pointers: player -> dialog, group -> dialog
        DynamoStore.batchWrite(
            runtimeTable,
            puts =
                userIds.map { DynamoStore.key(playerDialogPk(namespace, it), dialog._id.toString()) + mapOf(LAST_UPDATE to n(dialog.lastUpdateDate.toEpochMilli()), TTL to ttl) } +
                    listOfNotNull(
                        dialog.groupId?.let {
                            DynamoStore.key(groupDialogPk(namespace, it), dialog._id.toString()) + mapOf(LAST_UPDATE to n(dialog.lastUpdateDate.toEpochMilli()), TTL to ttl)
                        },
                    ),
        )
    }

    /** A dialog partition, without the snapshots. */
    private class StoredDialog(
        val col: DialogCol,
        val namespace: String,
        val items: List<Map<String, AttributeValue>>,
    )

    private fun loadStoredDialog(dialogId: String): StoredDialog? {
        val items =
            DynamoStore.queryAll(
                QueryRequest
                    .builder()
                    .tableName(runtimeTable)
                    .consistentRead(true)
                    .keyConditionExpression("#pk = :pk AND #sk < :sn")
                    .expressionAttributeNames(mapOf("#pk" to PK, "#sk" to SK))
                    .expressionAttributeValues(mapOf(":pk" to s("DLG#$dialogId"), ":sn" to s("SN#")))
                    .build(),
            )
        val header = items.firstOrNull { it.str(SK) == HEADER } ?: return null
        val chunks = header.long(CHUNKS) ?: 0
        val bytes =
            if (chunks == 0L) {
                header.bytes(DOC)
            } else {
                items
                    .filter { it.str(SK)!!.startsWith("D#") }
                    .sortedBy { it.str(SK) }
                    .take(chunks.toInt())
                    .fold(ByteArray(0)) { acc, item -> acc + item.bytes(DOC) }
            }
        return StoredDialog(DocumentCodec.decode(bytes), header.str(NS) ?: "", items)
    }

    /**
     * Converts the stored dialog and loads eagerly the connector messages and the NLP stats,
     * which the Mongo DAO loads lazily through [ai.tock.bot.mongo.UserTimelineMongoDAO].
     */
    private fun StoredDialog.toDialog(storyDefinitionProvider: (String) -> StoryDefinition): Dialog {
        val messages =
            items
                .filter { it.str(SK)!!.startsWith("CM#") }
                .associate { item ->
                    item.str(SK)!!.removePrefix("CM#") to
                        DocumentCodec.decode<ConnectorMessageCol>(item.bytes(DOC)).messages.mapNotNull { it?.value as? ConnectorMessage }
                }
        val stats =
            items
                .filter { it.str(SK)!!.startsWith("NS#") }
                .associate { item -> item.str(SK)!!.removePrefix("NS#") to DocumentCodec.decode<NlpStatsCol>(item.bytes(DOC)).stats }
        return col.toDialog(storyDefinitionProvider).also { dialog ->
            dialog.allActions().filterIsInstance<SendSentenceNotYetLoaded>().forEach { action ->
                action.setLoadedMessages(messages[action.id.toString()] ?: emptyList())
                action.nlpStats = stats[action.id.toString()]
            }
        }
    }

    private fun saveActionsSideData(dialog: Dialog) {
        val pk = dialogPk(dialog.id)
        val ttl = ttl(now(), dialogTtlDays)
        val puts = mutableListOf<Map<String, AttributeValue>>()
        dialog.allActions().forEach { action ->
            // the actions loaded from the store have already persisted their messages and stats:
            // unlike the Mongo DAO, they are not rewritten at each turn
            if (action is SendSentence && action !is SendSentenceNotYetLoaded) {
                if (action.messages.isNotEmpty()) {
                    val col = ConnectorMessageCol(ConnectorMessageColId(action.toActionId(), dialog.id), action.messages.map { AnyValueWrapper(it) })
                    puts += DynamoStore.key(pk, "CM#${action.id}") + mapOf(DOC to b(DocumentCodec.encode(col)), TTL to ttl)
                }
                action.nlpStats?.let { stats ->
                    val col = NlpStatsCol(NlpStatsColId(action.toActionId(), dialog.id), stats, stats.nlpQuery.namespace)
                    puts += DynamoStore.key(pk, "NS#${action.id}") + mapOf(DOC to b(DocumentCodec.encode(col)), TTL to ttl)
                }
            }
        }
        DynamoStore.batchWrite(runtimeTable, puts = puts)
    }

    private fun addSnapshot(dialog: Dialog) {
        val date = now()
        DynamoStore.putItem(
            runtimeTable,
            DynamoStore.key(dialogPk(dialog.id), "SN#${date.toEpochMilli().toString().padStart(15, '0')}#${UUID.randomUUID()}") +
                mapOf(DOC to b(DocumentCodec.encode(Snapshot(dialog))), TTL to ttl(date, dialogTtlDays)),
        )
    }

    private fun addArchivedValues(dialog: Dialog) {
        dialog.state.entityValues.values
            .filter { it.hasBeanUpdatedInBus }
            .forEach {
                val col = ArchivedEntityValuesCol(it.previousValues, it.stateValueId)
                DynamoStore.putItem(
                    runtimeTable,
                    DynamoStore.key("AEV#${col._id}", "AEV") + mapOf(DOC to b(DocumentCodec.encode(col)), TTL to ttl(now(), dialogTtlDays)),
                )
            }
    }

    private fun dialogIdsOfPlayer(
        namespace: String,
        playerId: String,
        validAfter: Instant? = null,
    ): List<Pair<String, Long>> =
        DynamoStore
            .queryPartition(runtimeTable, playerDialogPk(namespace, playerId), consistentRead = true)
            .map { it.str(SK)!! to (it.long(LAST_UPDATE) ?: 0) }
            .filter { validAfter == null || it.second > validAfter.toEpochMilli() }

    private fun loadLastValidDialog(
        namespace: String,
        userId: PlayerId,
        groupId: String?,
    ): StoredDialog? {
        val validAfter = now().minusSeconds(dialogMaxValidityInSeconds)
        val candidates =
            if (groupId != null) {
                DynamoStore
                    .queryPartition(runtimeTable, groupDialogPk(namespace, groupId), consistentRead = true)
                    .map { it.str(SK)!! to (it.long(LAST_UPDATE) ?: 0) }
                    .filter { it.second > validAfter.toEpochMilli() }
                    .takeIf { it.isNotEmpty() }
            } else {
                null
            } ?: dialogIdsOfPlayer(namespace, userId.id, validAfter)
        return candidates.maxByOrNull { it.second }?.let { loadStoredDialog(it.first) }
    }

    override suspend fun loadWithLastValidDialog(
        namespace: String,
        userId: PlayerId,
        priorUserId: PlayerId?,
        groupId: String?,
        storyDefinitionProvider: (String) -> StoryDefinition,
    ): UserTimeline =
        io {
            val timeline = loadWithoutDialogsSync(namespace, userId)
            safeLoad { loadLastValidDialog(namespace, userId, groupId)?.toDialog(storyDefinitionProvider) }?.let { timeline.dialogs.add(it) }

            if (priorUserId != null) {
                timeline.temporaryIds.add(priorUserId.id)
                loadTimelineCol(timelineId(priorUserId.id, namespace))?.first?.apply {
                    toUserTimeline().userState.flags.forEach { timeline.userState.flags.putIfAbsent(it.key, it.value) }
                }
                safeLoad { loadLastValidDialog(namespace, priorUserId, groupId)?.toDialog(storyDefinitionProvider) }
                    ?.let { timeline.dialogs.add(it.copy(it.playerIds + userId)) }
            }
            timeline
        }

    private fun <T> safeLoad(load: () -> T?): T? =
        try {
            load()
        } catch (e: Exception) {
            logger.error(e) { e.message }
            null
        }

    private fun loadWithoutDialogsSync(
        namespace: String,
        userId: PlayerId,
    ): UserTimeline =
        loadTimelineCol(timelineId(userId.id, namespace))
            ?.first
            ?.copy(playerId = userId)
            ?.toUserTimeline()
            ?: UserTimeline(userId)

    override suspend fun loadWithoutDialogs(
        namespace: String,
        userId: PlayerId,
    ): UserTimeline = io { loadWithoutDialogsSync(namespace, userId) }

    suspend fun loadByTemporaryIdsWithoutDialogs(
        namespace: String,
        temporaryIds: List<String>,
    ): List<UserTimeline> =
        io {
            temporaryIds
                .flatMap { tmp -> DynamoStore.queryPartition(runtimeTable, temporaryIdPk(namespace, tmp), consistentRead = true) }
                .mapNotNull { it.str(SK) }
                .distinct()
                .mapNotNull { loadTimelineCol(it) }
                .filter { (_, ns) -> ns == namespace }
                .map { (col, _) -> col.toUserTimeline() }
        }

    // ---------------------------------------------------------------- removal

    private fun removeDialog(dialogId: String) {
        val header = DynamoStore.getItem(runtimeTable, "DLG#$dialogId", HEADER)
        if (header != null) {
            val namespace = header.str(NS) ?: ""
            val pointers =
                (header[PLAYER_IDS]?.ss() ?: emptyList()).map { DynamoStore.key(playerDialogPk(namespace, it), dialogId) } +
                    listOfNotNull(header.str(GROUP_ID)?.let { DynamoStore.key(groupDialogPk(namespace, it), dialogId) })
            DynamoStore.batchWrite(runtimeTable, deleteKeys = pointers)
        }
        DynamoStore.deletePartition(runtimeTable, "DLG#$dialogId")
    }

    override suspend fun remove(
        namespace: String,
        playerId: PlayerId,
        clearLock: Boolean,
    ): Long =
        io {
            val dialogIds = dialogIdsOfPlayer(namespace, playerId.id).map { it.first }
            dialogIds.forEach { removeDialog(it) }
            val timelineId = timelineId(playerId.id, namespace)
            val timeline = loadTimelineCol(timelineId)?.takeIf { it.second == namespace }
            if (timeline != null) {
                DynamoStore.batchWrite(
                    runtimeTable,
                    deleteKeys =
                        listOf(DynamoStore.key(timelinePk(timelineId), "TL")) +
                            timeline.first.temporaryIds.map { DynamoStore.key(temporaryIdPk(namespace, it), timelineId) },
                )
            }
            if (clearLock) {
                logger.debug { "spike: the user lock is not stored in DynamoDB, nothing to clear" }
            }
            (if (timeline != null) 1L else 0L) + dialogIds.size
        }

    suspend fun removeClient(
        namespace: String,
        clientId: String,
    ) {
        val userIds = io { DynamoStore.queryPartition(runtimeTable, clientPk(clientId), consistentRead = true).mapNotNull { it.str(SK) } }
        userIds.forEach { remove(namespace, PlayerId(it)) }
        io { DynamoStore.deletePartition(runtimeTable, clientPk(clientId)) }
    }

    // ---------------------------------------------------------------- player id update

    override suspend fun updatePlayerId(
        namespace: String,
        oldPlayerId: PlayerId,
        newPlayerId: PlayerId,
    ): Long =
        io {
            val oldTimelineId = timelineId(oldPlayerId.id, namespace)
            val newTimelineId = timelineId(newPlayerId.id, namespace)
            val timelineUpdated =
                loadTimelineCol(oldTimelineId)?.first?.let {
                    saveTimelineCol(it.copy(_id = newTimelineId.toId(), playerId = newPlayerId))
                    if (oldTimelineId != newTimelineId) {
                        DynamoStore.deleteItem(runtimeTable, timelinePk(oldTimelineId), "TL")
                    }
                    1L
                } ?: 0L

            val dialogIds = dialogIdsOfPlayer(namespace, oldPlayerId.id).map { it.first }
            val newPlayer: JsonNode = DocumentCodec.mapper.valueToTree(newPlayerId)
            var updated = 0L
            dialogIds.forEach { dialogId ->
                val stored = loadStoredDialog(dialogId) ?: return@forEach
                // same update as the Mongo array filters, on the JSON tree of the document
                val document = DocumentCodec.tree(DocumentCodec.encode(stored.col)) as ObjectNode
                document.path("stories").forEach { story ->
                    story.path("actions").forEach { action ->
                        val a = action as ObjectNode
                        if (a.isPlayer("playerId", oldPlayerId.id)) a.set("playerId", newPlayer.deepCopy())
                        if (a.isPlayer("recipientId", oldPlayerId.id)) a.set("recipientId", newPlayer.deepCopy())
                    }
                }
                val playerIds = (document.get("playerIds") as? ArrayNode) ?: document.putArray("playerIds")
                playerIds.removeIf { it.path("id").asString() == oldPlayerId.id }
                if (playerIds.none { it.path("id").asString() == newPlayerId.id }) {
                    playerIds.add(newPlayer.deepCopy())
                }
                val updatedCol = DocumentCodec.fromTree(document, DialogCol::class.java)
                saveDialogCol(updatedCol.copy(lastUpdateDate = stored.col.lastUpdateDate))
                DynamoStore.deleteItem(runtimeTable, playerDialogPk(namespace, oldPlayerId.id), dialogId)
                updated++
            }
            newPlayerId.clientId?.let { DynamoStore.putItem(runtimeTable, DynamoStore.key(clientPk(it), newPlayerId.id)) }
            timelineUpdated + updated
        }

    private fun ObjectNode.isPlayer(
        field: String,
        id: String,
    ): Boolean = get(field)?.takeIf { it.isObject }?.path("id")?.asString() == id

    /** Fixed width, so that the lexicographic order of the sort key is the chronological order. */
    private fun sortableDate(date: Instant): String = date.toEpochMilli().toString().padStart(15, '0')

    // ---------------------------------------------------------------- queries

    suspend fun getClientDialogs(
        namespace: String,
        clientId: String,
        storyDefinitionProvider: (String) -> StoryDefinition,
    ): List<Dialog> =
        io {
            DynamoStore
                .queryPartition(runtimeTable, clientPk(clientId), consistentRead = true)
                .mapNotNull { it.str(SK) }
                .flatMap { dialogIdsOfPlayer(namespace, it) }
                .distinctBy { it.first }
                .sortedByDescending { it.second }
                .mapNotNull { loadStoredDialog(it.first) }
                .map { it.toDialog(storyDefinitionProvider) }
        }

    suspend fun getDialogsUpdatedFrom(
        namespace: String,
        from: Instant,
        storyDefinitionProvider: (String) -> StoryDefinition,
    ): List<Dialog> =
        io {
            DynamoStore
                .queryAll(
                    QueryRequest
                        .builder()
                        .tableName(runtimeTable)
                        .indexName(GSI1)
                        .keyConditionExpression("#pk = :pk AND #sk > :from")
                        .expressionAttributeNames(mapOf("#pk" to GSI1_PK, "#sk" to GSI1_SK))
                        // '~' is after every id character: strictly after [from], like the Mongo query
                        .expressionAttributeValues(mapOf(":pk" to s("NSD#$namespace"), ":from" to s("${sortableDate(from)}#~")))
                        .build(),
                ).mapNotNull { it.str(PK)?.removePrefix("DLG#") }
                .mapNotNull { loadStoredDialog(it) }
                .map { it.toDialog(storyDefinitionProvider) }
        }

    override suspend fun getSnapshots(dialogId: Id<Dialog>): List<Snapshot> =
        io {
            try {
                DynamoStore
                    .queryPartition(runtimeTable, dialogPk(dialogId), skPrefix = "SN#", consistentRead = true)
                    .map { DocumentCodec.decode<Snapshot>(it.bytes(DOC)) }
            } catch (e: Exception) {
                logger.error(e) { e.message }
                emptyList()
            }
        }

    suspend fun getLastStoryId(
        namespace: String,
        playerId: PlayerId,
    ): String? =
        io {
            safeLoad {
                loadLastValidDialog(namespace, playerId, null)
                    ?.col
                    ?.stories
                    ?.lastOrNull()
                    ?.storyDefinitionId
            }
        }

    override suspend fun getArchivedEntityValues(
        stateValueId: Id<EntityStateValue>,
        oldActionsMap: Map<Id<Action>, Action>,
    ): List<ArchivedEntityValue> =
        io {
            safeLoad {
                DynamoStore
                    .getItem(runtimeTable, "AEV#$stateValueId", "AEV")
                    ?.let { DocumentCodec.decode<ArchivedEntityValuesCol>(it.bytes(DOC)) }
                    ?.values
                    ?.map { it.toArchivedEntityValue(oldActionsMap) }
            } ?: emptyList()
        }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    /** Raw size of the stored dialog document, to measure the 400 KB constraint. */
    fun storedDialogSize(dialogId: Id<Dialog>): Int? = DynamoStore.getItem(runtimeTable, dialogPk(dialogId), HEADER)?.let { h -> if ((h.long(CHUNKS) ?: 0) == 0L) h.bytes(DOC).size else -1 }
}
