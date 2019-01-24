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