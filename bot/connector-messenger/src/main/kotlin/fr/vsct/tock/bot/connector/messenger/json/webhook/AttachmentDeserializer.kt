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

package fr.vsct.tock.bot.connector.messenger.json.webhook

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import fr.vsct.tock.bot.connector.messenger.model.webhook.Attachment
import fr.vsct.tock.bot.connector.messenger.model.webhook.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.webhook.Payload
import fr.vsct.tock.shared.jackson.readValueAs
import mu.KotlinLogging

/**
 *
 */
class AttachmentDeserializer : JsonDeserializer<Attachment>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }


    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Attachment? {
        //facebook can send empty attachments (ie attachments:[{}])
        var type: AttachmentType? = null
        var payload: Payload? = null
        while (jp.nextValue() != JsonToken.END_OBJECT) {
            when (jp.currentName) {
                Attachment::type.name -> type = jp.readValueAs(AttachmentType::class)
                Attachment::payload.name -> payload = jp.readValueAs(Payload::class)
                else -> logger.warn { "Unsupported field : ${jp.currentName}" }
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