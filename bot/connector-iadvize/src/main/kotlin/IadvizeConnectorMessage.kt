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

package ai.tock.bot.connector.iadvize

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.iadvize.model.payload.TextPayload
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMessage
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMultipartReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeTransfer
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericMessage
import com.fasterxml.jackson.annotation.JsonIgnore

data class IadvizeConnectorMessage(val replies: List<IadvizeReply>) : ConnectorMessage {
    override val connectorType: ConnectorType
        @JsonIgnore get() = IadvizeConnectorProvider.connectorType

    constructor(vararg replies: IadvizeReply) : this(replies.toList())

    constructor(multipartReplies: IadvizeMultipartReply) : this(multipartReplies.replies)

    override fun toGenericMessage(): GenericMessage? {
        val indexOfFirst = replies.indexOfFirst { it is IadvizeTransfer }
        return (
            if (indexOfFirst != -1) {
                // Ignore all messages after transfer reply
                replies
                    .slice(0..indexOfFirst)
            } else {
                replies
            }
        )
            .filterIsInstance<IadvizeMessage>()
            .map { message ->
                GenericMessage(
                    connectorType = connectorType,
                    texts = mapOf("text" to (message.payload as TextPayload).value.toString()),
                    choices = message.quickReplies.map { Choice.fromText(it.value) },
                )
            }
            .firstOrNull()
    }
}
