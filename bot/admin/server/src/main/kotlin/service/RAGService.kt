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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.model.BotRAGConfigurationDTO
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.nlp.core.Intent
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
    fun getRAGConfiguration(namespace: String, botId: String): BotRAGConfiguration? {
        return ragConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
    }

    /**
     * Deleting the RAG Configuration
     * @param namespace: the namespace
     * @param botId: the bot ID
     */
    fun deleteConfig(namespace: String, botId: String) {
        val ragConfig = ragConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
            ?: WebVerticle.badRequest("No RAG configuration is defined yet [namespace: $namespace, botId: $botId]")
        logger.info { "Deleting the RAG Configuration [namespace: $namespace, botId: $botId]" }
        return ragConfigurationDAO.delete(ragConfig._id)
    }

    /**
     * Save RAG configuration and filter errors
     * @param ragConfig : the rag configuration to create or update
     * @throws [BadRequestException] if a rag configuration is invalid
     * @return [BotRAGConfiguration]
     */
    fun saveRag(
        ragConfig: BotRAGConfigurationDTO
    ): BotRAGConfiguration {
        BotAdminService.getBotConfigurationsByNamespaceAndBotId(ragConfig.namespace, ragConfig.botId).firstOrNull()
            ?: WebVerticle.badRequest("No RAG configuration is defined yet [namespace: ${ragConfig.namespace}, botId: ${ragConfig.botId}]")
        logger.info { "Saving the RAG Configuration [namespace: ${ragConfig.namespace}, botId: ${ragConfig.botId}]" }
        return saveRagConfiguration(ragConfig)
    }

    /**
     * Save the RAG configuration
     * @param ragConfiguration [BotRAGConfigurationDTO]
     */
    private fun saveRagConfiguration(
        ragConfiguration: BotRAGConfigurationDTO
    ): BotRAGConfiguration {
        val ragConfig = ragConfiguration.toBotRAGConfiguration()

        // Check validity of the rag configuration
        RAGValidationService.validate(ragConfig).let { errors ->
            if (errors.isNotEmpty()) {
                throw BadRequestException(errors)
            }
        }

        return try {
            // If RAG Enabled, so disable the unknown story if exists
            // Else enable the unknown story if exists
            storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                ragConfiguration.namespace, ragConfiguration.botId, Intent.UNKNOWN_INTENT_NAME.withoutNamespace()
            )?.let {
                storyDefinitionDAO.save(
                    it.copy(
                        features = prepareEndingFeatures(
                            it, !ragConfiguration.enabled
                        )
                    )
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
        story: StoryDefinitionConfiguration, ragEnabled: Boolean
    ): List<StoryDefinitionConfigurationFeature> {
        val features = mutableListOf<StoryDefinitionConfigurationFeature>()
        features.addAll(story.features)
        features.removeIf { feature -> feature.enabled != null }
        features.add(StoryDefinitionConfigurationFeature(null, ragEnabled, null, null))
        return features
    }


}
