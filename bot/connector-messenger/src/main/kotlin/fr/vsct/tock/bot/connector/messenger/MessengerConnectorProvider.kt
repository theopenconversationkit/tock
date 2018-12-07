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

package fr.vsct.tock.bot.connector.messenger

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
internal object MessengerConnectorProvider : ConnectorProvider {

    private const val APP_ID = "appId"
    private const val PAGE_ID = "pageId"
    private const val TOKEN = "token"
    private const val VERIFY_TOKEN = "verifyToken"
    private const val SECRET = "secret"

    override val connectorType: ConnectorType get() = messengerConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return MessengerConnector(
                parameters[APP_ID]?.takeIf { it.isNotBlank() } ?: connectorId,
                path,
                parameters.getValue(PAGE_ID),
                "${parameters.getValue(APP_ID)}|${parameters.getValue(SECRET)}",
                parameters.getValue(TOKEN),
                parameters[VERIFY_TOKEN],
                MessengerClient(parameters.getValue(SECRET))
            )
        }
    }

    override fun check(connectorConfiguration: ConnectorConfiguration): List<String> =
        super.check(connectorConfiguration) +
                with(connectorConfiguration) {
                    listOfNotNull(
                        if (parameters[PAGE_ID].isNullOrBlank()) "page id is mandatory" else null,
                        if (parameters[TOKEN].isNullOrBlank()) "token is mandatory" else null,
                        if (parameters[SECRET].isNullOrBlank()) "secret is mandatory" else null
                    )
                }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            messengerConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Application Id",
                    APP_ID,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Page Id",
                    PAGE_ID,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Call Token",
                    TOKEN,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Webhook token",
                    VERIFY_TOKEN,
                    false
                ),
                ConnectorTypeConfigurationField(
                    "Secret",
                    SECRET,
                    true
                )
            ),
            resourceAsString("/messenger.svg")
        )
}

internal class MessengerConnectorProviderService : ConnectorProvider by MessengerConnectorProvider