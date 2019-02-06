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
import fr.vsct.tock.bot.connector.twitter.model.Application
import fr.vsct.tock.bot.connector.twitter.model.DirectMessage
import fr.vsct.tock.bot.connector.twitter.model.User
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType

/**
 * Direct Message IncomingEvent
 */
data class DirectMessageIncomingEvent(
    @JsonProperty("for_user_id")
    override val forUserId: String,
    override val users: Map<String, User>,
    val apps: Map<String, Application>?,
    @JsonProperty("direct_message_events")
    val directMessages: List<DirectMessage>
) : IncomingEvent() {
    override fun playerId(playerType: PlayerType): PlayerId =
        directMessages.first().playerId(playerType)

    override fun recipientId(playerType: PlayerType): PlayerId = directMessages.first().recipientId(playerType)
}