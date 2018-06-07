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

/**
 * Adds a slack connector.
 */
@Deprecated("This method is not used anymore and has no effect")
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
) {
}
