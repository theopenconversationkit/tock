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
import fr.vsct.tock.bot.connector.messenger.model.webhook.Message
import fr.vsct.tock.bot.connector.messenger.model.webhook.MessageEcho
import fr.vsct.tock.shared.jackson.readListValuesAs
import mu.KotlinLogging

/**
 *
 */
internal class MessageDeserializer : JsonDeserializer<Message>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }


    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Message? {
        var mid: String? = null
        var seq: Long? = null
        var text: String? = null
        var attachments: List<Attachment>? = null
        var isEcho: Boolean = false
        var appId: Long? = null
        var metadata: String? = null

        while (jp.nextValue() != JsonToken.END_OBJECT) {
            when (jp.currentName) {
                Message::mid.name -> mid = jp.valueAsString
                Message::seq.name -> seq = jp.longValue
                Message::text.name -> text = jp.valueAsString
                Message::attachments.name -> attachments = jp.readListValuesAs<Attachment>().filterNotNull()
                "is_echo" -> isEcho = jp.booleanValue
                "app_id" -> appId = jp.longValue
                MessageEcho::metadata.name -> metadata = jp.valueAsString
                else -> logger.warn { "Unsupported field : ${jp.currentName}" }
            }
        }

        if (mid == null || seq == null || (text == null && attachments?.isEmpty() ?: true)) {
            logger.warn { "invalid message $mid $seq $text $attachments" }
            return null
        }

        return if (isEcho) {
            if (appId == null) {
                logger.warn { "null appId foe echo $mid $seq $text $attachments $metadata" }
                null
            } else {
                MessageEcho(mid, seq, text, attachments ?: emptyList(), true, appId, metadata)
            }
        } else {
            Message(mid, seq, text, attachments ?: emptyList())
        }
    }
}