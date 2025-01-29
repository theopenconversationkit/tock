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

import ai.tock.genai.orchestratorcore.utils.OpenSearchUtils

data class OpenSearchVectorStoreSetting<T>(
    override val host: String,
    override val port: Int,
    override val username: String,
    override val password: T,
) : VectorStoreSettingBase<T>(
    provider = VectorStoreProvider.OpenSearch,
    host = host,
    port = port,
    username = username,
    password = password
) {

    override fun normalizeDocumentIndexName(namespace: String, botId: String, indexSessionId: String): String =
        OpenSearchUtils.normalizeDocumentIndexName(namespace, botId, indexSessionId)

    override fun getDocumentSearchParams(kNeighborsDocuments: Int): OpenSearchParams =
        OpenSearchParams(k = kNeighborsDocuments, filter = null)
}

data class OpenSearchParams(
    val k: Int,
    val filter: List<Term>? = null
) : DocumentSearchParamsBase(VectorStoreProvider.OpenSearch)

data class Term(
    val term: Map<String, Any>
)
