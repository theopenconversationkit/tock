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

package ai.tock.bot.connector.alexa

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.shared.resourceAsString
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * [ConnectorProvider] for [AlexaConnector].
 */
internal object AlexaConnectorProvider : ConnectorProvider {

    private val logger = KotlinLogging.logger {}

    private const val PROJECT_IDS = "_project_ids"
    private const val PROJECT_TIMESTAMP = "_project_timestamp"
    private const val PROJECT_ID_SEPARATOR = ","
    private const val ALEXA_MAPPER = "_mapper"

    private const val DEFAULT_TIMESTAMP: Long = 10000L

    override val connectorType: ConnectorType get() = alexaConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector =
        with(connectorConfiguration) {
            val mapper = parameters[ALEXA_MAPPER]
            AlexaConnector(
                connectorId,
                path,
                try {
                    if (mapper.isNullOrBlank()) {
                        AlexaTockMapper(connectorId)
                    } else {
                        Class.forName(mapper).kotlin.primaryConstructor!!.call(connectorId) as AlexaTockMapper
                    }
                } catch (e: Exception) {
                    logger.error("not found Alexa Mapper $mapper, fallback to default", e)
                    AlexaTockMapper(connectorId)
                },
                parameters[PROJECT_IDS]
                    ?.split(PROJECT_ID_SEPARATOR)
                    ?.filter { it.isNotBlank() }
                    ?.toSet()
                    ?: emptySet(),
                parameters[PROJECT_TIMESTAMP]?.toLong() ?: DEFAULT_TIMESTAMP
            )
        }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            alexaConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Project ids",
                    "_project_ids",
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Request timestamp (in ms)",
                    "_project_timestamp",
                    false
                ),
                ConnectorTypeConfigurationField(
                    "Alexa mapper class",
                    "_mapper",
                    false
                )
            ),
            resourceAsString("/alexa.svg")
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(AlexaMessage::class)
}

internal class AlexaConnectorProviderService : ConnectorProvider by AlexaConnectorProvider
