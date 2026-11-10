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

import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntryStatus
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobType
import ai.tock.bot.admin.model.genai.VectorStoreDeleteChunkRequestDTO
import ai.tock.bot.admin.model.genai.VectorStoreEmbeddingModelRequestDTO
import ai.tock.bot.admin.model.genai.VectorStoreIndexMetadataDTO
import ai.tock.bot.admin.model.genai.VectorStoreMutationDTO
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseBulkStatus
import ai.tock.genai.orchestratorclient.requests.VectorStoreChunkTargetRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreDeleteChunkRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreDeleteIndexRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreIndexTargetRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionCapabilitiesRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreSetEmbeddingModelRequest
import ai.tock.genai.orchestratorclient.services.VectorStoreInspectionService
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreProvider
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreSetting
import ai.tock.shared.exception.error.ErrorMessageWrapper
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import io.netty.handler.codec.http.HttpResponseStatus

/** The Studio supplies only identifiers; ownership, current-index status and KB origin are resolved here. */
class VectorStoreManagementAdminService(
    private val inspection: VectorStoreInspectionService = injector.provide(),
    private val knowledgeBase: KnowledgeBaseService = KnowledgeBaseService.default,
) {
    companion object {
        val default by lazy { VectorStoreManagementAdminService() }
    }

    private data class Target(
        val setting: VectorStoreSetting?,
        val prefix: String,
        val current: String?,
    )

    private fun target(
        namespace: String,
        botId: String,
        indexName: String,
    ): Target {
        val setting = VectorStoreService.getVectorStoreConfiguration(namespace, botId, enabled = true)?.setting
        val prefix = VectorStoreIndexLifecycle.indexName(namespace, botId, "", setting)
        if (!VectorStoreIndexLifecycle.belongsToBot(indexName, prefix)) fail("index_outside_bot")
        // Resolve the environment fallback too: a null setting does not necessarily mean PGVector.
        if (inspection
                .getCapabilities(
                    VectorStoreInspectionCapabilitiesRequest(setting),
                )?.provider != VectorStoreProvider.PGVector
        ) {
            fail("unsupported_provider")
        }
        return Target(setting, prefix, VectorStoreIndexLifecycle.currentIndexName(namespace, botId, setting))
    }

    fun metadata(
        namespace: String,
        botId: String,
        indexName: String,
    ): VectorStoreIndexMetadataDTO {
        val target = target(namespace, botId, indexName)
        val result = inspection.getIndexMetadata(VectorStoreIndexTargetRequest(target.setting, target.prefix, indexName))
        checkError(result.error)
        return metadataDTO(namespace, botId, indexName == target.current, result.metadata)
    }

    fun setEmbeddingModel(
        namespace: String,
        botId: String,
        request: VectorStoreEmbeddingModelRequestDTO,
    ): VectorStoreIndexMetadataDTO =
        VectorStoreIndexLifecycle.withLock(namespace, botId) {
            val target = target(namespace, botId, request.indexName)
            val model = request.embeddingModel.trim()
            if (model.isEmpty() || model.length > 300) fail("invalid_model")
            val result =
                inspection.setEmbeddingModel(
                    VectorStoreSetEmbeddingModelRequest(target.setting, target.prefix, request.indexName, model, request.expectedEmbeddingModel),
                )
            checkError(result.error)
            metadataDTO(namespace, botId, request.indexName == target.current, result.metadata)
        }

    private fun metadataDTO(
        namespace: String,
        botId: String,
        current: Boolean,
        metadata: Map<String, Any?>,
    ): VectorStoreIndexMetadataDTO {
        val botModel = RAGService.getRAGConfiguration(namespace, botId)?.emSetting?.let(EmbeddingModelIdentity::normalized)
        val model = EmbeddingModelIdentity.normalizeCollectionModel(metadata["embedding_model"] as? String)
        return VectorStoreIndexMetadataDTO(metadata, current, botModel, current && EmbeddingModelIdentity.coherence(model, botModel) == EmbeddingCoherence.MISMATCH)
    }

    fun deleteChunk(
        namespace: String,
        botId: String,
        request: VectorStoreDeleteChunkRequestDTO,
        author: String,
    ): VectorStoreMutationDTO {
        val mutation = {
            val target = target(namespace, botId, request.indexName)
            val current = request.indexName == target.current
            if (current != request.expectedCurrent) fail("confirmation_changed")
            if (request.rowId.isBlank()) fail("chunk_not_found")
            val chunk = inspection.getChunk(VectorStoreChunkTargetRequest(target.setting, target.prefix, request.indexName, request.rowId))
            checkError(chunk.error)
            if (chunk.chunkCount == 1 && !request.allowEmptyIndex) fail("confirmation_changed")
            val unpublish = current && chunk.metadata["source_type"] == "internal_kb"
            if (request.unpublishKnowledgeBase != unpublish) fail("confirmation_changed")
            if (unpublish) {
                val entryId = (chunk.metadata["kb_entry_id"] as? String)?.takeIf { it.isNotBlank() } ?: fail("knowledge_base_entry_missing")
                // The existing KB operation validates ownership, keeps the source, and queues its vector removal.
                val job = knowledgeBase.bulk(namespace, botId, KnowledgeBaseBulkStatus(listOf(entryId), KnowledgeBaseEntryStatus.DRAFT), author)
                VectorStoreMutationDTO(jobId = job.id)
            } else {
                knowledgeBase.invalidateProjections(namespace, botId, request.indexName, target.setting, request.rowId)
                val result = inspection.deleteChunk(VectorStoreDeleteChunkRequest(target.setting, target.prefix, request.indexName, request.rowId, current, request.allowEmptyIndex))
                checkError(result.error)
                VectorStoreMutationDTO(deletedCount = result.deletedCount)
            }
        }
        // Unpublication only queues the existing KB operation and remains available while indexing runs.
        // The authoritative origin check above prevents this path from performing a direct deletion.
        return if (request.unpublishKnowledgeBase) {
            VectorStoreIndexLifecycle.withLock(namespace, botId, mutation)
        } else {
            VectorStoreIndexLifecycle.withProjectionLock(namespace, botId, mutation)
        }
    }

    fun deleteIndex(
        namespace: String,
        botId: String,
        indexName: String,
    ): VectorStoreMutationDTO =
        VectorStoreIndexLifecycle.withProjectionLock(namespace, botId) {
            val target = target(namespace, botId, indexName)
            if (indexName == target.current) fail("current_index_protected")
            // A CREATE_INDEX job may still be filling an index before activating it.
            if (knowledgeBase.dao.activeJobs(namespace, botId).any {
                    it.type == KnowledgeBaseJobType.CREATE_INDEX && it.indexSessionId?.let { session ->
                        VectorStoreIndexLifecycle.indexName(namespace, botId, session, target.setting) == indexName
                    } == true
                }
            ) {
                fail("index_busy")
            }
            knowledgeBase.invalidateProjections(namespace, botId, indexName, target.setting)
            val result = inspection.deleteIndex(VectorStoreDeleteIndexRequest(target.setting, target.prefix, indexName, target.current))
            checkError(result.error)
            VectorStoreMutationDTO(deletedCount = result.deletedCount)
        }

    private fun checkError(error: String?) {
        if (error != null) fail(error)
    }

    private fun fail(error: String): Nothing =
        throw RestException(
            ErrorMessageWrapper("vsi.management.errors.$error"),
            if (error.endsWith("not_found")) HttpResponseStatus.NOT_FOUND else HttpResponseStatus.CONFLICT,
        )
}
