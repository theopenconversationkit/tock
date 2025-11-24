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

package ai.tock.bot.connector

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.shared.mapNotNullValues

/**
 * Configuration parameters used by a [ConnectorProvider] to create a new [Connector] instance.
 */
data class ConnectorConfiguration(
    /**
     * The connector id - unique for a given bot.
     */
    val connectorId: String,
    /**
     * The relative REST path of the connector.
     */
    val path: String,
    /**
     * The connector type.
     */
    val type: ConnectorType,
    /**
     * The underlying connector type. For example, you can have connectorType=rest and ownerConnectorType=messenger.
     */
    val ownerConnectorType: ConnectorType? = null,
    /**
     * Additional parameters for this connector.
     */
    val parameters: Map<String, String> = emptyMap(),
) {
    constructor(
        connectorId: String,
        path: String,
        type: ConnectorType,
        applicationName: String,
        baseUrl: String?,
        ownerConnectorType: ConnectorType? = null,
        parameters: Map<String, String> = emptyMap(),
    ) :
        this(
            connectorId,
            path,
            type,
            ownerConnectorType,
            parameters +
                mapNotNullValues(
                    APPLICATION_NAME to applicationName,
                    BASE_URL to baseUrl,
                ),
        )

    internal constructor(
        base: ConnectorConfiguration?,
        botApplicationConfiguration: BotApplicationConfiguration,
    ) : this(
        botApplicationConfiguration.applicationId,
        botApplicationConfiguration.path ?: base?.path ?: "/",
        botApplicationConfiguration.connectorType,
        botApplicationConfiguration.name,
        botApplicationConfiguration.baseUrl,
        botApplicationConfiguration.ownerConnectorType,
        botApplicationConfiguration.parameters +
            mapNotNullValues(
                APPLICATION_NAME to botApplicationConfiguration.name,
                BASE_URL to botApplicationConfiguration.baseUrl,
            ),
    )

    internal constructor(botApplicationConfiguration: BotApplicationConfiguration) :
        this(null, botApplicationConfiguration)

    companion object {
        private const val APPLICATION_NAME: String = "_name"
        private const val BASE_URL: String = "_base_url"
    }

    /**
     * The name of application.
     */
    fun getName(): String = parameters.getOrDefault(APPLICATION_NAME, connectorId)

    /**
     * The base url of the connector.
     */
    fun getBaseUrl(): String = parameters.getOrDefault(BASE_URL, BotApplicationConfiguration.defaultBaseUrl)

    internal fun parametersWithoutDefaultKeys(): Map<String, String> = parameters.filterKeys { it != APPLICATION_NAME && it != BASE_URL }
}
