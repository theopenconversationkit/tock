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
import ai.tock.bot.admin.bot.observability.BotObservabilityConfiguration
import ai.tock.bot.admin.bot.observability.BotObservabilityConfigurationDAO
import ai.tock.bot.admin.model.BotObservabilityConfigurationDTO
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import com.mongodb.MongoWriteException
import mu.KLogger
import mu.KotlinLogging

/**
 * Service that manage the observability functionality
 */
object ObservabilityService {

    private val logger: KLogger = KotlinLogging.logger {}
    private val observabilityConfigurationDAO: BotObservabilityConfigurationDAO get() = injector.provide()
    /**
     * Get the Observability configuration
     */
    fun getObservabilityConfiguration(namespace: String, botId: String): BotObservabilityConfiguration? {
        return observabilityConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
    }

    /**
     * Get the Observability configuration
     * @param namespace: the namespace
     * @param botId: the botId
     * @param enabled: the observability activation (enabled or not)
     */
    fun getObservabilityConfiguration(namespace: String, botId: String, enabled: Boolean): BotObservabilityConfiguration? {
        return observabilityConfigurationDAO.findByNamespaceAndBotIdAndEnabled(namespace, botId, enabled)
    }

    /**
     * Save Observability configuration and filter errors
     * @param observabilityConfig : the observability configuration to create or update
     * @throws [BadRequestException] if the observability configuration is invalid
     * @return [BotObservabilityConfiguration]
     */
    fun saveObservability(
        observabilityConfig: BotObservabilityConfigurationDTO
    ): BotObservabilityConfiguration {
        BotAdminService.getBotConfigurationsByNamespaceAndBotId(observabilityConfig.namespace, observabilityConfig.botId).firstOrNull()
            ?: WebVerticle.badRequest("No bot configuration is defined yet [namespace: ${observabilityConfig.namespace}, botId = ${observabilityConfig.botId}]")
        return saveObservabilityConfiguration(observabilityConfig)
    }

    /**
     * Save the Observability configuration
     * @param observabilityConfiguration [BotObservabilityConfigurationDTO]
     */
    private fun saveObservabilityConfiguration(
        observabilityConfiguration: BotObservabilityConfigurationDTO
    ): BotObservabilityConfiguration {
        val observabilityConfig = observabilityConfiguration.toBotObservabilityConfiguration()

        // Check validity of the observability configuration
        ObservabilityValidationService.validate(observabilityConfig).let { errors ->
            if (errors.isNotEmpty()) {
                throw BadRequestException(errors)
            }
        }

        return try {
            observabilityConfigurationDAO.save(observabilityConfig)
        } catch (e: MongoWriteException) {
            throw BadRequestException(e.message ?: "Observability Configuration: registration failed on mongo ")
        } catch (e: Exception) {
            throw BadRequestException(e.message ?: "Observability Configuration: registration failed ")
        }
    }

}
