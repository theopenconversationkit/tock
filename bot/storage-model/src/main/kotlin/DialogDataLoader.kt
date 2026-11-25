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
import ai.tock.shared.InternalTockApi
import org.litote.kmongo.Id
import java.util.ServiceLoader

/**
 * Loads the data of the actions that a dialog loads on demand: the custom messages of the connectors and the
 * NLP statistics. Each storage provides it with the [ServiceLoader]: the MongoDB one reads its collections.
 */
@InternalTockApi
interface DialogDataLoader {
    suspend fun loadConnectorMessage(
        actionId: Id<Action>,
        dialogId: Id<Dialog>,
    ): List<ConnectorMessage>

    suspend fun loadConnectorMessages(ids: List<ConnectorMessageColId>): Map<ConnectorMessageColId, List<ConnectorMessage>>

    suspend fun loadNlpStats(
        actionId: Id<Action>,
        dialogId: Id<Dialog>,
    ): NlpCallStats?

    companion object {
        /** The loader of the storage on the classpath. */
        val current: DialogDataLoader by lazy {
            ServiceLoader.load(DialogDataLoader::class.java).firstOrNull()
                ?: error("No DialogDataLoader found: add a bot storage (tock-bot-storage-mongo...) to the classpath")
        }
    }
}
