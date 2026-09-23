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

package ai.tock.genai.orchestratorclient.requests

import ai.tock.genai.orchestratorcore.models.em.EMSetting
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreSetting

data class KnowledgeBaseTargetRequest(
    val vectorStoreSetting: VectorStoreSetting?,
    val indexName: String,
    val indexNamePrefix: String,
)

data class KnowledgeBaseDocument(
    val entryId: String,
    val title: String,
    val searchHints: List<String>,
    val content: String,
    val sourceUrl: String?,
)

data class KnowledgeBaseIndexRequest(
    val vectorStoreSetting: VectorStoreSetting?,
    val indexName: String,
    val indexNamePrefix: String,
    val emSetting: EMSetting,
    val indexSessionId: String,
    val entries: List<KnowledgeBaseDocument>,
)

data class KnowledgeBaseDeletion(
    val entryId: String,
    val rowIds: List<String> = emptyList(),
)

data class KnowledgeBaseDeleteRequest(
    val vectorStoreSetting: VectorStoreSetting?,
    val indexName: String,
    val indexNamePrefix: String,
    val entries: List<KnowledgeBaseDeletion>,
)
