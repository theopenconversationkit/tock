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

package ai.tock.bot.connector.rest

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfiguration.Companion.defaultBaseUrl
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorType

/**
 * Adds a rest connector.
 */
fun addRestConnector(
    /**
     * Application id. Must be unique.
     */
    applicationId: String,
    /**
     * The http listening base path.
     */
    path: String = "/rest-connector",
    /**
     * The name of the application.
     */
    name: String = applicationId,
    /**
     * The base url for the connector path.
     */
    baseUrl: String? = defaultBaseUrl,
    /**
     * The owner of the rest connector (if applicable)
     */
    ownerConnectorType: ConnectorType? = null,
): ConnectorConfiguration {
    return ConnectorConfiguration(
        applicationId,
        path,
        ConnectorType.rest,
        name,
        baseUrl,
        ownerConnectorType,
    )
}

/**
 * Generates a default connector path from a base configuration.
 */
private fun generateRestConnectorPath(botConfiguration: BotApplicationConfiguration): String =
    "/io/${
        botConfiguration.namespace.lowercase().replace("\\s".toRegex(), "")
    }/test/test-${botConfiguration.applicationId.replace("\\s".toRegex(), "_")}"

/**
 * Returns a rest configuration from a base configuration.
 */
fun addRestConnector(botConfiguration: BotApplicationConfiguration): ConnectorConfiguration {
    return addRestConnector(
        "test-${botConfiguration.applicationId}",
        generateRestConnectorPath(botConfiguration),
        botConfiguration.name,
        botConfiguration.baseUrl,
        botConfiguration.connectorType,
    )
}
