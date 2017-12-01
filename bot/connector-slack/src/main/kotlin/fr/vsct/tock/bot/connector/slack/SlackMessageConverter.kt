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

import fr.vsct.tock.bot.connector.slack.model.SlackConnectorMessage
import fr.vsct.tock.bot.connector.slack.model.SlackMessageOut
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import mu.KotlinLogging


internal object SlackMessageConverter {

    val logger = KotlinLogging.logger {}

    fun toMessageOut(action: Action): SlackConnectorMessage? {
        logger.info { action.javaClass }
        return when (action) {
            is SendSentence ->
                if (action.hasMessage(SlackConnectorProvider.connectorType) ) {
                    action.message(SlackConnectorProvider.connectorType) as SlackConnectorMessage
                } else {
                    SlackMessageOut(action.stringText ?: "")
                }
            else -> {
                logger.warn { "Action $action not supported" }
                null
            }
        }
    }
}

