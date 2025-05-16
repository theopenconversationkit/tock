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

package ai.tock.bot.connector.whatsapp.cloud.model.send.message.content

import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotMessageType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotRecipientType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotTemplateMessage
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import ai.tock.bot.engine.message.GenericMessage

data class WhatsAppCloudBotTemplateMessage(
    val template: WhatsAppCloudBotTemplate,
    override val recipientType: WhatsAppCloudBotRecipientType,
    override val userId: String? = null,
) : WhatsAppCloudBotMessage(WhatsAppCloudBotMessageType.template, userId) {
    override fun prepareMessage(apiService: WhatsAppCloudApiService, recipientId: String): WhatsAppCloudSendBotMessage {
        val updatedComponents = template.components
        apiService.replaceWithRealImageId(updatedComponents, recipientId)

        return WhatsAppCloudSendBotTemplateMessage(
            template.copy(components = updatedComponents),
            recipientType,
            recipientId
        )
    }

    override fun toGenericMessage(): GenericMessage =
        GenericMessage(
            texts = mapOf(GenericMessage.TEXT_PARAM to "template"),
        )
}
