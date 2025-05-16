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

package ai.tock.bot.connector.whatsapp.cloud.model.send.message

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.whatsapp.cloud.UserHashedIdCache
import ai.tock.bot.connector.whatsapp.cloud.WhatsAppCloudConnectorMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotImageMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotInteractiveMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotLocationMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotTemplateMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotTextMessage
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import ai.tock.bot.connector.whatsapp.cloud.whatsAppCloudConnectorType
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendChoice.Companion.TITLE_PARAMETER
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericMessage
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)

@JsonSubTypes(
    JsonSubTypes.Type(value = WhatsAppCloudBotTextMessage::class, name = "text"),
    JsonSubTypes.Type(value = WhatsAppCloudBotInteractiveMessage::class, name = "interactive"),
    JsonSubTypes.Type(value = WhatsAppCloudBotLocationMessage::class, name = "location"),
    JsonSubTypes.Type(value = WhatsAppCloudBotTemplateMessage::class, name = "template"),
    JsonSubTypes.Type(value = WhatsAppCloudBotImageMessage::class, name = "image")
    )

abstract class WhatsAppCloudBotMessage (val type: WhatsAppCloudBotMessageType, @JsonIgnore internal open val userId: String?) :
    ConnectorMessage, WhatsAppCloudConnectorMessage() {

    @get:JsonIgnore
    override val connectorType: ConnectorType = whatsAppCloudConnectorType

    @get:JsonProperty("recipient_type")
    abstract val recipientType: WhatsAppCloudBotRecipientType

    /**
     * Processes a bot message in preparation for sending it to the WhatsApp cloud API
     */
    internal abstract fun prepareMessage(apiService: WhatsAppCloudApiService, recipientId: String): WhatsAppCloudSendBotMessage

    @get:JsonIgnore
    val to: String get() = userId?.let { UserHashedIdCache.getRealId(it) } ?: "unknown"



    override fun toGenericMessage(): GenericMessage? {

        return when(this) {
            is WhatsAppCloudBotInteractiveMessage -> GenericMessage(
                connectorType = whatsAppCloudConnectorType,
                choices = interactive.action?.buttons?.mapNotNull { actionButton ->
                    actionButton.reply.let {

                        SendChoice.decodeChoiceId(it.id)
                            .let { (intent, params) ->
                                Choice(
                                    intent,
                                    params + (TITLE_PARAMETER to it.title)
                                )
                            }


                    }
                }!!


            )

            else -> GenericMessage(
                connectorType = whatsAppCloudConnectorType,
                texts = mapOf(GenericMessage.TEXT_PARAM to "Unsupported message type")
            )
        }
    }


}
