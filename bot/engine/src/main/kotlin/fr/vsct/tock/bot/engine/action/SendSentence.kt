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

package fr.vsct.tock.bot.engine.action

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.dialog.ActionState
import fr.vsct.tock.bot.engine.dialog.BotMetadata
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.message.Sentence
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.shared.Dice
import java.time.Instant

/**
 * The most important [Action] class.
 * Could be a simple text, or a complex message using [ConnectorMessage].
 */
class SendSentence(
        playerId: PlayerId,
        applicationId: String,
        recipientId: PlayerId,
        val text: String?,
        val messages: MutableList<ConnectorMessage> = mutableListOf(),
        id: String = Dice.newId(),
        date: Instant = Instant.now(),
        state: ActionState = ActionState(),
        botMetadata: BotMetadata = BotMetadata()) : Action(playerId, recipientId, applicationId, id, date, state, botMetadata) {


    fun message(type: ConnectorType): ConnectorMessage? {
        return messages.find { it.connectorType == type }
    }

    fun hasMessage(type: ConnectorType): Boolean {
        return messages.any { it.connectorType == type }
    }

    override fun toMessage(): Message {
        return Sentence(text, messages)
    }

    override fun toString(): String {
        return "$text ${if (messages.isNotEmpty()) messages.toString() else ""}"
    }
}