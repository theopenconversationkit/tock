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

package ai.tock.bot.test

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorMessageProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericMessage
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
    val delay: Long,
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
    fun assertText(
        text: String,
        errorMessage: String? = null,
    ) = assertEquals(text, text(), errorMessage)

    /**
     * Assert that log contains specified text.
     */
    infix fun assert(text: String) = assertText(text)

    /**
     * Assert that log contains specified [ConnectorMessage].
     */
    fun assertMessage(
        message: ConnectorMessage,
        errorMessage: String? = null,
    ) = assertEquals(message, message(message.connectorType), errorMessage)

    /**
     * Assert that log contains specified message.
     */
    infix fun assert(message: ConnectorMessageProvider) = assertMessage(message.toConnectorMessage())

    /**
     * Convert current BotBusLog action first message to a generic message
     */
    fun genericMessage(): GenericMessage? =
        (action as? SendSentence)
            ?.messages
            ?.let { if (it.size == 1) it.first() else null }
            ?.toGenericMessage()

    /**
     * Retrieve choice member with expected title belonging to element with specified index
     */
    fun elementChoice(
        elementIndex: Int,
        title: String,
    ): Choice? =
        genericMessage()?.subElements?.get(elementIndex)
            ?.choices
            ?.find(hasTitle(title))

    /**
     * Retrieve choice member of main part of generic message with expected title
     */
    fun choice(title: String): Choice? =
        genericMessage()?.choices
            ?.find(hasTitle(title))

    private fun hasTitle(title: String): (Choice) -> Boolean = { it.parameters[SendChoice.TITLE_PARAMETER] == title }
}
