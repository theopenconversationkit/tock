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

package ai.tock.bot.connector.messenger.model.webhook

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ai.tock.bot.connector.messenger.json.webhook.WebhookDeserializer
import ai.tock.bot.connector.messenger.model.MessengerConnectorMessage
import ai.tock.bot.connector.messenger.model.Recipient
import ai.tock.bot.connector.messenger.model.Sender
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType

/**
 *
 */
@JsonDeserialize(using = WebhookDeserializer::class)
abstract class Webhook : MessengerConnectorMessage() {

    abstract val sender: Sender?
    abstract val recipient: Recipient
    abstract val timestamp: Long
    open val priorMessage: PriorMessage? get() = null

    override fun toGenericMessage(): GenericMessage? {
        return null
    }

    open fun playerId(playerType: PlayerType): PlayerId =
        PlayerId(sender?.id ?: error("null sender field in webhook"), playerType)

    open fun recipientId(playerType: PlayerType): PlayerId = PlayerId(
        recipient.id ?: recipient.userRef ?: error("id or userRef must not be null"),
        playerType
    )
}