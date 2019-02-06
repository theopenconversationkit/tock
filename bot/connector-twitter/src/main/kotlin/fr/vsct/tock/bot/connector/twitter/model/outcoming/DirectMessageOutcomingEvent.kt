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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import fr.vsct.tock.bot.connector.twitter.model.MessageCreate
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType

/**
 * Direct Message OutcomingEvent
 */
@JsonTypeName("message_create")
data class DirectMessageOutcomingEvent(
    @JsonProperty("message_create")
    val messageCreate: MessageCreate
) : AbstractOutcomingEvent() {
    override fun playerId(playerType: PlayerType): PlayerId =
        PlayerId(messageCreate.senderId, playerType)


    override fun recipientId(playerType: PlayerType): PlayerId = PlayerId(
        messageCreate.target.recipientId,
        playerType
    )
}