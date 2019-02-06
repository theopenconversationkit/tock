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
package fr.vsct.tock.bot.connector.twitter.model.outcoming

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import fr.vsct.tock.bot.connector.twitter.model.TwitterConnectorMessage
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType

/**
 * OutcomingEvent object
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = DirectMessageOutcomingEvent::class, name = "message_create")
)
abstract class AbstractOutcomingEvent {
    abstract fun playerId(playerType: PlayerType): PlayerId
    abstract fun recipientId(playerType: PlayerType): PlayerId
}