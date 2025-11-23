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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.shared.resourceAsString
import kotlin.reflect.KClass

/**
 *
 */
internal object GAConnectorProvider : ConnectorProvider {
    private const val PROJECT_IDS = "_project_ids"
    private const val PROJECT_ID_SEPARATOR = ","

    override val connectorType: ConnectorType get() = gaConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return GAConnector(
                connectorId,
                path,
                parameters[PROJECT_IDS]
                    ?.split(PROJECT_ID_SEPARATOR)
                    ?.filter { it.isNotBlank() }
                    ?.toSet()
                    ?: emptySet(),
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            gaConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Restricted project ids",
                    "_project_ids",
                    false,
                ),
            ),
            resourceAsString("/ga.svg"),
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(GAResponseConnectorMessage::class)
}

internal class GAConnectorProviderService : ConnectorProvider by GAConnectorProvider
