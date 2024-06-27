/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.bot.connector.*
import ai.tock.shared.resourceAsString

internal object WhatsAppConnectorCloudProvider : ConnectorProvider {

    internal const val WHATSAPP_PHONE_NUMBER_ID = "whatsAppPhoneNumberId"
    private const val WHATSAPP_BUSINESS_ACCOUNT_ID = "whatsAppBusinessAccountId"
    internal const val TOKEN = "token"
    private const val VERIFY_TOKEN = "verifyToken"
    internal const val SECRET = "secret"
    private const val MODE = "mode"

    override val connectorType: ConnectorType get() = whatsAppCloudConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration){
            return WhatsAppConnectorCloudConnector(
                connectorId = connectorId,
                phoneNumberId = parameters.getValue(WHATSAPP_PHONE_NUMBER_ID),
                whatsAppBusinessAccountId = parameters.getValue(WHATSAPP_BUSINESS_ACCOUNT_ID),
                path = path,
                token = parameters.getValue(TOKEN),
                verifyToken = parameters[VERIFY_TOKEN],
                mode = parameters.getValue(MODE),
                client = createCloudApiClient(this),
                requestFilter = createRequestFilter(connectorConfiguration)

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
                    "Mode",
                    MODE,
                    true
                ),
            ),
            resourceAsString("/whatsapp.svg")
        )
}

internal class WhatsAppConnectorCloudProviderService : ConnectorProvider by WhatsAppConnectorCloudProvider
