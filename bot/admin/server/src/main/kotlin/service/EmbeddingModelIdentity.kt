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

import ai.tock.genai.orchestratorcore.models.em.AzureOpenAIEMSetting
import ai.tock.genai.orchestratorcore.models.em.EMSettingBase
import ai.tock.genai.orchestratorcore.models.em.OllamaEMSetting
import ai.tock.genai.orchestratorcore.models.em.OpenAIEMSetting

/**
 * Single source of truth for the "normalized embedding model" of an embedding setting and for comparing that
 * identity between a collection and a bot. Used by both the knowledge base and the RAG settings.
 *
 * The normalized model is what identifies the vectors a collection holds: the same model reached through OpenAI or
 * Azure produces the same vectors, so the provider is never part of the identity, and neither are endpoint fields
 * (apiBase, baseUrl, apiVersion) nor Azure's arbitrary per-resource deploymentName.
 */
object EmbeddingModelIdentity {

    /**
     * The normalized embedding model, or null when it is unknown (Azure without an explicit model). null is never
     * blocking: Tock cannot certify a model it does not know, so it declines to create a collection but never refuses
     * an existing one over an unknown identity.
     */
    fun normalized(setting: EMSettingBase<*>): String? =
        when (setting) {
            is OpenAIEMSetting<*> -> normalize(setting.model)
            is OllamaEMSetting<*> -> normalize(setting.model, stripLatest = true)
            // Azure exposes an optional model; deploymentName is an arbitrary per-resource name and must NOT be used.
            is AzureOpenAIEMSetting<*> -> normalize(setting.model)
            else -> null
        }

    /**
     * Normalize a raw embedding model read from a collection's contract metadata: trim and strip a trailing ":latest".
     * The collection does not carry its provider, so this is applied uniformly (Ollama tags such as
     * "nomic-embed-text:latest" and "nomic-embed-text" denote the same model and must compare equal).
     */
    fun normalizeCollectionModel(model: String?): String? = normalize(model, stripLatest = true)

    /** Trim; for Ollama strip a trailing ":latest". Blank becomes null (unknown). */
    private fun normalize(
        model: String?,
        stripLatest: Boolean = false,
    ): String? {
        val trimmed = model?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return if (stripLatest && trimmed.endsWith(":latest")) {
            trimmed.removeSuffix(":latest").takeIf { it.isNotEmpty() }
        } else {
            trimmed
        }
    }

    /**
     * Compare an already-normalized collection model with an already-normalized bot model. Either side null/absent
     * yields [EmbeddingCoherence.UNKNOWN], which is never blocking.
     */
    fun coherence(
        collectionModel: String?,
        botModel: String?,
    ): EmbeddingCoherence =
        if (collectionModel.isNullOrBlank() || botModel.isNullOrBlank()) {
            EmbeddingCoherence.UNKNOWN
        } else if (collectionModel == botModel) {
            EmbeddingCoherence.MATCH
        } else {
            EmbeddingCoherence.MISMATCH
        }
}

/** Result of comparing a collection's embedding model with a bot's. Only [MISMATCH] is blocking. */
enum class EmbeddingCoherence { MATCH, MISMATCH, UNKNOWN }
