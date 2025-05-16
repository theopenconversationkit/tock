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

import ai.tock.bot.connector.twitter.model.Tweet
import ai.tock.bot.connector.twitter.model.User
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.ActionQuote
import ai.tock.bot.engine.action.ActionReply
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import com.fasterxml.jackson.annotation.JsonProperty
import mu.KotlinLogging

/**
 * Tweet (Status) IncomingEvent
 */
data class TweetIncomingEvent(
    @JsonProperty("for_user_id") override val forUserId: String,
    @JsonProperty("tweet_create_events") val tweets: List<Tweet>
) : IncomingEvent() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val ignored: Boolean
        get() = false

    override val users: Map<String, User>
        get() = mapOf(Pair(tweets.first().user.id, tweets.first().user))

    override fun playerId(playerType: PlayerType): PlayerId =
        tweets.first().playerId(playerType)

    override fun toEvent(applicationId: String): Event? {
        val tweet = tweets.first()

        val isReplyMessage = tweet.inReplyToStatusId != null
        val isFromAccountListened = forUserId == tweet.user.id
        // Ignore all replies from account listened
        return if (!isFromAccountListened) {
            SendSentence(
                playerId(PlayerType.user),
                applicationId,
                PlayerId(forUserId, PlayerType.bot),
                // extended entities and full_text
                tweet.extendedTweet?.text ?: tweet.text,
                metadata = ActionMetadata(
                    visibility = ActionVisibility.PUBLIC,
                    replyMessage = if (isReplyMessage) ActionReply.ISREPLY else ActionReply.NOREPLY,
                    quoteMessage = if (tweet.isQuote) ActionQuote.ISQUOTE else ActionQuote.NOQUOTE
                )
            )
        } else {
            logger.debug { "ignore event $this with tweet text = [${tweet.text}] from [${tweet.user.id}][${tweet.user.name}]" }
            null
        }
    }
}
