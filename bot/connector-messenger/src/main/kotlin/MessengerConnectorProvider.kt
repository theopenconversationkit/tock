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

package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.bot.connector.messenger.model.send.AttachmentMessage
import ai.tock.bot.connector.messenger.model.send.TextMessage
import ai.tock.shared.resourceAsString
import kotlin.reflect.KClass

/**
 *
 */
internal object MessengerConnectorProvider : ConnectorProvider {
    private const val APP_ID = "appId"
    private const val PAGE_ID = "pageId"
    private const val TOKEN = "token"
    private const val VERIFY_TOKEN = "verifyToken"
    private const val SECRET = "secret"
    private const val PERSONA_ID = "personaId"

    override val connectorType: ConnectorType get() = messengerConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            val appId = parameters[APP_ID]?.takeIf { it.isNotBlank() } ?: connectorId
            return MessengerConnector(
                connectorId.trim(),
                appId.trim(),
                path.trim(),
                parameters.getValue(PAGE_ID),
                "$appId|${parameters.getValue(SECRET)}",
                parameters.getValue(TOKEN),
                parameters[VERIFY_TOKEN],
                MessengerClient(parameters.getValue(SECRET)),
                personaId = parameters[PERSONA_ID]?.takeUnless { it.isBlank() },
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            messengerConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Application Id",
                    APP_ID,
                    true,
                ),
                ConnectorTypeConfigurationField(
                    "Page Id",
                    PAGE_ID,
                    true,
                ),
                ConnectorTypeConfigurationField(
                    "Call Token",
                    TOKEN,
                    true,
                ),
                ConnectorTypeConfigurationField(
                    "Webhook token",
                    VERIFY_TOKEN,
                    false,
                ),
                ConnectorTypeConfigurationField(
                    "Secret",
                    SECRET,
                    true,
                ),
                ConnectorTypeConfigurationField(
                    "Persona Id",
                    PERSONA_ID,
                    false,
                ),
            ),
            resourceAsString("/messenger.svg"),
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> =
        setOf(
            AttachmentMessage::class,
            TextMessage::class,
        )
}

internal class MessengerConnectorProviderService : ConnectorProvider by MessengerConnectorProvider
