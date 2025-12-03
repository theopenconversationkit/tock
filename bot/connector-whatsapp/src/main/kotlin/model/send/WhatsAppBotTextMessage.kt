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

package ai.tock.bot.connector.whatsapp.model.send

import ai.tock.bot.connector.whatsapp.model.common.WhatsAppTextBody
import ai.tock.bot.engine.message.GenericMessage
import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 */
data class WhatsAppBotTextMessage(
    val text: WhatsAppTextBody,
    override val recipientType: WhatsAppBotRecipientType,
    override val userId: String? = null,
    @get:JsonProperty("preview_url")
    val previewUrl: Boolean = false,
) : WhatsAppBotMessage(WhatsAppBotMessageType.text, userId) {
    override fun toSendBotMessage(recipientId: String): WhatsAppSendBotMessage =
        WhatsAppSendBotTextMessage(
            text,
            recipientType,
            recipientId,
            previewUrl,
        )

    override fun toGenericMessage(): GenericMessage? =
        GenericMessage(
            texts = mapOf(GenericMessage.TEXT_PARAM to text.body),
        )
}
