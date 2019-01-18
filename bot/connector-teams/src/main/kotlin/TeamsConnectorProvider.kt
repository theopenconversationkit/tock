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

package fr.vsct.tock.bot.connector.teams

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ConnectorTypeConfiguration
import fr.vsct.tock.bot.connector.ConnectorTypeConfigurationField
import fr.vsct.tock.shared.resourceAsString

/**
 *
 */
internal object TeamsConnectorProvider : ConnectorProvider {

    private const val APP_ID = "appId"
    private const val PASSWORD = "password"

    override val connectorType: ConnectorType get() = teamsConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return TeamsConnector(
                connectorId,
                path,
                parameters.getValue(APP_ID),
                parameters.getValue(PASSWORD)
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            teamsConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "appId",
                    APP_ID,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "password",
                    PASSWORD,
                    true
                )
            ),
            resourceAsString("/teams.svg")
        )

}

internal class TeamsConnectorProviderService : ConnectorProvider by TeamsConnectorProvider