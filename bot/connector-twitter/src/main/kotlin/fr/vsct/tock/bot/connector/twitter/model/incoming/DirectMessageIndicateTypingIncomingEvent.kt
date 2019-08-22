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
package fr.vsct.tock.bot.connector.twitter.model.incoming

import com.fasterxml.jackson.annotation.JsonProperty
import fr.vsct.tock.bot.connector.twitter.model.DirectMessageIndicateTyping
import fr.vsct.tock.bot.connector.twitter.model.User
import fr.vsct.tock.bot.engine.event.Event

/**
 * Direct Message Indicate Typing IncomingEvent
 */
data class DirectMessageIndicateTypingIncomingEvent(
    @JsonProperty("for_user_id")
    override val forUserId: String,
    override val users: Map<String, User>,
    @JsonProperty("direct_message_indicate_typing_events")
    val directMessagesIndicateTyping: List<DirectMessageIndicateTyping>
) : IncomingEvent() {
    override val ignored: Boolean
        get() = true

    override fun toEvent(applicationId: String): Event? {
        return null
    }
}