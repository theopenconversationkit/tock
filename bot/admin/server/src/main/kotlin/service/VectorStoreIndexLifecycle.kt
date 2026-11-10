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

import ai.tock.bot.engine.user.LockAcquisitionException
import ai.tock.bot.engine.user.UserLock
import ai.tock.genai.orchestratorcore.models.vectorstore.DocumentSearchType
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreSetting
import ai.tock.genai.orchestratorcore.utils.VectorStoreUtils
import ai.tock.shared.exception.error.ErrorMessageWrapper
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import io.netty.handler.codec.http.HttpResponseStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Serializes index mutations with changes to the bot's configured index/store across admin instances.
 *
 * Locks are not reentrant. When several are needed, acquire them in this order:
 * projection worker -> vector-store lifecycle -> KB mutations. Never acquire the worker lock from [withLock].
 */
object VectorStoreIndexLifecycle {
    /** Excludes an in-flight worker while deleting stored rows and invalidating their Mongo projections. */
    fun <T> withProjectionLock(
        namespace: String,
        botId: String,
        action: () -> T,
    ): T =
        try {
            runBlocking {
                injector.provide<UserLock>().withLock("knowledge-base-projection-worker") {
                    withContext(Dispatchers.IO) { withLock(namespace, botId, action) }
                }
            }
        } catch (e: LockAcquisitionException) {
            throw RestException(ErrorMessageWrapper("vsi.management.errors.index_busy"), HttpResponseStatus.CONFLICT)
        }

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

    /** Session identifiers cannot contain the separator introducing another bot's session. */
    fun belongsToBot(
        indexName: String,
        prefix: String,
    ): Boolean {
        if (!indexName.startsWith(prefix)) return false
        val session = indexName.removePrefix(prefix)
        return session.isNotBlank() && !session.contains("_session_") && !session.contains("-session-")
    }

    fun currentIndexName(
        namespace: String,
        botId: String,
        setting: VectorStoreSetting?,
    ): String? =
        RAGService.getRAGConfiguration(namespace, botId)?.indexSessionId?.takeIf { it.isNotBlank() }?.let {
            indexName(namespace, botId, it, setting)
        }
}
