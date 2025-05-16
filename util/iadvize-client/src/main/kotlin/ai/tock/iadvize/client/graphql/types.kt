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

package ai.tock.iadvize.client.graphql


data class CloseMessageInput(val _unusedField: Boolean = false)

data class TransferOptionsInput(val timeout: Int = 5)
data class TransferMessageInput(val routingRuleId: String, val transferOptions: TransferOptionsInput)
data class ChatbotActionInput(
    val closeMessage: CloseMessageInput? = null,
    val transferMessage: TransferMessageInput? = null
)

data class ChatbotQuickReplyMenuAttachmentInput(
    val choices: List<String>,
    val message: String
)
data class ChatbotConversationMessageAttachmentInput(
    val quickReplyMenu: ChatbotQuickReplyMenuAttachmentInput
)

data class ChatbotMessageInput(
    val chatbotMessageAttachmentInput: ChatbotConversationMessageAttachmentInput? = null,
    val chatbotSimpleTextMessage: String? = null
)

data class ChatbotActionOrMessageInput(
    val chatbotAction: ChatbotActionInput? = null,
    val chatbotMessage: ChatbotMessageInput? = null
)
