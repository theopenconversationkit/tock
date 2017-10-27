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

package fr.vsct.tock.bot.connector.ga

import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorConfigurationRepository

/**
 * Add a google assistant connector.
 */
fun addGoogleAssistantConnector(
        /**
         * Application id have to be different for each google assistant app served by the bot.
         */
        applicationId: String = "app",
        /**
         * The relative connector path.
         */
        path: String = "/ga/$applicationId",
        /**
         * Which are the google actions project ids?
         * If empty, no JWT is verified (so all project are allowed)
         * see (https://developers.google.com/actions/reference/rest/verify-requests)
         */
        allowedProjectIds: Set<String> = emptySet()
) {

    ConnectorConfigurationRepository.addConfiguration(
            GAConnectorProvider.newConfiguration(
                    applicationId,
                    path,
                    allowedProjectIds)
    )
    BotRepository.registerConnectorProvider(GAConnectorProvider)
}