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

package fr.vsct.tock.bot.connector.slack

import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorConfigurationRepository

/**
 * Adds a slack connector.
 */
fun BotDefinition.addSlackConnector(
    outToken1: String,
    outToken2: String,
    outToken3: String,
    /**
     * Should be unique for each connector.
     */
    connectorId: String = "slackApp",
    path: String = "/slack/$connectorId",
    baseUrl: String? = null
) = addSlackConnector(connectorId, path, nlpModelName, outToken1, outToken2, outToken3, baseUrl)

/**
 * Adds a slack connector.
 */
@Deprecated("use addSlackConnector with botDefinition receiver")
fun addSlackConnector(
    /**
     * Should be unique for each connector.
     */
    connectorId: String,
    path: String,
    /**
     * The name of the bot.
     */
    applicationName: String,
    outToken1: String,
    outToken2: String,
    outToken3: String,
    baseUrl: String? = null
) {
    ConnectorConfigurationRepository.addConfiguration(
        SlackConnectorProvider.newConfiguration(
            connectorId,
            path,
            outToken1,
            outToken2,
            outToken3,
            applicationName,
            baseUrl
        )
    )
    BotRepository.registerConnectorProvider(SlackConnectorProvider)
}

