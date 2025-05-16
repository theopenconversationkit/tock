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

import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.whatsapp.cloud.WhatsAppConnectorCloudProvider.TOKEN
import ai.tock.bot.connector.whatsapp.cloud.WhatsAppConnectorCloudProvider.WHATSAPP_BUSINESS_ACCOUNT_ID
import ai.tock.bot.connector.whatsapp.cloud.WhatsAppConnectorCloudProvider.WHATSAPP_PHONE_NUMBER_ID
import java.util.concurrent.ConcurrentHashMap

private val cloudApiClientCache = ConcurrentHashMap<String, WhatsAppCloudApiClient>()

internal fun createCloudApiClient(connectorConfiguration: ConnectorConfiguration): WhatsAppCloudApiClient =
    WhatsAppCloudApiClient(
        connectorConfiguration.parameters.getValue(TOKEN),
        connectorConfiguration.parameters.getValue(WHATSAPP_BUSINESS_ACCOUNT_ID),
        connectorConfiguration.parameters.getValue(WHATSAPP_PHONE_NUMBER_ID),
    )
        .apply {
            cloudApiClientCache[connectorConfiguration.connectorId] = this
        }

/**
 * Allow to retrieve the WhatsAppCloudApiClient from the cache.
 */
fun getWhatsAppCloudApiClient(connectorId: String? = null): WhatsAppCloudApiClient? =
    cloudApiClientCache[connectorId] ?: cloudApiClientCache.values.firstOrNull()

