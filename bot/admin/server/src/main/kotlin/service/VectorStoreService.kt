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
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfiguration
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfigurationDAO
import ai.tock.bot.admin.model.BotVectorStoreConfigurationDTO
import ai.tock.genai.orchestratorcore.utils.SecurityUtils
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import com.mongodb.MongoWriteException
import mu.KLogger
import mu.KotlinLogging

/**
 * Service that manage the vector store functionality
 */
object VectorStoreService {

    private val logger: KLogger = KotlinLogging.logger {}
    private val vectorStoreConfigurationDAO: BotVectorStoreConfigurationDAO get() = injector.provide()

    /**
     * Get the Vector Store configuration
     * @param namespace: the namespace
     * @param botId: the botId
     */
    fun getVectorStoreConfiguration(namespace: String, botId: String): BotVectorStoreConfiguration? {
        return vectorStoreConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
    }

    /**
     * Get the Vector Store configuration
     * @param namespace: the namespace
     * @param botId: the botId
     * @param enabled: the configuration activation (enabled or not)
     */
    fun getVectorStoreConfiguration(namespace: String, botId: String, enabled: Boolean): BotVectorStoreConfiguration? {
        return vectorStoreConfigurationDAO.findByNamespaceAndBotIdAndEnabled(namespace, botId, enabled)
    }

    /**
     * Deleting the Vector Store Configuration
     * @param namespace: the namespace
     * @param botId: the bot ID
     */
    fun deleteConfig(namespace: String, botId: String) {
        val vectorStoreConfig = vectorStoreConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
            ?: WebVerticle.badRequest("No Vector Store configuration is defined yet [namespace: $namespace, botId: $botId]")

        logger.info { "Deleting the Vector Store Configuration [namespace: $namespace, botId: $botId]" }
        vectorStoreConfigurationDAO.delete(vectorStoreConfig._id)

        logger.info { "Deleting the database secret ..." }
        SecurityUtils.deleteSecret(vectorStoreConfig.setting.password)
    }

    /**
     * Save Vector Store configuration and filter errors
     * @param vectorStoreConfig : the vector store configuration to create or update
     * @throws [BadRequestException] if the vector store configuration is invalid
     * @return [BotVectorStoreConfiguration]
     */
    fun saveVectorStore(
        vectorStoreConfig: BotVectorStoreConfigurationDTO
    ): BotVectorStoreConfiguration {
        BotAdminService.getBotConfigurationsByNamespaceAndBotId(vectorStoreConfig.namespace, vectorStoreConfig.botId).firstOrNull()
            ?: WebVerticle.badRequest("No Vector Store configuration is defined yet [namespace: ${vectorStoreConfig.namespace}, botId = ${vectorStoreConfig.botId}]")
        return saveVectorStoreConfiguration(vectorStoreConfig)
    }

    /**
     * Save the Vector Store configuration
     * @param vectorStoreConfiguration [BotVectorStoreConfigurationDTO]
     */
    private fun saveVectorStoreConfiguration(
        vectorStoreConfiguration: BotVectorStoreConfigurationDTO
    ): BotVectorStoreConfiguration {
        val vectorStoreConfig = vectorStoreConfiguration.toBotVectorStoreConfiguration()

        // Check validity of the vector store configuration
        if (vectorStoreConfig.enabled) {
            VectorStoreValidationService.validate(vectorStoreConfig).let { errors ->
                if (errors.isNotEmpty()) {
                    throw BadRequestException(errors)
                }
            }
        }

        return try {
            vectorStoreConfigurationDAO.save(vectorStoreConfig)
        } catch (e: MongoWriteException) {
            throw BadRequestException(e.message ?: "Vector Store Configuration: registration failed on mongo ")
        } catch (e: Exception) {
            throw BadRequestException(e.message ?: "Vector Store Configuration: registration failed ")
        }
    }

}
