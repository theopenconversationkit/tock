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

package fr.vsct.tock.bot.test

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.alexa.AlexaMessage
import fr.vsct.tock.bot.connector.alexa.alexaConnectorType
import fr.vsct.tock.bot.connector.ga.GAResponseConnectorMessage
import fr.vsct.tock.bot.connector.ga.gaConnectorType
import fr.vsct.tock.bot.connector.messenger.messengerConnectorType
import fr.vsct.tock.bot.connector.messenger.model.MessengerConnectorMessage
import fr.vsct.tock.bot.connector.slack.model.SlackConnectorMessage
import fr.vsct.tock.bot.connector.slack.slackConnectorType
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import kotlin.test.assertEquals

/**
 * The actions sent by the mocked bus.
 */
data class BotBusMockLog(
    /**
     * The action sent.
     */
    val action: Action,
    /**
     * The delay before the action is sent.
     */
    val delay: Long
) {

    /**
     * The message of the specified [ConnectorType] if it exists.
     */
    fun message(connectorType: ConnectorType): ConnectorMessage? = (action as? SendSentence)?.message(connectorType)

    /**
     * The Messenger message if any.
     */
    fun messenger(): MessengerConnectorMessage? = message(messengerConnectorType) as? MessengerConnectorMessage

    /**
     * The Google Assistant message if any.
     */
    fun ga(): GAResponseConnectorMessage? = message(gaConnectorType) as? GAResponseConnectorMessage

    /**
     * The Slack message if any.
     */
    fun slack(): SlackConnectorMessage? = message(slackConnectorType) as? SlackConnectorMessage

    /**
     * The Alexa message if any.
     */
    fun alexa(): AlexaMessage? = message(alexaConnectorType) as? AlexaMessage

    /**
     * The text message if any.
     */
    fun text(): String? = (action as SendSentence).stringText

    /**
     * Assert that log contains specified text.
     */
    fun assertText(text: String, errorMessage: String? = null) = assertEquals(text, text(), errorMessage)

    /**
     * Assert that log contains specified text.
     */
    infix fun assert(text: String) = assertText(text)

    /**
     * Assert that log contains specified [ConnectorMessage].
     */
    fun assertMessage(message: ConnectorMessage, errorMessage: String? = null) =
        assertEquals(message, message(message.connectorType), errorMessage)

    /**
     * Assert that log contains specified message.
     */
    infix fun assert(message: ConnectorMessage) = assertMessage(message)

}