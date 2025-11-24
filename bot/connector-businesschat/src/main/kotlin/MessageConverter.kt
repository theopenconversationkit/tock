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

package ai.tock.bot.connector.businesschat

import ai.tock.bot.connector.businesschat.model.common.MessageType
import ai.tock.bot.connector.businesschat.model.common.ReceivedModel
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorTextMessage
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType

internal object MessageConverter {
    /**
     * Converts a [BotBus] [Action] to a [BusinessChatConnectorMessage]
     */
    fun toMessage(action: Action): BusinessChatConnectorMessage? {
        return if (action is SendSentence) {
            if (action.text == null) {
                action.messages.firstOrNull() as? BusinessChatConnectorMessage
            } else {
                BusinessChatConnectorTextMessage(
                    sourceId = action.playerId.id,
                    destinationId = action.recipientId.id,
                    body = action.text.toString(),
                )
            }
        } else {
            null
        }
    }

    /**
     * Converts a message to a [Event]
     */
    fun toEvent(
        message: ReceivedModel,
        connectorId: String,
        businessChatClient: CSPBusinessChatClient,
    ): Event? =
        when (message.type) {
            MessageType.text -> {
                SendSentence(
                    applicationId = connectorId,
                    playerId = PlayerId(message.sourceId, PlayerType.user),
                    recipientId = PlayerId(message.destinationId, PlayerType.bot),
                    text = message.body,
                )
            }
            MessageType.interactive -> {
                val listPickerChoice = businessChatClient.receiveListPickerChoice(message)
                if (listPickerChoice != null) {
                    SendSentence(
                        applicationId = connectorId,
                        playerId = PlayerId(message.sourceId, PlayerType.user),
                        recipientId = PlayerId(message.destinationId, PlayerType.bot),
                        text = listPickerChoice.text,
                    )
                } else {
                    null
                }
            }
            MessageType.pass_thread_control -> {
                businessChatClient.integrationService.parseThreadControl(message, connectorId)
            }
            else -> null
        }
}
