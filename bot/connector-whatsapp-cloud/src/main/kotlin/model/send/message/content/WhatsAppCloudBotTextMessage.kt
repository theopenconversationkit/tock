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

import ai.tock.bot.connector.whatsapp.cloud.model.common.TextContent
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotMessageType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotRecipientType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotTextMessage
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import ai.tock.bot.engine.message.GenericMessage

data class WhatsAppCloudBotTextMessage (
        val text: TextContent,
        override val recipientType: WhatsAppCloudBotRecipientType,
        override val userId: String? = null,
        val previewUrl: Boolean = false,
) : WhatsAppCloudBotMessage(WhatsAppCloudBotMessageType.text, userId) {
    override fun prepareMessage(apiService: WhatsAppCloudApiService, recipientId: String): WhatsAppCloudSendBotMessage =
            WhatsAppCloudSendBotTextMessage(
                    text,
                    recipientType,
                    recipientId,
                    previewUrl,
            )

    override fun toGenericMessage(): GenericMessage =
            GenericMessage(
                    texts = mapOf(GenericMessage.TEXT_PARAM to text.body),
            )
}