/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.businesschat

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ConnectorTypeConfiguration
import fr.vsct.tock.bot.connector.ConnectorTypeConfigurationField
import fr.vsct.tock.translator.UserInterfaceType

const val BUSINESS_CHAT_CONNECTOR_TYPE_ID = "business chat"
val businessChatConnectorType = ConnectorType(BUSINESS_CHAT_CONNECTOR_TYPE_ID, UserInterfaceType.textAndVoiceAssistant)

/**
 * Defines the configuration to be exposed on the bot admin
 */
internal object BusinessChatConnectorProvider : ConnectorProvider {

    private const val BUSINESS_ID = "businessId"
    override val connectorType: ConnectorType get() = businessChatConnectorType

    /**
     * Instantiates the connector according to the configuration
     */
    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return BusinessChatConnector(path, connectorId, connectorConfiguration.parameters[BUSINESS_ID] ?: "")
        }
    }

    /**
     * Returns the configuration fields for the bot admin
     */
    override fun configuration(): ConnectorTypeConfiguration {
        return ConnectorTypeConfiguration(
            businessChatConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Business Id",
                    BUSINESS_ID,
                    true
                )
            )
        )
    }
}

internal class BusinessChatConnectorProviderService : ConnectorProvider by BusinessChatConnectorProvider