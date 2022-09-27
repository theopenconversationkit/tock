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

package ai.tock.bot.connector.iadvize.model.response

import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMultipartReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeReply
import ai.tock.shared.jackson.JacksonDeserializer
import ai.tock.shared.jackson.read
import ai.tock.shared.jackson.readListValues
import ai.tock.shared.jackson.readValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import mu.KotlinLogging
/*
internal class IadvizeMultipartDeserializer : JacksonDeserializer<IadvizeMultipartReply>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): IadvizeMultipartReply? {

        data class EventFields(
            var list: List<IadvizeReply>? = null
        )

        val (list) =
            jp.readListValues() { fields, name ->
            with(fields) {
                when (name) {
                }
            }
        }

        return when {
            directMessages != null -> DirectMessageIncomingEvent(forUserId!!, users!!, apps, directMessages)
            directMessageIndicateTyping != null -> DirectMessageIndicateTypingIncomingEvent(forUserId!!, users!!, directMessageIndicateTyping)
            statuses != null -> TweetIncomingEvent(forUserId!!, statuses)
            else -> null
        }
    }
}*/
