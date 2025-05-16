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

package ai.tock.bot.connector.twitter

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import ai.tock.shared.resourceAsString
import kotlin.reflect.KClass

internal object TwitterConnectorProvider : ConnectorProvider {

    private const val APP_ID = "appId"
    private const val ACCOUNT_ID = "accountId"
    private const val ENVIRONMENT = "develop"
    private const val CONSUMER_KEY = "consumerKey"
    private const val CONSUMER_SECRET = "consumerSecret"
    private const val TOKEN = "token"
    private const val SECRET = "secret"

    /**
     * The connector type provided
     */
    override val connectorType: ConnectorType get() = twitterConnectorType

    /**
     * Provides a new [Connector] instance from the specified [ConnectorConfiguration].
     */
    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            val appId = parameters.getValue(APP_ID)
            val accountId = parameters.getValue(ACCOUNT_ID)
            return TwitterConnector(
                appId,
                accountId,
                getBaseUrl(),
                path,
                TwitterClient(
                    parameters.getValue(ENVIRONMENT),
                    parameters.getValue(CONSUMER_KEY),
                    parameters.getValue(CONSUMER_SECRET),
                    parameters.getValue(TOKEN),
                    parameters.getValue(SECRET)
                )
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            twitterConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Application Id",
                    APP_ID,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Account Id",
                    ACCOUNT_ID,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Environment",
                    ENVIRONMENT,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Consumer Key",
                    CONSUMER_KEY,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Consumer Secret",
                    CONSUMER_SECRET,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Token",
                    TOKEN,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Secret",
                    SECRET,
                    true
                )
            ),
            resourceAsString("/twitter.svg")
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(OutcomingEvent::class)
}

internal class TwitterConnectorProviderService : ConnectorProvider by TwitterConnectorProvider
