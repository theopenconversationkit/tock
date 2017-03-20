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

import fr.vsct.tock.bot.connector.messenger.MessengerConnectorProvider.connectorType
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.send.OutputMessage
import fr.vsct.tock.bot.connector.messenger.model.send.Payload
import fr.vsct.tock.bot.connector.messenger.model.webhook.InputAttachment
import fr.vsct.tock.bot.connector.messenger.model.webhook.InputMessage
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.audio
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.image
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.video
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendLocation
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.user.PlayerType.bot
import fr.vsct.tock.bot.engine.user.PlayerType.user
import mu.KotlinLogging
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment as FacebookAttachment

/**
 *
 */
object MessengerActionConverter {

    private val logger = KotlinLogging.logger {}

    fun messengerToAction(message: InputMessage, applicationId: String): Action? {
        if (message.message != null) {
            with(message.message) {
                //skip for now
                if (isEcho) {
                    return null
                }
                if (attachments != null && attachments.isNotEmpty() && attachments.first().type != null) {
                    val type = attachments.first().type
                    if (type == "location") {
                        return readLocation(message, attachments.first(), applicationId)
                    } else if (type == "image" && text.isNullOrEmpty()) {
                        return readImage(message, attachments.first(), applicationId)
                    }
                }
                return readSentence(message, applicationId)
            }
        } else if (message.postback != null) {
            return SendChoice(
                    message.playerId(user),
                    applicationId,
                    message.recipientId(bot),
                    message.postback.payload)
        } else if (message.optin != null) {
            //not yet supported
            TODO()
        }

        logger.error { "unknown message $message" }
        return readSentence(message, applicationId)
    }

    private fun readSentence(message: InputMessage, applicationId: String): SendSentence {
        return SendSentence(
                message.playerId(user),
                applicationId,
                message.recipientId(bot),
                message.message?.text ?: "",
                mutableListOf(message)
        )
    }

    private fun readLocation(message: InputMessage, attachment: InputAttachment, applicationId: String): SendLocation {
        logger.debug { "read location attachment : $attachment" }
        return SendLocation(
                message.playerId(user),
                applicationId,
                message.recipientId(bot),
                attachment.payload?.coordinates
        )
    }

    private fun readImage(message: InputMessage, attachment: InputAttachment, applicationId: String): SendAttachment {
        logger.debug { "read image attachment : $attachment" }
        return SendAttachment(
                message.playerId(user),
                applicationId,
                message.recipientId(bot),
                attachment.payload?.url ?: "",
                image
        )
    }


    fun actionToMessenger(action: Action): MessageRequest? {
        return when (action) {
            is SendSentence ->
                if (action.hasMessage(connectorType)) {
                    action.message(connectorType) as OutputMessage
                } else {
                    OutputMessage(action.text ?: "")
                }

            is SendAttachment -> OutputMessage(
                    FacebookAttachment(
                            when (action.type) {
                                image -> "image"
                                audio -> "audio"
                                video -> "video"
                            },
                            Payload(url = action.url))
            )
            else -> {
                logger.warn { "action not supported : $action " }
                null
            }
        }?.let {
            MessageRequest(Recipient(action.recipientId.id), it)
        }
    }

}