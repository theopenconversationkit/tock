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

import ai.tock.iadvize.client.graphql.ChatbotActionOrMessageInput
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(name = "await", value = IadvizeAwait::class),
    JsonSubTypes.Type(name = "close", value = IadvizeClose::class),
    JsonSubTypes.Type(name = "message", value = IadvizeMessage::class),
    JsonSubTypes.Type(name = "transfer", value = IadvizeTransfer::class),
)
abstract class IadvizeReply(val type: ReplyType) {
    /**
     * Convert the REST reply [IadvizeReply] to a GraphQL type [ChatbotActionOrMessageInput]
     */
    open fun toChatBotActionOrMessageInput() = ChatbotActionOrMessageInput()
}
