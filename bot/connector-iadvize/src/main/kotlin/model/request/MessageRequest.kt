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

package ai.tock.bot.connector.iadvize.model.request

import ai.tock.bot.connector.iadvize.model.response.conversation.payload.TextPayload
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class MessageRequest(val idOperator: String, val idConversation: String, val message: Message) : IadvizeRequest {
    data class MessageRequestJson(val idOperator: String, val message: Message)

    constructor(messageRequestJson: MessageRequestJson, idConversation: String) :
            this(messageRequestJson.idOperator, idConversation, messageRequestJson.message)

    data class Message(val idMessage: String,
                       val author: Author,
                       val payload: TextPayload,
                       //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z[Etc/UTC]'")
                       //val createdAt: ZonedDateTime)
                       val createdAt: String)

    data class Author(val role: String)
}