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
import fr.vsct.tock.bot.connector.twitter.model.Tweet
import fr.vsct.tock.bot.connector.twitter.model.User
import fr.vsct.tock.bot.engine.action.ActionMetadata
import fr.vsct.tock.bot.engine.action.ActionVisibility
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
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

    override val users: Map<String, User>
        get() = mapOf(Pair(tweets.first().user.id, tweets.first().user))

    override fun playerId(playerType: PlayerType): PlayerId =
        tweets.first().playerId(playerType)

    override fun toEvent(applicationId: String): Event? {
        val tweet = tweets.first()
        // Ignore all replies and quoted tweets && message from account listened
        val isReplyMessage = tweet.isQuote && tweet.inReplyToStatusId == null
        val isFromAccountListened = forUserId.equals(tweet.user.id)
        return if (!isReplyMessage && !isFromAccountListened) {
            SendSentence(
                playerId(PlayerType.user),
                applicationId,
                PlayerId(forUserId, PlayerType.bot),
                //extended entities and full_text
                tweet.extendedTweet?.text ?: tweet.text,
                metadata = ActionMetadata(visibility = ActionVisibility.public)
            )
        } else {
            logger.debug { "ignore event $this with tweet text = [${tweet.text}] from [${tweet.user.id}][${tweet.user.name}]" }
            null
        }
    }
}