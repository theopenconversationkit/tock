/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.twitter.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import fr.vsct.tock.bot.connector.twitter.model.Application
import fr.vsct.tock.bot.connector.twitter.model.DirectMessage
import fr.vsct.tock.bot.connector.twitter.model.DirectMessageIndicateTyping
import fr.vsct.tock.bot.connector.twitter.model.User
import fr.vsct.tock.bot.connector.twitter.model.incoming.DirectMessageIncomingEvent
import fr.vsct.tock.bot.connector.twitter.model.incoming.DirectMessageIndicateTypingIncomingEvent
import fr.vsct.tock.bot.connector.twitter.model.incoming.IncomingEvent
import fr.vsct.tock.shared.jackson.JacksonDeserializer
import fr.vsct.tock.shared.jackson.read
import fr.vsct.tock.shared.jackson.readListValues
import fr.vsct.tock.shared.jackson.readValue
import mu.KotlinLogging

class EventDeserializer : JacksonDeserializer<IncomingEvent>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): IncomingEvent? {

        data class EventFields(
            var forUserId: String? = null,
            var users: Map<String, User>? = null,
            var apps: Map<String, Application>? = null,
            var directMessages: List<DirectMessage>? = null,
            var directMessagesIndicateTyping: List<DirectMessageIndicateTyping>? = null
        )

        val (forUserId, users, apps, directMessages, directMessageIndicateTyping)
                = jp.read<EventFields> { fields, name ->
            with(fields) {
                when (name) {
                    "for_user_id" -> forUserId = jp.readValue()
                    DirectMessageIncomingEvent::users.name -> users = jp.readValueAs(object : TypeReference<Map<String, User>>() {})
                    DirectMessageIncomingEvent::apps.name -> apps = jp.readValueAs(object : TypeReference<Map<String, Application>>() {})
                    "direct_message_events" -> directMessages = jp.readListValues()
                    "direct_message_indicate_typing_events" -> directMessagesIndicateTyping = jp.readListValues()
                    else -> unknownValue
                }
            }
        }

        return if (directMessages != null) {
            DirectMessageIncomingEvent(forUserId!!, users!!, apps, directMessages)
        } else if (directMessageIndicateTyping != null) {
            DirectMessageIndicateTypingIncomingEvent(forUserId!!, users!!, directMessageIndicateTyping)
        } else {
            logger.error { "unknown event" }
            null
        }
    }

}