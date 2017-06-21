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

package fr.vsct.tock.bot.connector.rest

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorConfigurationRepository

/**
 * Add a rest connector.
 */
fun addRestConnector(
        /**
         * Application id. Must be unique.
         */
        applicationId: String,
        /**
         * The http listening base path.
         */
        path: String = "/rest-connector",
        /**
         * The name of the application.
         */
        name: String = applicationId,
        /**
         * The base url for the connector path.
         */
        baseUrl: String? = null
): ConnectorConfiguration {

    val configuration = ConnectorConfiguration(
            applicationId,
            path,
            restConnectorType,
            name,
            baseUrl
    )
    ConnectorConfigurationRepository.addConfiguration(configuration)
    BotRepository.registerConnectorProvider(RestConnectorProvider)
    return configuration
}

fun addRestConnector(botConfiguration: BotApplicationConfiguration): ConnectorConfiguration {
    return addRestConnector(
            "rest-${botConfiguration.applicationId}",
            "/rest/rest-${botConfiguration.applicationId}",
            botConfiguration.name,
            botConfiguration.baseUrl)
}