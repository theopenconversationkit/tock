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
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfiguration
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfigurationDAO
import ai.tock.bot.admin.model.genai.BotDocumentCompressorConfigurationDTO
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import com.mongodb.MongoWriteException
import mu.KLogger
import mu.KotlinLogging

/**
 * Service that manage the document compressor functionality
 */
object DocumentCompressorService {

    private val logger: KLogger = KotlinLogging.logger {}
    private val documentCompressorConfigurationDAO: BotDocumentCompressorConfigurationDAO get() = injector.provide()
    /**
     * Get the Document Compressor Configuration
     * @param namespace: the namespace
     * @param botId: the bot ID
     */
    fun getDocumentCompressorConfiguration(namespace: String, botId: String): BotDocumentCompressorConfiguration? {
        return documentCompressorConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
    }

    /**
     * Get the Document Compressor Configuration
     * @param namespace: the namespace
     * @param botId: the botId
     * @param enabled: the document compressor activation (enabled or not)
     */
    fun getDocumentCompressorConfiguration(namespace: String, botId: String, enabled: Boolean): BotDocumentCompressorConfiguration? {
        return documentCompressorConfigurationDAO.findByNamespaceAndBotIdAndEnabled(namespace, botId, enabled)
    }

    /**
     * Deleting the Document Compressor Configuration
     * @param namespace: the namespace
     * @param botId: the bot ID
     */
    fun deleteConfig(namespace: String, botId: String) {
        val documentCompressorConfig = documentCompressorConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
            ?: WebVerticle.badRequest("No Document Compressor Configuration is defined yet [namespace: $namespace, botId: $botId]")
        logger.info { "Deleting the Document Compressor Configuration [namespace: $namespace, botId: $botId]" }
        return documentCompressorConfigurationDAO.delete(documentCompressorConfig._id)
    }

    /**
     * Save Document Compressor Configuration and filter errors
     * @param documentCompressorConfig : the document compressor configuration to create or update
     * @throws [BadRequestException] if the document compressor configuration is invalid
     * @return [BotDocumentCompressorConfiguration]
     */
    fun saveDocumentCompressor(
        documentCompressorConfig: BotDocumentCompressorConfigurationDTO
    ): BotDocumentCompressorConfiguration {
        BotAdminService.getBotConfigurationsByNamespaceAndBotId(documentCompressorConfig.namespace, documentCompressorConfig.botId).firstOrNull()
            ?: WebVerticle.badRequest("No bot configuration is defined yet [namespace: ${documentCompressorConfig.namespace}, botId = ${documentCompressorConfig.botId}]")
        return saveDocumentCompressorConfiguration(documentCompressorConfig)
    }

    /**
     * Save the Document Compressor Configuration
     * @param documentCompressorConfiguration [BotDocumentCompressorConfigurationDTO]
     */
    private fun saveDocumentCompressorConfiguration(
        documentCompressorConfiguration: BotDocumentCompressorConfigurationDTO
    ): BotDocumentCompressorConfiguration {
        val documentCompressorConfig = documentCompressorConfiguration.toBotDocumentCompressorConfiguration()

        // Check validity of the document compressor configuration
        if(documentCompressorConfig.enabled) {
            DocumentCompressorValidationService.validate(documentCompressorConfig).let { errors ->
                if (errors.isNotEmpty()) {
                    throw BadRequestException(errors)
                }
            }
        }

        return try {
            documentCompressorConfigurationDAO.save(documentCompressorConfig)
        } catch (e: MongoWriteException) {
            throw BadRequestException(e.message ?: "Document Compressor Configuration: registration failed on mongo ")
        } catch (e: Exception) {
            throw BadRequestException(e.message ?: "Document Compressor Configuration: registration failed ")
        }
    }

}
