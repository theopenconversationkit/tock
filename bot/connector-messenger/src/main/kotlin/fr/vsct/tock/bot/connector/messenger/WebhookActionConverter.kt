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

package fr.vsct.tock.bot.connector.messenger

import fr.vsct.tock.bot.connector.messenger.model.webhook.Attachment
import fr.vsct.tock.bot.connector.messenger.model.webhook.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.webhook.LocationPayload
import fr.vsct.tock.bot.connector.messenger.model.webhook.MessageWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.PostbackWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.UrlPayload
import fr.vsct.tock.bot.connector.messenger.model.webhook.Webhook
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendLocation
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.user.PlayerType
import mu.KotlinLogging

/**
 *
 */
internal object WebhookActionConverter {

    private val logger = KotlinLogging.logger {}

    fun toAction(message: Webhook, applicationId: String): Action? {
        return when (message) {
            is MessageWebhook ->
                with(message.message) {
                    val a = attachments
                    if (a.isNotEmpty()) {
                        val type = a.first().type
                        when (type) {
                            AttachmentType.location -> readLocation(message, a.first(), applicationId)
                            AttachmentType.image -> readImage(message, a.first(), applicationId)
                        // ignore for now
                            else -> readSentence(message, applicationId)
                        }
                    } else {
                        readSentence(message, applicationId)
                    }
                }
            is PostbackWebhook ->
                SendChoice.decodeChoiceId(message.postback.payload)
                        .let { (intentName, parameters) ->
                            SendChoice(
                                    message.playerId(PlayerType.user),
                                    applicationId,
                                    message.recipientId(PlayerType.bot),
                                    intentName,
                                    parameters)
                        }

            else -> {
                logger.error { "unknown message $message" }
                null
            }
        }
    }

    private fun readSentence(message: MessageWebhook, applicationId: String): SendSentence {
        return SendSentence(
                message.playerId(PlayerType.user),
                applicationId,
                message.recipientId(PlayerType.bot),
                message.message.text ?: "",
                mutableListOf(message)
        )
    }

    private fun readLocation(message: MessageWebhook, attachment: Attachment, applicationId: String): SendLocation {
        logger.debug { "read location attachment : $attachment" }
        return SendLocation(
                message.playerId(PlayerType.user),
                applicationId,
                message.recipientId(PlayerType.bot),
                (attachment.payload as LocationPayload).coordinates.toUserLocation()
        )
    }

    private fun readImage(message: MessageWebhook, attachment: Attachment, applicationId: String): SendAttachment {
        logger.debug { "read image attachment : $attachment" }
        return SendAttachment(
                message.playerId(PlayerType.user),
                applicationId,
                message.recipientId(PlayerType.bot),
                (attachment.payload as UrlPayload).url,
                SendAttachment.AttachmentType.image
        )
    }
}