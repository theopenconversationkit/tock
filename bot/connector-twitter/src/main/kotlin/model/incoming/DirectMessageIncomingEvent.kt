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
package ai.tock.bot.connector.twitter.model.incoming

import ai.tock.bot.connector.twitter.model.Application
import ai.tock.bot.connector.twitter.model.DirectMessage
import ai.tock.bot.connector.twitter.model.OptionsResponse
import ai.tock.bot.connector.twitter.model.User
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.ContinuePublicConversationInPrivateEvent
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import com.fasterxml.jackson.annotation.JsonProperty
import mu.KotlinLogging

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
    override val ignored: Boolean
        get() = false

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun playerId(playerType: PlayerType): PlayerId =
        directMessages.first().playerId(playerType)

    override fun recipientId(playerType: PlayerType): PlayerId = directMessages.first().recipientId(playerType)

    override fun toEvent(
        applicationId: String
    ): Event? {
        // ignore direct message sent from the bot
        val firstOrNull = directMessages.firstOrNull()
        return if (forUserId != firstOrNull?.messageCreated?.senderId) {
            firstOrNull?.let {
                val quickReplyResponse = it.messageCreated.messageData.quickReplyResponse
                return if (quickReplyResponse != null) {
                    when (quickReplyResponse) {
                        is OptionsResponse -> {
                            SendChoice.decodeChoice(
                                quickReplyResponse.metadata,
                                playerId(PlayerType.user),
                                applicationId,
                                recipientId(PlayerType.bot)
                            ).apply {
                                metadata.visibility = ActionVisibility.PRIVATE
                            }
                        }
                        else -> {
                            logger.debug { "unknown quick reply response type $this" }
                            null
                        }
                    }
                } else {
                    if (it.isQuote()) {
                        ContinuePublicConversationInPrivateEvent(playerId(PlayerType.user), recipientId(PlayerType.bot), applicationId)
                    } else {
                        SendSentence(
                            playerId(PlayerType.user),
                            applicationId,
                            recipientId(PlayerType.bot),
                            it.textWithoutUrls(),
                            metadata = ActionMetadata(visibility = ActionVisibility.PRIVATE)
                        )
                    }
                }
            }
        } else {
            logger.debug { "ignore event $this from applicationId $applicationId" }
            null
        }
    }
}
