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

package ai.tock.genai.orchestratorcore.utils

import ai.tock.genai.orchestratorcore.models.vectorstore.*
import ai.tock.shared.intProperty
import ai.tock.shared.property

private val vectorStore = property(
    name = "tock_gen_ai_orchestrator_vector_store_provider",
    defaultValue = VectorStoreProvider.OpenSearch.name)

typealias DocumentIndexName = String

object VectorStoreUtils {

    fun getVectorStoreElements(
        namespace: String,
        botId: String,
        indexSessionId: String,
        kNeighborsDocuments: Int,
        vectorStoreSetting: VectorStoreSetting?,
    ): Pair<DocumentSearchParamsBase, DocumentIndexName> {

        vectorStoreSetting?.let {
            val searchParams = it.getDocumentSearchParams(kNeighborsDocuments)
            val indexName = it.normalizeDocumentIndexName(namespace, botId, indexSessionId)
            return Pair(searchParams, indexName)
        }

        val (documentSearchParams, indexName) = when (vectorStore) {
            VectorStoreProvider.OpenSearch.name -> {
                OpenSearchParams(k = kNeighborsDocuments) to
                        OpenSearchUtils.normalizeDocumentIndexName(namespace, botId, indexSessionId)
            }
            VectorStoreProvider.PGVector.name -> {
                PGVectorParams(k = kNeighborsDocuments) to
                        PGVectorUtils.normalizeDocumentIndexName(namespace, botId, indexSessionId)
            }
            else -> throw IllegalArgumentException("Unsupported Vector Store Provider [$vectorStore]")
        }

        return Pair(documentSearchParams, indexName)
    }
}