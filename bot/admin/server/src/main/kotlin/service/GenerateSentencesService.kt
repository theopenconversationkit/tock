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
import ai.tock.bot.admin.bot.generatesentences.BotGenerateSentencesConfigurationDAO
import ai.tock.bot.admin.bot.generatesentences.BotGenerateSentencesConfiguration
import ai.tock.bot.admin.model.BotGenerateSentencesConfigurationDTO
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import mu.KLogger
import mu.KotlinLogging

/**
 * Service that manage the Generate Sentences with Large Language Model (LLM) functionality
 */
object GenerateSentencesService {

    private val logger: KLogger = KotlinLogging.logger {}
    private val generateSentencesConfigurationDAO: BotGenerateSentencesConfigurationDAO get() = injector.provide()

    /**
     * Get the LLM Sentence Generation configuration
     */
    fun getGenerateSentencesConfiguration(namespace: String, botId: String): BotGenerateSentencesConfiguration? {
        return generateSentencesConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
    }

    /**
     * Save GenerateSentences configuration and filter errors
     * @param generateSentencesConfig : the llm sentence generation configuration to create or update
     * @throws [BadRequestException] if a llm sentence generation configuration is invalid
     * @return [BotGenerateSentencesConfiguration]
     */
    fun saveGenerateSentences(
        generateSentencesConfig: BotGenerateSentencesConfigurationDTO
    ): BotGenerateSentencesConfiguration {
        BotAdminService.getBotConfigurationsByNamespaceAndBotId(generateSentencesConfig.namespace, generateSentencesConfig.botId).firstOrNull()
            ?: WebVerticle.badRequest("No bot configuration is defined yet [namespace: ${generateSentencesConfig.namespace}, botId = ${generateSentencesConfig.botId}]")
        return saveGenerateSentencesConfiguration(generateSentencesConfig)
    }

    /**
     * Save the Generate Sentences configuration
     * @param generateSentencesConfiguration [BotGenerateSentencesConfigurationDTO]
     */
    private fun saveGenerateSentencesConfiguration(
        generateSentencesConfiguration: BotGenerateSentencesConfigurationDTO
    ): BotGenerateSentencesConfiguration {
        val generateSentencesConfig = generateSentencesConfiguration.toGenerateSentencesConfiguration()

        // Check validity of the configuration
        GenerateSentencesValidationService.validate(generateSentencesConfig).let { errors ->
            if(errors.isNotEmpty()) {
                throw BadRequestException(errors)
            }
        }

        return try {
            generateSentencesConfigurationDAO.save(generateSentencesConfig)
        } catch (e: Exception) {
            throw BadRequestException(e.message ?: "Generation Sentences Configuration: registration failed ")
        }
    }


}
