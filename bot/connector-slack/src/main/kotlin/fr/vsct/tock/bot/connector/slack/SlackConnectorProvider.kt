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

package fr.vsct.tock.bot.connector.slack

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ConnectorTypeConfiguration
import fr.vsct.tock.bot.connector.ConnectorTypeConfigurationField
import fr.vsct.tock.shared.resourceAsString

internal object SlackConnectorProvider : ConnectorProvider {

    private const val OUT_TOKEN_1 = "outToken1"
    private const val OUT_TOKEN_2 = "outToken2"
    private const val OUT_TOKEN_3 = "outToken3"

    override val connectorType: ConnectorType get() = slackConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return SlackConnector(
                connectorId,
                path,
                "#bot",
                parameters.getValue(OUT_TOKEN_1),
                parameters.getValue(OUT_TOKEN_2),
                parameters.getValue(OUT_TOKEN_3),
                SlackClient
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            slackConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Token 1",
                    "outToken1",
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Token 2",
                    "outToken2",
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Token 3",
                    "outToken3",
                    true
                )
            ),
            resourceAsString("/slack.svg")
        )

}

internal class SlackConnectorProviderService : ConnectorProvider by SlackConnectorProvider