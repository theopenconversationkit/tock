/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.mongo

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.nlp.NlpCallStats
import org.litote.kmongo.Id

/**
 * The [DialogDataLoader] of the MongoDB storage: it reads the collections of [UserTimelineMongoDAO].
 */
internal class MongoDialogDataLoader : DialogDataLoader {
    override suspend fun loadConnectorMessage(
        actionId: Id<Action>,
        dialogId: Id<Dialog>,
    ): List<ConnectorMessage> = UserTimelineMongoDAO.loadConnectorMessage(actionId, dialogId)

    override suspend fun loadConnectorMessages(ids: List<ConnectorMessageColId>): Map<ConnectorMessageColId, List<ConnectorMessage>> = UserTimelineMongoDAO.loadConnectorMessages(ids)

    override suspend fun loadNlpStats(
        actionId: Id<Action>,
        dialogId: Id<Dialog>,
    ): NlpCallStats? = UserTimelineMongoDAO.loadNlpStats(actionId, dialogId)
}
