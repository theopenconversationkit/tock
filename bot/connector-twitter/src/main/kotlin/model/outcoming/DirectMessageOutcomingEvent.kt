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
package ai.tock.bot.connector.twitter.model.outcoming

import ai.tock.bot.connector.twitter.model.MessageCreate
import ai.tock.bot.connector.twitter.model.MessageData
import ai.tock.bot.connector.twitter.model.Option
import ai.tock.bot.connector.twitter.model.OptionWithoutDescription
import ai.tock.bot.connector.twitter.model.Options
import ai.tock.bot.connector.twitter.model.Recipient
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Direct Message OutcomingEvent
 */
@JsonTypeName("message_create")
data class DirectMessageOutcomingEvent(
    @JsonProperty("message_create")
    val messageCreate: MessageCreate
) : AbstractOutcomingEvent() {
    override fun toGenericMessage(): GenericMessage =
        messageCreate.messageData.toGenericMessage()

    override fun playerId(playerType: PlayerType): PlayerId =
        PlayerId(messageCreate.senderId, playerType)

    override fun recipientId(playerType: PlayerType): PlayerId = PlayerId(
        messageCreate.target.recipientId,
        playerType
    )

    override fun toString(): String = messageCreate.messageData.text

    companion object {
        fun builder(
            target: Recipient,
            senderId: String,
            text: String
        ) = Builder(target, senderId, text)
    }

    class Builder(
        val target: Recipient,
        val senderId: String,
        val text: String
    ) {

        var sourceAppId: String? = null
        var options: List<Option> = listOf()
        var optionsWithoutDescription: List<OptionWithoutDescription> = listOf()

        fun build(): DirectMessageOutcomingEvent {
            return DirectMessageOutcomingEvent(
                MessageCreate(
                    target = target,
                    sourceAppId = sourceAppId,
                    senderId = senderId,
                    messageData = MessageData(
                        text = text,
                        quickReply =
                        if (!options.isEmpty())
                            Options(options)
                        else if (!optionsWithoutDescription.isEmpty())
                            Options(optionsWithoutDescription)
                        else null
                    )
                )
            )
        }

        fun withSourceAppId(sourceAppId: String): Builder {
            this.sourceAppId = sourceAppId
            return this
        }

        fun withOptions(vararg options: Option): Builder {
            this.options = options.toList()
            return this
        }

        fun withOptions(vararg optionsWithoutDescription: OptionWithoutDescription): Builder {
            this.optionsWithoutDescription = optionsWithoutDescription.toList()
            return this
        }
    }
}
