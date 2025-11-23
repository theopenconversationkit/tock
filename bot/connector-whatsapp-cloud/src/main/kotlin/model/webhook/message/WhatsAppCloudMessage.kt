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

package ai.tock.bot.connector.whatsapp.cloud.model.webhook.message

import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.content.ContextContent
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.content.ErrorItem
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.content.Referral
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = WhatsAppCloudTextMessage::class, name = "text"),
    JsonSubTypes.Type(value = WhatsAppCloudAudioMessage::class, name = "audio"),
    JsonSubTypes.Type(value = WhatsAppCloudButtonMessage::class, name = "button"),
    JsonSubTypes.Type(value = WhatsAppCloudDocumentMessage::class, name = "document"),
    JsonSubTypes.Type(value = WhatsAppCloudImageMessage::class, name = "image"),
    JsonSubTypes.Type(value = WhatsAppCloudInteractiveMessage::class, name = "interactive"),
    JsonSubTypes.Type(value = WhatsAppCloudOrderMessage::class, name = "order"),
    JsonSubTypes.Type(value = WhatsAppCloudStickerMessage::class, name = "sticker"),
    JsonSubTypes.Type(value = WhatsAppCloudSystemMessage::class, name = "system"),
    JsonSubTypes.Type(value = WhatsAppCloudVideoMessage::class, name = "video"),
    JsonSubTypes.Type(value = WhatsAppCloudLocationMessage::class, name = "location"),
)
abstract class WhatsAppCloudMessage(val type: WhatsAppCloudMessageType) {
    abstract val from: String
    abstract val id: String
    abstract val timestamp: String
    abstract val context: ContextContent?
    abstract val referral: Referral?
    abstract val errors: List<ErrorItem>?
}
