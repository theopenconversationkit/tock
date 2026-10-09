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

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.model.genai.BotRAGConfigurationDTO
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.genai.orchestratorcore.utils.SecurityUtils
import ai.tock.nlp.core.Intent
import ai.tock.shared.exception.error.ErrorMessage
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.withoutNamespace
import com.mongodb.MongoWriteException
import mu.KLogger
import mu.KotlinLogging

/**
 * Service that manage the retrieval augmented generation (RAG) with Large Language Model (LLM) functionality
 */
object RAGService {
    private val logger: KLogger = KotlinLogging.logger {}
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    private val ragConfigurationDAO: BotRAGConfigurationDAO get() = injector.provide()

    /**
     * Get the RAG configuration
     * @param namespace: the namespace
     * @param botId: the bot ID
     */
    fun getRAGConfiguration(
        namespace: String,
        botId: String,
    ): BotRAGConfiguration? = ragConfigurationDAO.findByNamespaceAndBotId(namespace, botId)

    /**
     * Deleting the RAG Configuration
     * @param namespace: the namespace
     * @param botId: the bot ID
     */
    fun deleteConfig(
        namespace: String,
        botId: String,
    ) {
        val ragConfig =
            ragConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
                ?: WebVerticle.badRequest("No RAG configuration is defined yet [namespace: $namespace, botId: $botId]")

        logger.info { "Deleting the RAG Configuration [namespace: $namespace, botId: $botId]" }
        ragConfigurationDAO.delete(ragConfig._id)

        logger.info { "Deleting the question condensing LLM secret ..." }
        ragConfig.questionCondensingLlmSetting.apiKey?.let { SecurityUtils.deleteSecret(it) }
        logger.info { "Deleting the question answering LLM secret ..." }
        ragConfig.questionAnsweringLlmSetting.apiKey?.let { SecurityUtils.deleteSecret(it) }
        logger.info { "Deleting the Embedding secret ..." }
        ragConfig.emSetting.apiKey?.let { SecurityUtils.deleteSecret(it) }
    }

    /**
     * Save RAG configuration and filter errors
     * @param ragConfig : the rag configuration to create or update
     * @throws [BadRequestException] if a rag configuration is invalid
     * @return [BotRAGConfiguration]
     */
    fun saveRag(
        ragConfig: BotRAGConfigurationDTO,
        author: String? = null,
    ): BotRAGConfiguration =
        BotHistoryService.configuration(
            ragConfig.namespace,
            ragConfig.botId,
            "rag-settings",
            author,
            previous = { ragConfigurationDAO.findByNamespaceAndBotId(ragConfig.namespace, ragConfig.botId) },
            save = { saveWithValidation(ragConfig) },
        )

    private fun saveWithValidation(ragConfig: BotRAGConfigurationDTO): BotRAGConfiguration {
        BotAdminService.getBotConfigurationsByNamespaceAndBotId(ragConfig.namespace, ragConfig.botId).firstOrNull()
            ?: WebVerticle.badRequest("No RAG configuration is defined yet [namespace: ${ragConfig.namespace}, botId: ${ragConfig.botId}]")
        rejectIncompatibleEmbedding(ragConfig)
        logger.info { "Saving the RAG Configuration [namespace: ${ragConfig.namespace}, botId: ${ragConfig.botId}]" }
        return saveRagConfiguration(ragConfig)
    }

    /**
     * Guard against silently pointing the bot at (or re-embedding against) a collection whose embedding model no longer
     * matches: when the incoming config carries a non-blank indexSessionId and that session, or the embedding provider or
     * model, changed compared to the stored config, resolve the target collection and reject a MISMATCH. UNKNOWN (no
     * contract metadata, missing collection, or an undefined model) never blocks. PGVector only: OpenSearch collections
     * expose no embedding metadata, so their coherence is always UNKNOWN and this guard is a no-op for them.
     */
    private fun rejectIncompatibleEmbedding(ragConfig: BotRAGConfigurationDTO) {
        // Work from the DTO's emSetting directly: converting to the entity form (toBotRAGConfiguration/toEntity) would
        // create or update a secret through the secret manager. This guard must have no such side effect.
        val session = ragConfig.indexSessionId?.takeIf { it.isNotBlank() } ?: return
        val stored = ragConfigurationDAO.findByNamespaceAndBotId(ragConfig.namespace, ragConfig.botId)
        val changed =
            stored == null ||
                stored.indexSessionId != ragConfig.indexSessionId ||
                stored.emSetting.provider != ragConfig.emSetting.provider ||
                EmbeddingModelIdentity.normalized(stored.emSetting) != EmbeddingModelIdentity.normalized(ragConfig.emSetting)
        if (!changed) return
        val status = KnowledgeBaseService.default.indexStatusFor(ragConfig.namespace, ragConfig.botId, session, ragConfig.emSetting)
        if (status.coherence == EmbeddingCoherence.MISMATCH) {
            throw BadRequestException(setOf(ErrorMessage(message = "rag.embedding.incompatible_index")))
        }
    }

    /**
     * Point the bot's RAG configuration at a (re-)created knowledge base collection. Never touches `enabled`: switching
     * the index and activating RAG are two distinct actions. Validates and saves only if the stored config is unchanged.
     */
    internal fun switchKnowledgeBaseIndex(
        previous: BotRAGConfiguration,
        updated: BotRAGConfiguration,
    ) {
        check(RAGValidationService.validate(updated).isEmpty()) { "knowledge-base.job.activation_failed" }
        check(ragConfigurationDAO.saveIfUnchanged(previous, updated)) { "knowledge-base.job.configuration_changed" }
    }

    private fun saveRagConfiguration(ragConfiguration: BotRAGConfigurationDTO): BotRAGConfiguration {
        val ragConfig = ragConfiguration.toBotRAGConfiguration()

        // Check validity of the rag configuration
        if (ragConfig.enabled) {
            RAGValidationService.validate(ragConfig).let { errors ->
                if (errors.isNotEmpty()) {
                    throw BadRequestException(errors)
                }
            }
        }

        return try {
            // If RAG Enabled, so disable the unknown story if exists
            // Else enable the unknown story if exists
            storyDefinitionDAO
                .getStoryDefinitionByNamespaceAndBotIdAndIntent(
                    ragConfiguration.namespace,
                    ragConfiguration.botId,
                    Intent.UNKNOWN_INTENT_NAME.withoutNamespace(),
                )?.let {
                    storyDefinitionDAO.save(
                        it.copy(
                            features =
                                prepareEndingFeatures(
                                    it,
                                    !ragConfiguration.enabled,
                                ),
                        ),
                    )
                }

            ragConfigurationDAO.save(ragConfig)
        } catch (e: MongoWriteException) {
            throw BadRequestException(e.message ?: "Rag Configuration: registration failed on mongo ")
        } catch (e: Exception) {
            throw BadRequestException(e.message ?: "Rag Configuration: registration failed ")
        }
    }

    private fun prepareEndingFeatures(
        story: StoryDefinitionConfiguration,
        ragEnabled: Boolean,
    ): List<StoryDefinitionConfigurationFeature> {
        val features = mutableListOf<StoryDefinitionConfigurationFeature>()
        features.addAll(story.features)
        features.removeIf { feature -> feature.enabled != null }
        features.add(StoryDefinitionConfigurationFeature(null, ragEnabled, null, null))
        return features
    }
}
