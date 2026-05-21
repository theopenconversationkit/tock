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
import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfiguration
import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfigurationDAO
import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesLexiconGroup
import ai.tock.bot.admin.model.genai.BotBusinessRulesConfigurationDTO
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import com.mongodb.MongoWriteException
import mu.KLogger
import mu.KotlinLogging

/**
 * Service that manages bot business rules used by generative AI features.
 */
object BusinessRulesService {
    private val logger: KLogger = KotlinLogging.logger {}
    private val businessRulesConfigurationDAO: BotBusinessRulesConfigurationDAO get() = injector.provide()

    /**
     * Get the Business Rules configuration
     * @param namespace: the namespace
     * @param botId: the bot ID
     */
    fun getBusinessRulesConfiguration(
        namespace: String,
        botId: String,
    ): BotBusinessRulesConfiguration? {
        return businessRulesConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
    }

    /**
     * Save Business Rules configuration.
     * @param namespace the namespace
     * @param botId the bot ID
     * @param businessRulesConfig the business rules configuration to create or update
     * @return [BotBusinessRulesConfiguration]
     */
    fun saveBusinessRules(
        namespace: String,
        botId: String,
        businessRulesConfig: BotBusinessRulesConfigurationDTO,
    ): BotBusinessRulesConfiguration {
        BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, botId).firstOrNull()
            ?: WebVerticle.badRequest("No bot configuration is defined yet [namespace: $namespace, botId = $botId]")

        logger.info {
            "Saving the Business Rules Configuration [namespace: $namespace, botId: $botId]"
        }
        return saveBusinessRulesConfiguration(namespace, botId, businessRulesConfig)
    }

    private fun saveBusinessRulesConfiguration(
        namespace: String,
        botId: String,
        businessRulesConfiguration: BotBusinessRulesConfigurationDTO,
    ): BotBusinessRulesConfiguration {
        val existingConfiguration =
            businessRulesConfigurationDAO.findByNamespaceAndBotId(
                namespace,
                botId,
            )
        val businessRulesConfigurationWithLexiconGroupIds =
            businessRulesConfiguration.withAssignedLexiconGroupIds(existingConfiguration?.lexiconGroups.orEmpty())

        val businessRulesConfig =
            businessRulesConfigurationWithLexiconGroupIds.toBotBusinessRulesConfiguration(
                namespace = namespace,
                botId = botId,
                existingId = existingConfiguration?._id,
            )

        return try {
            businessRulesConfigurationDAO.save(businessRulesConfig)
        } catch (e: MongoWriteException) {
            throw BadRequestException(e.message ?: "Business Rules Configuration: registration failed on mongo ")
        } catch (e: Exception) {
            throw BadRequestException(e.message ?: "Business Rules Configuration: registration failed ")
        }
    }

    private fun BotBusinessRulesConfigurationDTO.withAssignedLexiconGroupIds(existingLexiconGroups: List<BotBusinessRulesLexiconGroup>): BotBusinessRulesConfigurationDTO {
        val providedIds = lexiconGroups.mapNotNull { it.id }
        val duplicatedIds =
            providedIds.groupingBy { it }
                .eachCount()
                .filterValues { it > 1 }
                .keys

        if (duplicatedIds.isNotEmpty()) {
            throw BadRequestException("Duplicate lexicon group id(s): ${duplicatedIds.joinToString()}")
        }

        if (providedIds.any { it <= 0 }) {
            throw BadRequestException("Lexicon group ids must be positive")
        }

        var nextId = (existingLexiconGroups.map { it.id } + providedIds).maxOrNull()?.plus(1) ?: 1

        return copy(
            lexiconGroups =
                lexiconGroups.map { group ->
                    group.copy(id = group.id ?: nextId++)
                },
        )
    }
}
