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

package ai.tock.bot.connector.businesschat.model.common

import ai.tock.bot.connector.businesschat.model.csp.BusinessChatCommonModel
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Common model for different types of message in Business Chat
 * See [https://developer.apple.com/documentation/businesschatapi/messages_received/receiving_messages_from_the_business_chat_service]
 * - message : the body contains the message
 * - interactive message : simple list picker response. Datas are in the InteractiveData
 * - large interactive message : list picker with icons. Datas are in th InteractiveDataRef
 * - rich link data : url with image loaded
 */
class ReceivedModel(
    sourceId: String,
    destinationId: String,
    val body: String?,
    val interactiveData: InteractiveData?,
    val interactiveDataRef: InteractiveDataRef?,
    val richLinkData: RichLinkData?,
    @JsonProperty("data")
    val handoverData: HandoverData?,
) : BusinessChatCommonModel(sourceId = sourceId, destinationId = destinationId, type = MessageType.text)

data class HandoverData(
    @JsonProperty("new_owner_app_id")
    val newOwnerAppId: String?,
    @JsonProperty("recipient_id")
    val recipientId: String?,
    val metadata: String?,
    @JsonProperty("recipient_user_id")
    val recipientUserId: String?,
)

class InteractiveData(val data: Data)

class Data(val replyMessage: ReplyMessage?)

class ReplyMessage(val title: String)

class InteractiveDataRef(
    val url: String,
    val bid: String,
    @JsonProperty("signature-base64")
    val signatureBase64: String,
    val key: String,
    val signature: String,
    val owner: String,
    val size: Int,
)

class RichLinkData(
    val url: String,
    val title: String,
)
