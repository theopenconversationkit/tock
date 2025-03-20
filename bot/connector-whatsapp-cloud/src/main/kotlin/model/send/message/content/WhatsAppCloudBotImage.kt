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

import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotImageMessage
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.message.Attachment
import java.util.Base64

sealed interface WhatsAppCloudBotImage {
    val caption: String?
    fun prepare(apiService: WhatsAppCloudApiService): WhatsAppCloudSendBotImageMessage.Image
    fun toGenericAttachment(): Attachment

    data class LinkedImage(val url: String, override val caption: String?, val uploadToWhatsapp: Boolean) : WhatsAppCloudBotImage {
        override fun prepare(apiService: WhatsAppCloudApiService): WhatsAppCloudSendBotImageMessage.Image {
            return WhatsAppCloudSendBotImageMessage.Image(
                id = if (uploadToWhatsapp) apiService.getUploadedImageId(url) else null,
                link = url.takeUnless { uploadToWhatsapp },
                caption = caption,
            )
        }

        override fun toGenericAttachment() = Attachment(url, SendAttachment.AttachmentType.image)
    }

    class UploadedImage(val id: String, val bytes: ByteArray, val mimeType: String, override val caption: String?) : WhatsAppCloudBotImage {
        override fun prepare(apiService: WhatsAppCloudApiService): WhatsAppCloudSendBotImageMessage.Image {
            return WhatsAppCloudSendBotImageMessage.Image(
                id = apiService.getUploadedImageId(id, bytes, mimeType),
                link = null,
                caption = caption,
            )
        }

        override fun toGenericAttachment(): Attachment {
            val base64String = Base64.getEncoder().encodeToString(bytes)
            return Attachment(url = "data:$mimeType;base64,$base64String", SendAttachment.AttachmentType.image)
        }
    }
}
