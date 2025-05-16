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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.genai.orchestratorclient.requests.EMProviderSettingStatusRequest
import ai.tock.genai.orchestratorclient.requests.LLMProviderSettingStatusRequest
import ai.tock.genai.orchestratorclient.requests.VectorStoreProviderSettingStatusRequest
import ai.tock.genai.orchestratorclient.responses.ProviderSettingStatusResponse
import ai.tock.genai.orchestratorclient.services.EMProviderService
import ai.tock.genai.orchestratorclient.services.LLMProviderService
import ai.tock.genai.orchestratorclient.services.VectorStoreProviderService
import ai.tock.genai.orchestratorcore.utils.VectorStoreUtils
import ai.tock.shared.exception.error.ErrorMessage
import ai.tock.shared.injector
import ai.tock.shared.provide


object RAGValidationService {

    private val llmProviderService: LLMProviderService get() = injector.provide()
    private val emProviderService: EMProviderService get() = injector.provide()
    private val vectorStoreProviderService: VectorStoreProviderService get() = injector.provide()

    fun validate(ragConfig: BotRAGConfiguration): Set<ErrorMessage> {
        val observabilitySetting = ObservabilityService.getObservabilityConfiguration(
            ragConfig.namespace, ragConfig.botId, enabled = true
        )?.setting

        return mutableSetOf<ErrorMessage>().apply {
            val questionCondensingLlmErrors = llmProviderService.checkSetting(
                LLMProviderSettingStatusRequest(
                    ragConfig.questionCondensingLlmSetting!!,
                    observabilitySetting
                )
            ).getErrors("LLM setting check failed (for question condensing)")

            val questionAnsweringLlmErrors = llmProviderService.checkSetting(
                LLMProviderSettingStatusRequest(
                    ragConfig.questionAnsweringLlmSetting!!,
                    observabilitySetting
                )
            ).getErrors("LLM setting check failed (for question answering)")

            val embeddingErrors = emProviderService.checkSetting(
                EMProviderSettingStatusRequest(ragConfig.emSetting)
            ).getErrors("Embedding Model setting check failed")

            val indexSessionIdErrors = validateIndexSessionId(ragConfig)

            val vectorStoreErrors = (indexSessionIdErrors + embeddingErrors).takeIf { it.isEmpty() }?.let {
                val vectorStoreSetting = VectorStoreService.getVectorStoreConfiguration(
                    ragConfig.namespace, ragConfig.botId, enabled = true
                )?.setting

                val (_, indexName) = VectorStoreUtils.getVectorStoreElements(
                    ragConfig.namespace,
                    ragConfig.botId,
                    ragConfig.indexSessionId!!,
                    ragConfig.maxDocumentsRetrieved,
                    vectorStoreSetting
                )

                vectorStoreProviderService.checkSetting(
                    VectorStoreProviderSettingStatusRequest(
                        vectorStoreSetting = vectorStoreSetting,
                        emSetting = ragConfig.emSetting,
                        documentIndexName = indexName
                    )
                ).getErrors("Vector store setting check failed")
            } ?: emptySet()

            addAll(questionCondensingLlmErrors + questionAnsweringLlmErrors + embeddingErrors + indexSessionIdErrors + vectorStoreErrors)
        }
    }

    private fun validateIndexSessionId(ragConfig: BotRAGConfiguration): Set<ErrorMessage> {
        val errors = mutableSetOf<ErrorMessage>()
        if (ragConfig.enabled && ragConfig.indexSessionId.isNullOrBlank()) {
            errors.add(
                ErrorMessage(
                    message = "The index session ID is required to enable the RAG feature"
                )
            )
        }
        return errors
    }

    private fun ProviderSettingStatusResponse?.getErrors(message: String): Set<ErrorMessage> =
        this?.errors?.map { ErrorMessage(message = message, params = errors) }?.toSet() ?: emptySet()

}
