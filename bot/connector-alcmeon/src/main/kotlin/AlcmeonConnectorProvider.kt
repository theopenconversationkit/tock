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
package ai.tock.bot.connector.alcmeon

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.shared.resourceAsString
import kotlin.reflect.KClass

private const val APPLICATION_SECRET_FIELD = "application_secret"
private const val SUB_BOT_DESCRIPTION_FIELD = "sub_bot_description"

internal object AlcmeonConnectorProvider : ConnectorProvider {

    override val connectorType: ConnectorType get() = alcmeonConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            val applicationSecret = parameters[APPLICATION_SECRET_FIELD]
                ?: error("Alcmeon application secret should be provided")

            return AlcmeonConnector(
                connectorId = connectorId,
                path = path,
                description = parameters[SUB_BOT_DESCRIPTION_FIELD]
                    ?: error("Alcmeon subbot description should be provided"),
                authorisationHandler = AlcmeonAuthorisationHandler(applicationSecret)
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            connectorType = alcmeonConnectorType,
            fields = listOf(
                ConnectorTypeConfigurationField("SubBot description", SUB_BOT_DESCRIPTION_FIELD, true),
                ConnectorTypeConfigurationField("Application secret ", APPLICATION_SECRET_FIELD, true),
            ),
            svgIcon = resourceAsString("/alcmeon.svg")
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> =
        setOf(AlcmeonConnectorMessageResponse::class)
}

internal class AlcmeonConnectorProviderService : ConnectorProvider by AlcmeonConnectorProvider


internal const val ALCMEON_CONNECTOR_TYPE_ID = "alcmeon"

val alcmeonConnectorType = ConnectorType(
    ALCMEON_CONNECTOR_TYPE_ID,
)



