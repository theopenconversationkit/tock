/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.genai.orchestratorcore.models.vectorstore

import ai.tock.genai.orchestratorcore.utils.PGVectorUtils

data class PGVectorStoreSetting<T>(
    override val host: String,
    override val port: Int,
    override val username: String,
    override val password: T,
    override val k: Int,
    val database: String,
) : VectorStoreSettingBase<T>(
    provider = VectorStoreProvider.PGVector,
    host = host,
    port = port,
    username = username,
    password = password,
    k = k
) {

    override fun normalizeDocumentIndexName(namespace: String, botId: String, indexSessionId: String): String =
        PGVectorUtils.normalizeDocumentIndexName(namespace, botId, indexSessionId)

    override fun getDocumentSearchParams(): PGVectorParams =
        PGVectorParams(k = k, filter = null)
}

data class PGVectorParams(
    val k: Int = 4,
    val filter: Map<String, String>? = null
) : DocumentSearchParamsBase(VectorStoreProvider.PGVector)