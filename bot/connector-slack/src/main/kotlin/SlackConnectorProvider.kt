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

package ai.tock.bot.connector.slack

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.bot.connector.slack.model.SlackMessageOut
import ai.tock.shared.resourceAsString
import kotlin.reflect.KClass

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
                parameters.getValue(OUT_TOKEN_1),
                parameters.getValue(OUT_TOKEN_2),
                parameters.getValue(OUT_TOKEN_3),
                SlackClient,
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            slackConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Token 1",
                    OUT_TOKEN_1,
                    true,
                ),
                ConnectorTypeConfigurationField(
                    "Token 2",
                    OUT_TOKEN_2,
                    true,
                ),
                ConnectorTypeConfigurationField(
                    "Token 3",
                    OUT_TOKEN_3,
                    true,
                ),
            ),
            resourceAsString("/slack.svg"),
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(SlackMessageOut::class)
}

internal class SlackConnectorProviderService : ConnectorProvider by SlackConnectorProvider
