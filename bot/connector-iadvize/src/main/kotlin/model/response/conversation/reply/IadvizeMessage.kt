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

package ai.tock.bot.connector.iadvize.model.response.conversation.reply

import ai.tock.bot.connector.iadvize.model.payload.Payload
import ai.tock.bot.connector.iadvize.model.payload.TextPayload
import ai.tock.bot.connector.iadvize.model.response.conversation.QuickReply
import ai.tock.iadvize.client.graphql.ChatbotActionOrMessageInput
import ai.tock.iadvize.client.graphql.ChatbotConversationMessageAttachmentInput
import ai.tock.iadvize.client.graphql.ChatbotMessageInput
import ai.tock.iadvize.client.graphql.ChatbotQuickReplyMenuAttachmentInput

data class IadvizeMessage(
    val payload: Payload,
    val quickReplies: MutableList<QuickReply> = mutableListOf(),
) : IadvizeReply(ReplyType.message) {
    constructor(messagePayload: String) : this(TextPayload(messagePayload))

    override fun toChatBotActionOrMessageInput(): ChatbotActionOrMessageInput {
        return if (quickReplies.isEmpty() && payload is TextPayload) {
            // A simple message
            ChatbotActionOrMessageInput(
                chatbotMessage =
                    ChatbotMessageInput(
                        chatbotSimpleTextMessage = payload.value.toString(),
                    ),
            )
        } else {
            // A complex message with QuickReply
            ChatbotActionOrMessageInput(
                chatbotMessage =
                    ChatbotMessageInput(
                        chatbotMessageAttachmentInput =
                            ChatbotConversationMessageAttachmentInput(
                                quickReplyMenu =
                                    ChatbotQuickReplyMenuAttachmentInput(
                                        message = if (payload is TextPayload) payload.value.toString() else payload.toString(),
                                        choices = quickReplies.map { quickReply -> quickReply.value },
                                    ),
                            ),
                    ),
            )
        }
    }
}
