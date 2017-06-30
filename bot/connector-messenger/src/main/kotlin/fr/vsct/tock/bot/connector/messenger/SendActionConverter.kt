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

import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.send.NotificationType
import fr.vsct.tock.bot.connector.messenger.model.send.TextMessage
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.action.SendSentence
import mu.KotlinLogging

/**
 *
 */
internal object SendActionConverter {

    private val logger = KotlinLogging.logger {}

    fun toMessageRequest(action: Action): MessageRequest? {
        return when (action) {
            is SendSentence ->
                if (action.hasMessage(MessengerConnectorProvider.connectorType)) {
                    action.message(MessengerConnectorProvider.connectorType) as AttachmentMessage
                } else {
                    TextMessage(action.text ?: "")
                }

            is SendAttachment -> AttachmentMessage(
                    Attachment(
                            AttachmentType.fromTockAttachmentType(action.type),
                            UrlPayload.getUrlPayload(action.url)
                    )
            )
            else -> {
                logger.warn { "action not supported : $action " }
                null
            }
        }?.let {
            MessageRequest(
                    Recipient(action.recipientId.id),
                    it,
                    NotificationType.toNotificationType(action)
            )
        }
    }
}