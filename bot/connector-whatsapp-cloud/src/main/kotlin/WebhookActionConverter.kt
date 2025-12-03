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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.connector.whatsapp.cloud.UserHashedIdCache.createHashedId
import ai.tock.bot.connector.whatsapp.cloud.database.repository.PayloadWhatsAppCloudDAO
import ai.tock.bot.connector.whatsapp.cloud.database.repository.PayloadWhatsAppCloudMongoDAO
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsAppCloudButtonMessage
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsAppCloudImageMessage
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsAppCloudInteractiveMessage
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsAppCloudLocationMessage
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsAppCloudMessage
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsAppCloudTextMessage
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendLocation
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserLocation

internal object WebhookActionConverter {
    private val payloadWhatsApp: PayloadWhatsAppCloudDAO = PayloadWhatsAppCloudMongoDAO

    fun toEvent(
        message: WhatsAppCloudMessage,
        applicationId: String,
        whatsAppCloudApiService: WhatsAppCloudApiService,
    ): Event? {
        val senderId = createHashedId(message.from)
        return when (message) {
            is WhatsAppCloudTextMessage ->
                SendSentence(
                    PlayerId(senderId),
                    applicationId,
                    PlayerId(applicationId, PlayerType.bot),
                    message.text.body,
                )

            is WhatsAppCloudImageMessage -> {
                val binaryImg = whatsAppCloudApiService.downloadImgByBinary(message.image.id, message.image.mimeType)
                SendAttachment(
                    PlayerId(senderId),
                    applicationId,
                    PlayerId(applicationId, PlayerType.bot),
                    binaryImg,
                    SendAttachment.AttachmentType.image,
                )
            }

            is WhatsAppCloudLocationMessage ->
                SendLocation(
                    PlayerId(senderId),
                    applicationId,
                    PlayerId(applicationId, PlayerType.bot),
                    UserLocation(message.location.latitude, message.location.longitude),
                )

            is WhatsAppCloudButtonMessage -> {
                val messageCopy = getMessageButtonCopy(message)
                messageCopy.button.payload.let { payload ->
                    SendChoice.decodeChoice(
                        payload,
                        PlayerId(senderId),
                        applicationId,
                        PlayerId(applicationId, PlayerType.bot),
                        messageCopy.referral?.ref,
                    )
                }
            }

            is WhatsAppCloudInteractiveMessage -> {
                val messageCopy = getMessageInteractiveCopy(message)

                val payloadButtonReply = messageCopy.interactive.buttonReply?.id
                val payloadListReply = messageCopy.interactive.listReply?.id

                val payload = payloadButtonReply ?: payloadListReply
                return payload?.let {
                    SendChoice.decodeChoice(
                        it,
                        PlayerId(senderId),
                        applicationId,
                        PlayerId(applicationId, PlayerType.bot),
                        messageCopy.referral?.ref,
                    )
                }
            }

            else -> null
        }
    }

    private fun getMessageInteractiveCopy(message: WhatsAppCloudInteractiveMessage): WhatsAppCloudInteractiveMessage {
        val buttonReply = message.interactive.buttonReply
        val listReply = message.interactive.listReply

        val payloadButtonReply = buttonReply?.let { payloadWhatsApp.getPayloadById(it.id) }
        val payloadListReply = listReply?.let { payloadWhatsApp.getPayloadById(it.id) }

        val copyInteractive =
            message.interactive.copy(
                buttonReply =
                    payloadButtonReply?.let {
                        buttonReply.copy(id = it, title = buttonReply.title)
                    },
                listReply =
                    payloadListReply?.let {
                        listReply.copy(id = it, title = listReply.title, description = listReply.description)
                    },
            )

        return message.copy(
            interactive = copyInteractive,
            id = message.id,
            from = message.from,
            timestamp = message.timestamp,
            context = message.context,
            referral = message.referral,
            errors = message.errors,
        )
    }

    private fun getMessageButtonCopy(message: WhatsAppCloudButtonMessage): WhatsAppCloudButtonMessage {
        val payload = payloadWhatsApp.getPayloadById(message.button.payload)
        return if (payload != null) {
            val copyButton =
                message.button.copy(
                    payload = payload,
                    text = message.button.text,
                )
            message.copy(
                button = copyButton,
                id = message.id,
                from = message.from,
                timestamp = message.timestamp,
                context = message.context,
                referral = message.referral,
                errors = message.errors,
            )
        } else {
            message
        }
    }
}
