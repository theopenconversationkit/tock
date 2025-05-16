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

package ai.tock.bot.connector.twitter.model

import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.Dice
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class DirectMessage(
    val id: String,
    @JsonProperty("created_timestamp") val created: Long,
    @JsonProperty("message_create") val messageCreated: MessageCreate
) {
    fun playerId(playerType: PlayerType): PlayerId =
        PlayerId(messageCreated.senderId, playerType)

    fun recipientId(playerType: PlayerType): PlayerId = PlayerId(
        messageCreated.target.recipientId,
        playerType
    )

    @JsonIgnore
    fun getMessageId(): String =
        id.run {
            if (isEmpty()) {
                Dice.newId()
            } else {
                this
            }
        }

    fun isQuote(): Boolean =
        textWithoutUrls().isBlank() && messageCreated.messageData.entities?.urls?.all { url -> url.url.startsWith("https://t.co/") } ?: false

    fun textWithoutUrls(): String {
        val messageData = messageCreated.messageData

        return messageData.entities?.urls?.fold(messageData.text) { acc, url ->
            acc.replace(url.url, "")
        } ?: messageData.text
    }
}
