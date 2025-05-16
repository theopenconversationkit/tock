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

package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.messenger.model.Recipient
import ai.tock.bot.connector.messenger.model.send.Attachment
import ai.tock.bot.connector.messenger.model.send.AttachmentMessage
import ai.tock.bot.connector.messenger.model.send.AttachmentType
import ai.tock.bot.connector.messenger.model.send.Message
import ai.tock.bot.connector.messenger.model.send.MessageRequest
import ai.tock.bot.connector.messenger.model.send.MessageTag
import ai.tock.bot.connector.messenger.model.send.MessagingType.MESSAGE_TAG
import ai.tock.bot.connector.messenger.model.send.MessagingType.RESPONSE
import ai.tock.bot.connector.messenger.model.send.NotificationType
import ai.tock.bot.connector.messenger.model.send.TextMessage
import ai.tock.bot.connector.messenger.model.send.UrlPayload
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendSentence
import mu.KotlinLogging

/**
 *
 */
object SendActionConverter {

    private val logger = KotlinLogging.logger {}

    fun toMessageRequest(action: Action, personaId: String? = null): MessageRequest? {
        return when (action) {
            is SendSentence ->
                if (action.hasMessage(MessengerConnectorProvider.connectorType)) {
                    action.message(MessengerConnectorProvider.connectorType) as Message
                } else {
                    TextMessage(action.stringText ?: "")
                }

            is SendAttachment -> AttachmentMessage(
                Attachment(
                    AttachmentType.fromTockAttachmentType(action.type),
                    UrlPayload.getUrlPayload(action)
                )
            )
            else -> {
                logger.warn { "action not supported : $action " }
                null
            }
        }?.let {
            val messageTag = MessageTag.toMessageTag(action)
            MessageRequest(
                Recipient(action.recipientId.id),
                it,
                if (messageTag == null) RESPONSE else MESSAGE_TAG,
                NotificationType.toNotificationType(action),
                messageTag,
                personaId
            )
        }
    }
}
