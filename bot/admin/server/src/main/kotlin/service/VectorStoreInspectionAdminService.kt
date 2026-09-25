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

import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.model.genai.VectorStoreIndexDTO
import ai.tock.bot.admin.model.genai.VectorStoreIndexListDTO
import ai.tock.bot.admin.model.genai.VectorStoreInspectionCondenseRequestDTO
import ai.tock.bot.admin.model.genai.VectorStoreInspectionDocumentsRequestDTO
import ai.tock.bot.admin.model.genai.VectorStoreInspectionSearchRequestDTO
import ai.tock.bot.admin.model.genai.VectorStoreInspectionSearchResponseDTO
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionCapabilitiesRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionCompressionOverride
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionCondenseRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionDocumentsFilter
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionDocumentsRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionIndexesRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreInspectionSearchRequest
import ai.tock.genai.orchestratorclient.responses.VectorStoreCapabilitiesResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionCondenseResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionDocumentsResponse
import ai.tock.genai.orchestratorclient.services.VectorStoreInspectionService
import ai.tock.genai.orchestratorcore.models.vectorstore.DocumentSearchType
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreSetting
import ai.tock.genai.orchestratorcore.utils.VectorStoreUtils
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle

/** Resolves bot configuration before delegating read-only inspection to the orchestrator. */
object VectorStoreInspectionAdminService {
    private val inspectionService: VectorStoreInspectionService get() = injector.provide()

    fun getCapabilities(
        namespace: String,
        botId: String,
    ): VectorStoreCapabilitiesResponse? =
        inspectionService.getCapabilities(
            VectorStoreInspectionCapabilitiesRequest(vectorStoreSetting(namespace, botId)),
        )

    fun getIndexes(
        namespace: String,
        botId: String,
    ): VectorStoreIndexListDTO? {
        val vectorStoreSetting = vectorStoreSetting(namespace, botId)
        val response =
            inspectionService.getIndexes(
                VectorStoreInspectionIndexesRequest(
                    vectorStoreSetting = vectorStoreSetting,
                    indexNamePrefix = indexNamePrefix(namespace, botId, vectorStoreSetting),
                ),
            ) ?: return null
        val currentSessionId = RAGService.getRAGConfiguration(namespace, botId)?.indexSessionId
        return VectorStoreIndexListDTO(
            response.indexes.map { index ->
                VectorStoreIndexDTO(
                    indexName = index.indexName,
                    indexSessionId = index.indexSessionId,
                    indexDatetime = index.indexDatetime,
                    documentCount = index.documentCount,
                    chunkCount = index.chunkCount,
                    isCurrent = index.indexSessionId == currentSessionId,
                )
            },
        )
    }

    fun getDocuments(
        request: VectorStoreInspectionDocumentsRequestDTO,
        namespace: String,
        botId: String,
    ): VectorStoreInspectionDocumentsResponse? {
        val vectorStoreSetting = vectorStoreSetting(namespace, botId)
        return inspectionService.getDocuments(
            VectorStoreInspectionDocumentsRequest(
                vectorStoreSetting = vectorStoreSetting,
                indexNamePrefix = indexNamePrefix(namespace, botId, vectorStoreSetting),
                indexName = request.indexName,
                filter =
                    request.filter?.let {
                        VectorStoreInspectionDocumentsFilter(
                            text = it.text,
                            documentId = it.documentId,
                            anomaly = it.anomaly,
                        )
                    },
                start = request.start,
                size = request.size,
                includeStats = request.includeStats,
                includeChunks = request.includeChunks,
            ),
        )
    }

