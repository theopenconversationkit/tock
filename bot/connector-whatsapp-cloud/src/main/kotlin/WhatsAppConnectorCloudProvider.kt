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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.shared.resourceAsString

internal object WhatsAppConnectorCloudProvider : ConnectorProvider {

    internal const val WHATSAPP_PHONE_NUMBER_ID = "whatsAppPhoneNumberId"
    internal const val WHATSAPP_BUSINESS_ACCOUNT_ID = "whatsAppBusinessAccountId"
    internal const val META_APPLICATION_ID = "metaApplicationId"
    internal const val TOKEN = "token"
    private const val VERIFY_TOKEN = "verifyToken"

    override val connectorType: ConnectorType get() = whatsAppCloudConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration){
            return WhatsAppConnectorCloudConnector(
                connectorId = connectorId,
                phoneNumberId = parameters.getValue(WHATSAPP_PHONE_NUMBER_ID),
                whatsAppBusinessAccountId = parameters.getValue(WHATSAPP_BUSINESS_ACCOUNT_ID),
                metaApplicationId = parameters[META_APPLICATION_ID],
                path = path,
                verifyToken = parameters[VERIFY_TOKEN],
                requestFilter = createRequestFilter(connectorConfiguration),
                client = createCloudApiClient(this),
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            whatsAppCloudConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "WhatsApp Phone Number Id",
                    WHATSAPP_PHONE_NUMBER_ID,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "WhatsApp Business Account Id",
                        WHATSAPP_BUSINESS_ACCOUNT_ID,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Webhook verify token",
                    VERIFY_TOKEN,
                    false
                ),
                ConnectorTypeConfigurationField(
                    "Call Token",
                    TOKEN,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Meta Application Id",
                    META_APPLICATION_ID,
                    false
                ),
            ),
            resourceAsString("/whatsapp.svg")
        )
}

internal class WhatsAppConnectorCloudProviderService : ConnectorProvider by WhatsAppConnectorCloudProvider
