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

package ai.tock.bot.connector.whatsapp.model.send

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.whatsapp.UserHashedIdCache
import ai.tock.bot.connector.whatsapp.whatsAppConnectorType
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
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
    JsonSubTypes.Type(value = WhatsAppBotTextMessage::class, name = "text"),
    JsonSubTypes.Type(value = WhatsAppBotImageMessage::class, name = "image"),
    JsonSubTypes.Type(value = WhatsAppBotMessageInteractiveMessage::class, name = "interactive"),
    JsonSubTypes.Type(value = WhatsAppBotInteractiveMessage::class, name = "template")
)
abstract class WhatsAppBotMessage(val type: WhatsAppBotMessageType, @JsonIgnore internal open val userId: String?) :
    ConnectorMessage {

    @get:JsonIgnore
    override val connectorType: ConnectorType = whatsAppConnectorType

    @get:JsonProperty("recipient_type")
    abstract val recipientType: WhatsAppBotRecipientType

    internal abstract fun toSendBotMessage(recipientId: String): WhatsAppSendBotMessage

    @get:JsonIgnore
    val to: String get() = userId?.let { UserHashedIdCache.getRealId(it) } ?: "unknown"
}
