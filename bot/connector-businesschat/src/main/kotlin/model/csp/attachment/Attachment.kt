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

package ai.tock.bot.connector.businesschat.model.csp.attachment

import ai.tock.bot.connector.businesschat.model.common.MessageType
import ai.tock.bot.connector.businesschat.model.csp.BusinessChatCommonModel
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * https://developer.apple.com/documentation/businesschatapi/messages_received/receiving_a_text_message_with_attachments
 */
class Attachment(
    sourceId: String,
    destinationId: String,
    val attachments: Array<AttachmentDictionnary>,
) : BusinessChatCommonModel(sourceId = sourceId, destinationId = destinationId, type = MessageType.text) {
    val body: String = "\uFFFc"
}

/**
 * https://developer.apple.com/documentation/businesschatapi/attachment
 */
class AttachmentDictionnary(
    val key: String,
    val mimeType: String,
    val name: String,
    val owner: String,
    @JsonProperty("signature-base64")
    val signatureBase64: String,
    val size: Int,
    val url: String,
)
