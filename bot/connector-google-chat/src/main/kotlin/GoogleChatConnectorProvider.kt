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
package ai.tock.bot.connector.googlechat

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.bot.connector.googlechat.builder.googleChatConnectorType
import ai.tock.shared.resourceAsStream
import ai.tock.shared.resourceAsString
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.chat.v1.HangoutsChat
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.reflect.KClass

private const val CHAT_SCOPE = "https://www.googleapis.com/auth/chat.bot"
private const val SERVICE_CREDENTIAL_PATH_PARAMETER = "serviceCredentialPath"
private const val SERVICE_CREDENTIAL_CONTENT_PARAMETER = "serviceCredentialContent"
private const val BOT_PROJECT_NUMBER_PARAMETER = "botProjectNumber"
private const val CONDENSED_FOOTNOTES_PARAMETER = "useCondensedFootnotes"
private const val TRUNCATE_URLS_PARAMETER = "truncateUrls"

internal object GoogleChatConnectorProvider : ConnectorProvider {

    override val connectorType: ConnectorType get() = googleChatConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {

            val credentialInputStream =
                connectorConfiguration.parameters[SERVICE_CREDENTIAL_PATH_PARAMETER]
                    ?.let { resourceAsStream(it) }
                    ?: connectorConfiguration.parameters[SERVICE_CREDENTIAL_CONTENT_PARAMETER]
                        ?.let { ByteArrayInputStream(it.toByteArray()) }
                    ?: error("Service credential missing : either $SERVICE_CREDENTIAL_PATH_PARAMETER or $SERVICE_CREDENTIAL_CONTENT_PARAMETER must be provided")

            val requestInitializer: HttpRequestInitializer =
                HttpCredentialsAdapter(loadCredentials(credentialInputStream))

            val useCondensedFootnotes =
                connectorConfiguration.parameters[CONDENSED_FOOTNOTES_PARAMETER] == "1"

            val truncateUrls = connectorConfiguration.parameters[TRUNCATE_URLS_PARAMETER] == "1"

            val chatService = HangoutsChat.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                requestInitializer
            )
                .setApplicationName(connectorId)
                .build()

            val authorisationHandler = GoogleChatAuthorisationHandler(
                connectorConfiguration.parameters[BOT_PROJECT_NUMBER_PARAMETER]
                    ?: error("Parameter Bot project number not present")
            )

            return GoogleChatConnector(
                connectorId,
                path,
                chatService,
                authorisationHandler,
                useCondensedFootnotes,
                truncateUrls
            )
        }
    }

    private fun loadCredentials(inputStream: InputStream): GoogleCredentials =
        ServiceAccountCredentials
            .fromStream(inputStream)
            .createScoped(CHAT_SCOPE)

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            googleChatConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Bot project number (application ID in google hangouts configuration page)",
                    BOT_PROJECT_NUMBER_PARAMETER,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Service account credential file path (default : /service-account-{connectorId}.json)",
                    SERVICE_CREDENTIAL_PATH_PARAMETER,
                    false
                ),
                ConnectorTypeConfigurationField(
                    "Service account credential json content",
                    SERVICE_CREDENTIAL_CONTENT_PARAMETER,
                    false
                ),
                ConnectorTypeConfigurationField(
                    "Use condensed footnotes (true = 1, false = 0)",
                    CONDENSED_FOOTNOTES_PARAMETER,
                    false
                ),
                ConnectorTypeConfigurationField(
                    "Truncate long URLs (true = 1, false = 0)",
                    TRUNCATE_URLS_PARAMETER,
                    false
                )
            ),
            svgIcon = resourceAsString("/google_chat.svg")
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> =
        setOf(GoogleChatConnectorTextMessageOut::class)
}

internal class GoogleChatConnectorProviderService : ConnectorProvider by GoogleChatConnectorProvider
