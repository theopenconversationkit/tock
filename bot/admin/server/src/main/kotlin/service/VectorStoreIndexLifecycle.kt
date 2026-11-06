/*
 * Copyright (C) 2017/2026 SNCF Connect & Tech
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

package ai.tock.bot.admin.service

import ai.tock.bot.engine.user.UserLock
import ai.tock.genai.orchestratorcore.models.vectorstore.DocumentSearchType
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreSetting
import ai.tock.genai.orchestratorcore.utils.VectorStoreUtils
import ai.tock.shared.injector
import ai.tock.shared.provide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/** Serializes index mutations with changes to the bot's configured index/store across admin instances. */
object VectorStoreIndexLifecycle {
    fun <T> withLock(
        namespace: String,
        botId: String,
        action: () -> T,
    ): T =
        runBlocking {
            injector.provide<UserLock>().withLock("vector-store-lifecycle:${KnowledgeBaseService.hash("$namespace/$botId")}") {
                withContext(Dispatchers.IO) { action() }
            }
        }

    fun indexName(
        namespace: String,
        botId: String,
        session: String,
        setting: VectorStoreSetting?,
    ): String = VectorStoreUtils.getVectorStoreElements(namespace, botId, session, 1, DocumentSearchType.SIMILARITY_SEARCH, setting).second

    fun currentIndexName(
        namespace: String,
        botId: String,
        setting: VectorStoreSetting?,
    ): String? =
        RAGService.getRAGConfiguration(namespace, botId)?.indexSessionId?.takeIf { it.isNotBlank() }?.let {
            indexName(namespace, botId, it, setting)
        }
}
