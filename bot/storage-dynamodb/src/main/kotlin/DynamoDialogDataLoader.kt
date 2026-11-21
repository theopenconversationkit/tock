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
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.nlp.NlpCallStats
import ai.tock.bot.mongo.ConnectorMessageCol
import ai.tock.bot.mongo.ConnectorMessageColId
import ai.tock.bot.mongo.DialogDataLoader
import ai.tock.bot.mongo.NlpStatsCol
import ai.tock.storage.dynamodb.DynamoStore.runtimeTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.litote.kmongo.Id

/**
 * The [DialogDataLoader] of the DynamoDB storage: the custom messages and the NLP stats of an action are items
 * of the partition of its dialog (`CM#<actionId>`, `NS#<actionId>`).
 */
class DynamoDialogDataLoader : DialogDataLoader {
    override suspend fun loadConnectorMessage(
        actionId: Id<Action>,
        dialogId: Id<Dialog>,
    ): List<ConnectorMessage> =
        withContext(Dispatchers.IO) {
            DynamoStore
                .getItem(runtimeTable, "DLG#$dialogId", "CM#$actionId")
                ?.let { DocumentCodec.decode<ConnectorMessageCol>(it.bytes(DOC)).messages.mapNotNull { message -> message?.value as? ConnectorMessage } }
                ?: emptyList()
        }

    override suspend fun loadConnectorMessages(ids: List<ConnectorMessageColId>): Map<ConnectorMessageColId, List<ConnectorMessage>> =
        ids.associateWith { loadConnectorMessage(it.actionId, it.dialogId) }

    override suspend fun loadNlpStats(
        actionId: Id<Action>,
        dialogId: Id<Dialog>,
    ): NlpCallStats? =
        withContext(Dispatchers.IO) {
            DynamoStore.getItem(runtimeTable, "DLG#$dialogId", "NS#$actionId")?.let { DocumentCodec.decode<NlpStatsCol>(it.bytes(DOC)).stats }
        }
}
