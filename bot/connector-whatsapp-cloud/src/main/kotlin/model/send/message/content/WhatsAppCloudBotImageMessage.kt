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

import ai.tock.bot.connector.whatsapp.cloud.model.send.message.*
import ai.tock.bot.engine.config.UploadedFilesService.attachmentType
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.GenericMessage

data class WhatsAppCloudBotImageMessage (
        override val messagingProduct: String,
        val image: WhatsAppCloudBotImage,
        override val recipientType: WhatsAppCloudBotRecipientType,
        override val userId: String? = null,
) : WhatsAppCloudBotMessage(WhatsAppCloudBotMessageType.image, userId) {
    override fun toSendBotMessage(recipientId: String): WhatsAppCloudSendBotMessage =
            WhatsAppCloudSendBotImageMessage(
                    messagingProduct,
                    image,
                    recipientType,
                    recipientId
            )

    override fun toGenericMessage(): GenericMessage =
            GenericMessage(
                    texts = mapOf(GenericMessage.TEXT_PARAM to "image"),
                    attachments = listOf(Attachment(image.id, attachmentType(image.id)))
            )
}