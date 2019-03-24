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

package fr.vsct.tock.bot.connector.twitter

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ConnectorTypeConfiguration
import fr.vsct.tock.bot.connector.ConnectorTypeConfigurationField
import fr.vsct.tock.shared.resourceAsString

internal object TwitterConnectorProvider : ConnectorProvider {

    private const val APP_ID = "appId"
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
            return TwitterConnector(
                appId,
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

    override fun check(connectorConfiguration: ConnectorConfiguration): List<String> =
        super.check(connectorConfiguration) +
                with(connectorConfiguration) {
                    listOfNotNull(
                        if (parameters[APP_ID].isNullOrBlank()) "application id is mandatory" else null,
                        if (parameters[ENVIRONMENT].isNullOrBlank()) "enviroment is mandatory" else null,
                        if (parameters[CONSUMER_KEY].isNullOrBlank()) "consumer key is mandatory" else null,
                        if (parameters[CONSUMER_SECRET].isNullOrBlank()) "consumer secret is mandatory" else null,
                        if (parameters[TOKEN].isNullOrBlank()) "token is mandatory" else null,
                        if (parameters[SECRET].isNullOrBlank()) "secret is mandatory" else null
                    )
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
}

internal class TwitterConnectorProviderService : ConnectorProvider by TwitterConnectorProvider