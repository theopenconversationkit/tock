/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.ga

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType

/**
 *
 */
internal object GAConnectorProvider : ConnectorProvider {

    private const val PROJECT_IDS = "_project_ids"
    private const val PROJECT_ID_SEPARATOR = ","

    override val connectorType: ConnectorType = gaConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return GAConnector(
                connectorId,
                path,
                parameters[PROJECT_IDS]
                    ?.split(PROJECT_ID_SEPARATOR)
                    ?.filter { it.isNotBlank() }
                    ?.toSet()
                        ?: emptySet())
        }
    }

    /**
     * Create a new google assistant connector configuration.
     */
    fun newConfiguration(
        connectorId: String = "ga",
        path: String = "/ga",
        applicationName: String,
        allowedProjectIds: Set<String> = emptySet()
    ): ConnectorConfiguration {

        return ConnectorConfiguration(
            connectorId,
            path,
            connectorType,
            applicationName,
            null,
            parameters = mapOf(
                PROJECT_IDS to allowedProjectIds.joinToString(PROJECT_ID_SEPARATOR)
            )
        )
    }
}