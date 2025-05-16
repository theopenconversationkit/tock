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

package ai.tock.bot.connector.whatsapp.model.webhook

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 *
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes(
    Type(value = WhatsAppTextMessage::class, name = "text"),
    Type(value = WhatsAppLocationMessage::class, name = "location"),
    Type(value = WhatsAppImageMessage::class, name = "image"),
    Type(value = WhatsAppDocumentMessage::class, name = "document"),
    Type(value = WhatsAppVoiceMessage::class, name = "voice"),
    Type(value = WhatsAppSystemMessage::class, name = "system"),
    Type(value = WhatsAppButtonMessage::class, name = "button"),
    Type(value = WhatsAppUnknownMessage::class, name = "unknown")
)
abstract class WhatsAppMessage(val type: WhatsAppMessageType) {
    abstract val from: String
    abstract val id: String
    abstract val timestamp: String
    abstract val context: WhatsAppContext?
    @get:JsonProperty("group_id")
    abstract val groupId: String?
}
