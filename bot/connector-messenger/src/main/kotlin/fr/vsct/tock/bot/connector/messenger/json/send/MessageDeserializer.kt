/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.messenger.json.send

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.Message
import fr.vsct.tock.bot.connector.messenger.model.send.TextMessage
import fr.vsct.tock.shared.jackson.JacksonDeserializer
import fr.vsct.tock.shared.jackson.read
import fr.vsct.tock.shared.jackson.readValueAs
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
                var attachment: Attachment? = null)

        val (text, attachment) = jp.read<MessageFields> { fields, name ->
            with(fields) {
                when (name) {
                    TextMessage::text.name -> text = jp.valueAsString
                    AttachmentMessage::attachment.name -> attachment = jp.readValueAs(Attachment::class)
                    else -> unknownValue
                }
            }
        }

        return if (text != null) {
            TextMessage(text)
        } else if (attachment != null) {
            AttachmentMessage(attachment)
        } else {
            logger.warn { "invalid message" }
            null
        }
    }

}