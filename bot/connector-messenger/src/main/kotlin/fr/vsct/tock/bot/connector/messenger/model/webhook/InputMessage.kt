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

package fr.vsct.tock.bot.connector.messenger.model.webhook

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.messenger.MessengerConnectorProvider
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.Sender
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType

data class InputMessage(val sender: Sender,
                        val recipient: Recipient,
                        val timestamp: Long,
                        val message: Message?,
                        val postback: Postback?,
                        val optin: Optin?) : ConnectorMessage {

    override val connectorType: ConnectorType get() = MessengerConnectorProvider.connectorType

    fun playerId(playerType: PlayerType): PlayerId = PlayerId(sender.id, playerType)

    fun recipientId(playerType: PlayerType): PlayerId = PlayerId(recipient.id, playerType)
}