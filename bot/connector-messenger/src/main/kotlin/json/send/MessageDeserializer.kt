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

package ai.tock.bot.connector.messenger.json.send

import ai.tock.bot.connector.messenger.model.send.Attachment
import ai.tock.bot.connector.messenger.model.send.AttachmentMessage
import ai.tock.bot.connector.messenger.model.send.Message
import ai.tock.bot.connector.messenger.model.send.QuickReply
import ai.tock.bot.connector.messenger.model.send.TextMessage
import ai.tock.shared.jackson.JacksonDeserializer
import ai.tock.shared.jackson.read
import ai.tock.shared.jackson.readListValues
import ai.tock.shared.jackson.readValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import mu.KotlinLogging

/**
 *
 */
internal class MessageDeserializer : JacksonDeserializer<Message>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Message? {
        data class MessageFields(
            var text: String? = null,
            var attachment: Attachment? = null,
            var quickReplies: List<QuickReply>? = null,
            var other: EmptyJson? = null
        )

        val (text, attachment, quickReplies) = jp.read<MessageFields> { fields, name ->
            with(fields) {
                when (name) {
                    TextMessage::text.name -> text = jp.valueAsString
                    AttachmentMessage::attachment.name -> attachment = jp.readValue()
                    "quick_replies" -> quickReplies = jp.readListValues()
                    else -> other = jp.readUnknownValue()
                }
            }
        }

        return if (text != null) {
            TextMessage(text, quickReplies)
        } else if (attachment != null) {
            AttachmentMessage(attachment, quickReplies)
        } else {
            logger.warn { "invalid message" }
            null
        }
    }
}
