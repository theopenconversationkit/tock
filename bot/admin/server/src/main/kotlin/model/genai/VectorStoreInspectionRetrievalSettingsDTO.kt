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

package ai.tock.bot.admin.model.genai

import ai.tock.genai.orchestratorcore.models.compressor.DocumentCompressorProvider
import ai.tock.genai.orchestratorcore.models.vectorstore.DocumentSearchType

/**
 * The bot's effective retrieval settings, used to pre-fill the diagnostic form
 * and to flag divergences from the running chain. The compressor endpoint is
 * deliberately never exposed.
 *
 * [ragEnabled] tells whether the runtime applies this configuration at all:
 * the bot only loads an enabled RAG configuration.
 */
data class VectorStoreInspectionRetrievalSettingsDTO(
    val ragEnabled: Boolean,
    val searchType: DocumentSearchType,
    val maxDocumentsRetrieved: Int,
    val compressionEnabled: Boolean,
    val minScore: Float? = null,
    val maxDocuments: Int? = null,
    val fillToMaxDocuments: Boolean? = null,
    val provider: DocumentCompressorProvider? = null,
    val model: String? = null,
)
