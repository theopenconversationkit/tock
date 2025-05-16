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

package ai.tock.bot.connector.businesschat

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorImageMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorListPickerMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorRichLinkMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorTextMessage
import ai.tock.shared.resourceAsString
import ai.tock.translator.UserInterfaceType.textAndVoiceAssistant
import kotlin.reflect.KClass

internal const val BUSINESS_CHAT_CONNECTOR_TYPE_ID = "businesschat"
/**
 * The Business Chat connector type.
 */
val businessChatConnectorType = ConnectorType(BUSINESS_CHAT_CONNECTOR_TYPE_ID, textAndVoiceAssistant)

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
            ),
            resourceAsString("/businesschat.svg")
        )
    }

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(
        BusinessChatConnectorImageMessage::class,
        BusinessChatConnectorListPickerMessage::class,
        BusinessChatConnectorRichLinkMessage::class,
        BusinessChatConnectorTextMessage::class
    )
}

internal class BusinessChatConnectorProviderService : ConnectorProvider by BusinessChatConnectorProvider
