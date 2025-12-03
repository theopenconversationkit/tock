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

package ai.tock.bot.connector.messenger.json.webhook

import ai.tock.bot.connector.messenger.model.webhook.Attachment
import ai.tock.bot.connector.messenger.model.webhook.AttachmentType
import ai.tock.bot.connector.messenger.model.webhook.Payload
import ai.tock.shared.jackson.JacksonDeserializer
import ai.tock.shared.jackson.read
import ai.tock.shared.jackson.readValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import mu.KotlinLogging

/**
 *
 */
internal class AttachmentDeserializer : JacksonDeserializer<Attachment>() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): Attachment? {
        // facebook can send empty attachments (ie attachments:[{}])
        data class AttachmentFields(
            var type: AttachmentType? = null,
            var payload: Payload? = null,
            var other: EmptyJson? = null,
        )

        val (type, payload) =
            jp.read<AttachmentFields> { fields, name ->
                with(fields) {
                    when (name) {
                        Attachment::type.name -> type = jp.readValue()
                        Attachment::payload.name -> payload = jp.readValue()
                        else -> other = jp.readUnknownValue()
                    }
                }
            }

        return if (type == null || payload == null) {
            logger.debug { "empty attachment" }
            null
        } else {
            Attachment(type, payload)
        }
    }
}
