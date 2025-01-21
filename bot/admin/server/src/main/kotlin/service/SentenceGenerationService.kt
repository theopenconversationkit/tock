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
import ai.tock.bot.admin.bot.sentencegeneration.BotSentenceGenerationConfiguration
import ai.tock.bot.admin.bot.sentencegeneration.BotSentenceGenerationConfigurationDAO
import ai.tock.bot.admin.model.genai.BotSentenceGenerationConfigurationDTO
import ai.tock.genai.orchestratorcore.utils.SecurityUtils
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import mu.KLogger
import mu.KotlinLogging

/**
 * Service that manage the Generate Sentences with Large Language Model (LLM) functionality
 */
object SentenceGenerationService {

    private val logger: KLogger = KotlinLogging.logger {}
    private val sentenceGenerationConfigurationDAO: BotSentenceGenerationConfigurationDAO get() = injector.provide()

    /**
     * Get the LLM Sentence Generation configuration
     * @param namespace: the namespace
     * @param botId: the bot ID
     */
    fun getSentenceGenerationConfiguration(namespace: String, botId: String): BotSentenceGenerationConfiguration? {
        return sentenceGenerationConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
    }

    /**
     * Deleting the LLM Sentence Generation Configuration
     * @param namespace: the namespace
     * @param botId: the bot ID
     */
    fun deleteConfig(namespace: String, botId: String) {
        val sentenceGenerationConfiguration = sentenceGenerationConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
            ?: WebVerticle.badRequest("No LLM Sentence Generation configuration is defined yet [namespace: $namespace, botId: $botId]")

        logger.info { "Deleting the LLM Sentence Generation Configuration [namespace: $namespace, botId: $botId] ..." }
        sentenceGenerationConfigurationDAO.delete(sentenceGenerationConfiguration._id)

        logger.info { "Deleting the LLM secret ..." }
        sentenceGenerationConfiguration.llmSetting.apiKey?.let { SecurityUtils.deleteSecret(it) }
    }

    /**
     * Save SentenceGeneration configuration and filter errors
     * @param sentenceGenerationConfig : the llm sentence generation configuration to create or update
     * @throws [BadRequestException] if a llm sentence generation configuration is invalid
     * @return [BotSentenceGenerationConfiguration]
     */
    fun saveSentenceGeneration(
        sentenceGenerationConfig: BotSentenceGenerationConfigurationDTO
    ): BotSentenceGenerationConfiguration {
        BotAdminService.getBotConfigurationsByNamespaceAndBotId(sentenceGenerationConfig.namespace, sentenceGenerationConfig.botId).firstOrNull()
            ?: WebVerticle.badRequest("No bot configuration is defined yet [namespace: ${sentenceGenerationConfig.namespace}, botId = ${sentenceGenerationConfig.botId}]")
        return saveSentenceGenerationConfiguration(sentenceGenerationConfig)
    }

    /**
     * Save the Generate Sentences configuration
     * @param sentenceGenerationConfiguration [BotSentenceGenerationConfigurationDTO]
     */
    private fun saveSentenceGenerationConfiguration(
        sentenceGenerationConfiguration: BotSentenceGenerationConfigurationDTO
    ): BotSentenceGenerationConfiguration {
        val sentenceGenerationConfig = sentenceGenerationConfiguration.toSentenceGenerationConfiguration()

        // Check validity of the configuration
        if(sentenceGenerationConfig.enabled) {
            SentenceGenerationValidationService.validate(sentenceGenerationConfig).let { errors ->
                if (errors.isNotEmpty()) {
                    throw BadRequestException(errors)
                }
            }
        }

        return try {
            sentenceGenerationConfigurationDAO.save(sentenceGenerationConfig)
        } catch (e: Exception) {
            throw BadRequestException(e.message ?: "Generation Sentences Configuration: registration failed ")
        }
    }

}
