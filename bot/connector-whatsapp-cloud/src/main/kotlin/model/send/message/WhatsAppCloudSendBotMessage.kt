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

package ai.tock.bot.connector.whatsapp.cloud.model.send.message

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes(
        JsonSubTypes.Type(value = WhatsAppCloudSendBotTextMessage::class, name = "text"),
        JsonSubTypes.Type(value = WhatsAppCloudSendBotInteractiveMessage::class, name = "interactive"),
        JsonSubTypes.Type(value = WhatsAppCloudSendBotLocationMessage::class, name = "location"),
        JsonSubTypes.Type(value = WhatsAppCloudSendBotTemplateMessage::class, name = "template"),
        JsonSubTypes.Type(value = WhatsAppCloudSendBotImageMessage::class, name = "image"),
    )
@JsonPropertyOrder("messaging_product")
abstract class WhatsAppCloudSendBotMessage(val type: WhatsAppCloudBotMessageType) {

    @Suppress("unused")
    @JsonProperty("messaging_product")
    val messagingProduct = "whatsapp"

    abstract val to: String?

    @get:JsonProperty("recipient_type")
    abstract val recipientType: WhatsAppCloudBotRecipientType?
}