    fun condense(
        request: VectorStoreInspectionCondenseRequestDTO,
        namespace: String,
        botId: String,
    ): VectorStoreInspectionCondenseResponse? {
        val rag = ragConfiguration(namespace, botId)
        val businessRulesConfiguration = BusinessRulesService.getBusinessRulesConfiguration(namespace, botId)
        return inspectionService.condense(
            VectorStoreInspectionCondenseRequest(
                questionCondensingLlmSetting = rag.questionCondensingLlmSetting,
                questionCondensingPrompt =
                    rag.questionCondensingPrompt.copy(
                        inputs =
                            mapOf(
                                "lexicon_groups" to businessRulesConfiguration?.lexiconGroups.orEmpty().map { it.terms },
                            ),
                    ),
                question = request.question,
            ),
        )
    }

    fun search(
        request: VectorStoreInspectionSearchRequestDTO,
        namespace: String,
        botId: String,
    ): VectorStoreInspectionSearchResponseDTO? {
        val rag = ragConfiguration(namespace, botId)
        val vectorStoreSetting = vectorStoreSetting(namespace, botId)
        val compressorSetting =
            if (request.compressionEnabled) {
                DocumentCompressorService.getDocumentCompressorConfiguration(namespace, botId)?.setting
                    ?: WebVerticle.badRequest(
                        "No Document Compressor configuration is defined yet " +
                            "[namespace: $namespace, botId: $botId]",
                    )
            } else {
                null
            }
        val response =
            inspectionService.search(
                VectorStoreInspectionSearchRequest(
                    vectorStoreSetting = vectorStoreSetting,
                    embeddingQuestionEmSetting = rag.emSetting,
                    compressorSetting = compressorSetting,
                    indexNamePrefix = indexNamePrefix(namespace, botId, vectorStoreSetting),
                    indexName = request.indexName,
                    searchType = request.searchType,
                    query = request.query,
                    keyWords = request.keyWords,
                    fetchK = request.fetchK,
                    k = request.k,
                    compressionEnabled = request.compressionEnabled,
                    compressionStage = request.compressionStage.toInternalCompressionStage(),
                    compressionOverride =
                        request.compressionOverride?.let {
                            VectorStoreInspectionCompressionOverride(
                                minScore = it.minScore,
                                maxDocuments = it.maxDocuments,
                                fillToMaxDocuments = it.fillToMaxDocuments,
                            )
                        },
                    pinnedChunkIds = request.pinnedChunkIds,
                    pinnedRankStrategy = request.pinnedRankStrategy,
                ),
            ) ?: return null
        return VectorStoreInspectionSearchResponseDTO(
            funnel = response.funnel,
            compressionStage = response.compressionStage.toPublicCompressionStage(),
            results = response.results,
            duration = response.duration,
        )
    }

    private fun vectorStoreSetting(
        namespace: String,
        botId: String,
    ): VectorStoreSetting? = VectorStoreService.getVectorStoreConfiguration(namespace, botId, enabled = true)?.setting

    private fun indexNamePrefix(
        namespace: String,
        botId: String,
        vectorStoreSetting: VectorStoreSetting?,
    ): String =
        VectorStoreUtils
            .getVectorStoreElements(
                namespace = namespace,
                botId = botId,
                indexSessionId = "",
                kNeighborsDocuments = 1,
                documentSearchType = DocumentSearchType.SIMILARITY_SEARCH,
                vectorStoreSetting = vectorStoreSetting,
            ).second

    private fun ragConfiguration(
        namespace: String,
        botId: String,
    ): BotRAGConfiguration =
        RAGService.getRAGConfiguration(namespace, botId)
            ?: WebVerticle.badRequest(
                "No RAG configuration is defined yet [namespace: $namespace, botId: $botId]",
            )
}

private fun String.toInternalCompressionStage(): String =
    when (this) {
        "beforeCut" -> "before_cut"
        "afterCut" -> "after_cut"
        else -> WebVerticle.badRequest("Unsupported compression stage [$this]")
    }

private fun String.toPublicCompressionStage(): String =
    when (this) {
        "before_cut" -> "beforeCut"
        "after_cut" -> "afterCut"
        else -> throw IllegalStateException("Unsupported orchestrator compression stage [$this]")
    }
