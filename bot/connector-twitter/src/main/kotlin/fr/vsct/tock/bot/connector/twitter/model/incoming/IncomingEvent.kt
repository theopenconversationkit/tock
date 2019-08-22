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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import fr.vsct.tock.bot.connector.twitter.json.EventDeserializer
import fr.vsct.tock.bot.connector.twitter.model.TwitterConnectorMessage
import fr.vsct.tock.bot.connector.twitter.model.User
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType

/**
 * IncomingEvent object
 */
@JsonDeserialize(using = EventDeserializer::class)
abstract class IncomingEvent : TwitterConnectorMessage() {
    abstract val forUserId: String
    abstract val users: Map<String, User>
    abstract val ignored: Boolean

    open fun playerId(playerType: PlayerType): PlayerId =
        PlayerId(users.values.firstOrNull()?.id ?: error("null sender field in IncomingEvent"), playerType)

    open fun recipientId(playerType: PlayerType): PlayerId = PlayerId(
        users.values.lastOrNull()?.id ?: error("id or userRef must not be null"),
        playerType
    )

    abstract fun toEvent(applicationId: String): Event?
}