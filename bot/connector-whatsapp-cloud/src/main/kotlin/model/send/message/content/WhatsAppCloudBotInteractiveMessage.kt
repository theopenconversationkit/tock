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

package ai.tock.bot.connector.whatsapp.cloud.model.send.message.content

import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotMessageType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotRecipientType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotInteractiveMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotMessage
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.GenericMessage

data class WhatsAppCloudBotInteractiveMessage(
    val interactive: WhatsAppCloudBotInteractive,
    override val recipientType: WhatsAppCloudBotRecipientType,
    override val userId: String? = null,
) : WhatsAppCloudBotMessage(WhatsAppCloudBotMessageType.interactive, userId) {
    override fun toGenericMessage(): GenericMessage {
        val texts = listOfNotNull(
            interactive.header?.text?.let { GenericMessage.TITLE_PARAM to it },
            GenericMessage.TEXT_PARAM to (interactive.body?.text ?: "")
        ).toMap()
        return GenericMessage(
            texts = texts,
            attachments = listOfNotNull(
                interactive.header?.image?.id?.let { Attachment(it, SendAttachment.AttachmentType.image) },
                interactive.header?.video?.id?.let { Attachment(it, SendAttachment.AttachmentType.video) },
            ),
            choices = interactive.action.buttons?.map { it.toChoice() }
                ?: interactive.action.sections?.flatMap { it.rows ?: listOf() }?.map { it.toChoice() }
                ?: listOf()
        )
    }

    override fun prepareMessage(
        apiService: WhatsAppCloudApiService,
        recipientId: String
    ): WhatsAppCloudSendBotMessage {
        val action = interactive.action
        val updatedButtons = action.buttons.takeIf { !it.isNullOrEmpty() }?.map { btn ->
            btn.copy(reply = btn.reply.copy(id = apiService.shortenPayload(btn.reply.id)))
        }
        val updatedSections = action.sections.takeIf { !it.isNullOrEmpty() }?.map { section ->
            section.copy(rows = section.rows?.map { row ->
                row.copy(id = apiService.shortenPayload(row.id))
            })
        }
        val updatedHeader = interactive.header?.let { header ->
            if (header.image != null) {
                header.copy(
                    image = WhatsAppCloudBotMediaImage(
                        id = apiService.getUploadedImageId(
                            header.image.id
                        )
                    )
                )
            } else {
                header
            }
        }
        val updatedAction = interactive.action.copy(
            buttons = updatedButtons,
            sections = updatedSections,
        )
        return WhatsAppCloudSendBotInteractiveMessage(
            interactive.copy(header = updatedHeader, action = updatedAction),
            recipientType,
            recipientId,
        )
    }
}
