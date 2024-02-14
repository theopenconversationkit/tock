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

    private const val APP_ID = "appId"
    private const val PHONE_NUMBER_ID = "phoneNumberId"
    private const val TOKEN = "token"
    private const val VERIFY_TOKEN = "verifyToken"
    private const val SECRET = "secret"
    private const val MODE = "mode"

    override val connectorType: ConnectorType get() = ConnectorType("whatsapp_cloud")

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration){
            val appId = parameters[APP_ID]?.takeIf { it.isNotBlank() } ?: connectorId
            return WhatsAppConnectorCloud(
                connectorId,
                appId,
                parameters.getValue(PHONE_NUMBER_ID),
                path,
                "$appId|${parameters.getValue(SECRET)}",
                parameters.getValue(TOKEN),
                parameters[VERIFY_TOKEN],
                parameters.getValue(MODE),
                WhatsAppCloudClient(parameters.getValue(SECRET))

            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            ConnectorType("whatsapp_cloud"),
            listOf(
                ConnectorTypeConfigurationField(
                    "Application Id",
                    APP_ID,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Phone Number Id",
                    PHONE_NUMBER_ID,
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
                ConnectorTypeConfigurationField(
                    "Secret",
                    SECRET,
                    true
                )
            ),
            resourceAsString("/whatsapp.svg")
        )
}

internal class WhatsAppConnectorCloudProviderService : ConnectorProvider by WhatsAppConnectorCloudProvider