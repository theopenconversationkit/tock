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

package ai.tock.bot.connector.alcmeon

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.messenger.model.webhook.Attachment
import ai.tock.bot.connector.messenger.model.webhook.UserActionPayload
import ai.tock.bot.connector.whatsapp.model.common.WhatsAppTextBody
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "backend"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AlcmeonConnectorWhatsappMessageIn::class, name = whatsappBackend),
    JsonSubTypes.Type(value = AlcmeonConnectorFacebookMessageIn::class, name = facebookBackend),
)
abstract class AlcmeonConnectorMessageIn(
    val backend: String,
) : ConnectorMessage {
    @get:JsonProperty("user_external_id")
    abstract val userExternalId: String

    @get:JsonProperty("user_name")
    abstract val userName: String

    override val connectorType: ConnectorType @JsonIgnore get() = alcmeonConnectorType
}

data class AlcmeonConnectorWhatsappMessageIn(
    override val userExternalId: String,
    override val userName: String,
    val event: AlcmeonConnectorWhatsappMessageEvent
) : AlcmeonConnectorMessageIn(whatsappBackend)

data class AlcmeonConnectorWhatsappMessageEvent(val text: WhatsAppTextBody)

data class AlcmeonConnectorFacebookMessageIn(
    override val userExternalId: String,
    override val userName: String,
    val event: AlcmeonConnectorFacebookMessageEvent
) : AlcmeonConnectorMessageIn(facebookBackend)

data class AlcmeonConnectorFacebookMessageEvent(val message: MessengerMessage)

data class MessengerMessage(
    var text: String? = null,
    val attachments: List<Attachment> = emptyList(),
    @get:JsonProperty("quick_reply") val quickReply: UserActionPayload? = null
)
