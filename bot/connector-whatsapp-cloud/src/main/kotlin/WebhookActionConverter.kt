/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.connector.whatsapp.cloud.UserHashedIdCache.createHashedId
import ai.tock.bot.connector.whatsapp.cloud.database.repository.PayloadWhatsAppCloudDAO
import ai.tock.bot.connector.whatsapp.cloud.database.repository.PayloadWhatsAppCloudMongoDAO
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.*
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendLocation
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserLocation


internal object WebhookActionConverter {

    private val payloadWhatsApp: PayloadWhatsAppCloudDAO = PayloadWhatsAppCloudMongoDAO

    fun toEvent(message: WhatsAppCloudMessage, applicationId: String, client: WhatsAppCloudApiClient): Event? {
        val senderId = createHashedId(message.from)
        return when (message) {
            is WhatsAppCloudTextMessage -> SendSentence(
                PlayerId(senderId),
                applicationId,
                PlayerId(applicationId, PlayerType.bot),
                message.text.body
            )

            is WhatsAppCloudLocationMessage -> SendLocation(
                PlayerId(senderId),
                applicationId,
                PlayerId(applicationId, PlayerType.bot),
                UserLocation(message.location.latitude, message.location.longitude)
            )

            is WhatsAppCloudButtonMessage -> {
                val messageCopy = getMessageButtonCopy(message)
                messageCopy.button.payload.let { payload ->
                    SendChoice.decodeChoice(
                        payload,
                        PlayerId(senderId),
                        applicationId,
                        PlayerId(applicationId, PlayerType.bot),
                        messageCopy.referral?.ref
                    )
                }
            }

            is WhatsAppCloudInteractiveMessage -> {
                val messageCopy = getMessageInteractiveCopy(message)
                messageCopy.interactive.buttonReply?.id?.let { payload ->
                    SendChoice.decodeChoice(
                        payload,
                        PlayerId(senderId),
                        applicationId,
                        PlayerId(applicationId, PlayerType.bot),
                        messageCopy.referral?.ref
                    )
                }
            }

            else -> null
        }
    }

    private fun getMessageInteractiveCopy(
        message: WhatsAppCloudInteractiveMessage
    )
        : WhatsAppCloudInteractiveMessage {
        val payload = payloadWhatsApp.getPayloadById(message.interactive.buttonReply!!.id)
        return if (payload != null) {
            val copyButtonReply = message.interactive.buttonReply.copy(
                id = payload,
                title = message.interactive.buttonReply.title
            )
            val copyInteractive =
                message.interactive.copy(buttonReply = copyButtonReply, listReply = message.interactive.listReply)
            message.copy(
                interactive = copyInteractive,
                id = message.id,
                from = message.from,
                timestamp = message.timestamp,
                context = message.context,
                referral = message.referral,
                errors = message.errors
            )

        } else {
            message
        }
    }

    private fun getMessageButtonCopy(message: WhatsAppCloudButtonMessage): WhatsAppCloudButtonMessage {
        val payload = payloadWhatsApp.getPayloadById(message.button.payload)
        return if (payload != null) {
            val copyButton = message.button.copy(
                payload = payload, text = message.button.text
            )
            message.copy(
                button = copyButton,
                id = message.id,
                from = message.from,
                timestamp = message.timestamp,
                context = message.context,
                referral = message.referral,
                errors = message.errors
            )
        } else {
            message
        }
    }
}