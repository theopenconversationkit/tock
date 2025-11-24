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

package ai.tock.genai.orchestratorcore.models.vectorstore

import ai.tock.genai.orchestratorcore.mappers.VectorStoreSettingMapper
import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.shared.security.key.SecretKey
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "provider",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = OpenSearchVectorStoreSetting::class, name = Constants.OPEN_SEARCH),
    JsonSubTypes.Type(value = PGVectorStoreSetting::class, name = Constants.PG_VECTOR),
)
abstract class VectorStoreSettingBase<T>(
    val provider: VectorStoreProvider,
    open val host: String,
    open val port: Int,
    open val username: String,
    open val password: T,
) {
    /**
     * Normalize the document index name
     * @param namespace the namespace
     * @param botId the bot ID
     */
    abstract fun normalizeDocumentIndexName(
        namespace: String,
        botId: String,
        indexSessionId: String,
    ): String

    /**
     * Get search params (filter) params
     */
    abstract fun getDocumentSearchParams(kNeighborsDocuments: Int): DocumentSearchParamsBase
}

typealias VectorStoreSettingDTO = VectorStoreSettingBase<String>
typealias VectorStoreSetting = VectorStoreSettingBase<SecretKey>

// Extension functions for DTO conversion
fun VectorStoreSetting.toDTO(): VectorStoreSettingDTO = VectorStoreSettingMapper.toDTO(this)

abstract class DocumentSearchParamsBase(
    val provider: VectorStoreProvider,
)
