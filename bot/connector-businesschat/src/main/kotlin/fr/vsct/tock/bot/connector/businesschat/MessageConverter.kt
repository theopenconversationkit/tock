/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.businesschat

import fr.vsct.tock.bot.connector.businesschat.model.common.MessageType
import fr.vsct.tock.bot.connector.businesschat.model.csp.message.Message
import fr.vsct.tock.bot.connector.businesschat.model.input.BusinessChatConnectorMessage
import fr.vsct.tock.bot.connector.businesschat.model.input.BusinessChatConnectorTextMessage
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType

internal object MessageConverter {

    /**
     * Converts a [BotBus] [Action] to a [BusinessChatConnectorMessage]
     */
    fun toMessage(action: Action): BusinessChatConnectorMessage? {
        return if (action is SendSentence) {
            if (action.text == null) {
                action.messages.first() as? BusinessChatConnectorMessage
            } else BusinessChatConnectorTextMessage(
                    sourceId = action.playerId.id,
                    destinationId = action.recipientId.id,
                    body = action.text.toString()
            )
        } else null
    }

    /**
     * Converts a message to a [Event]
     */
    fun toEvent(message: Message, connectorId: String): Event? =
            when (message.type) {
                MessageType.text -> {
                    SendSentence(
                            applicationId = connectorId,
                            playerId = PlayerId(message.sourceId, PlayerType.user),
                            recipientId = PlayerId(message.destinationId, PlayerType.bot),
                            text = message.body
                    )
                }
                else -> null
            }
}

