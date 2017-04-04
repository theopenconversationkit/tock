/*
 * Copyright (C) 2017 VSCT
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

package fr.vsct.tock.bot.connector.messenger

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.shared.mapNotNullValues

/**
 *
 */
internal object MessengerConnectorProvider : ConnectorProvider {

    private const val PAGE_ID = "pageId"
    private const val TOKEN = "token"
    private const val VERIFY_TOKEN = "verifyToken"
    private const val SECRET = "secret"

    override val connectorType: ConnectorType = ConnectorType("messenger")
    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return MessengerConnector(
                    applicationId,
                    path,
                    parameters.getValue(PAGE_ID),
                    parameters.getValue(TOKEN),
                    parameters.get(VERIFY_TOKEN),
                    MessengerClient(parameters.getValue(SECRET)))
        }
    }

    /**
     * Create a new messenger connector configuration.
     */
    fun newConfiguration(
            pageId: String,
            pageToken: String,
            applicationSecret: String,
            webhookVerifyToken: String? = null,
            applicationId: String = pageId,
            path: String = "/messenger"): ConnectorConfiguration {


        return ConnectorConfiguration(
                applicationId,
                path,
                connectorType,
                mapNotNullValues(
                        PAGE_ID to pageId,
                        TOKEN to pageToken,
                        SECRET to applicationSecret,
                        VERIFY_TOKEN to webhookVerifyToken
                ))
    }
}