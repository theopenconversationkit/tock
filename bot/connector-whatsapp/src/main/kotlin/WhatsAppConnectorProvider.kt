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
package fr.vsct.tock.bot.connector.whatsapp

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ConnectorTypeConfiguration
import fr.vsct.tock.bot.connector.ConnectorTypeConfigurationField
import fr.vsct.tock.shared.resourceAsString

internal object WhatsAppConnectorProvider : ConnectorProvider {

    private const val WHATS_APP_URL = "whatsAppUrl"
    private const val LOGIN = "login"
    private const val PASSWORD = "password"

    override val connectorType: ConnectorType get() = whatsAppConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return WhatsAppConnector(
                connectorId,
                path,
                parameters.getValue(WHATS_APP_URL),
                parameters.getValue(LOGIN),
                parameters.getValue(PASSWORD),
                createRequestFilter(connectorConfiguration)
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            whatsAppConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "WhatsApp Url",
                    WHATS_APP_URL,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "User Login",
                    LOGIN,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "User Password",
                    PASSWORD,
                    true
                )
            ) + ConnectorTypeConfiguration.commonSecurityFields(),
            resourceAsString("/whatsapp.svg")
        )

}

internal class WhatsAppConnectorProviderService : ConnectorProvider by WhatsAppConnectorProvider