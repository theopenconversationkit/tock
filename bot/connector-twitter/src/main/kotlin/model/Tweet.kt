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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Tweet(
    @JsonProperty("created_at") val created: String,
    val id: Long,
    val text: String,
    val lang: String,
    val truncated: Boolean,
    val user: User,
    @JsonProperty("in_reply_to_status_id") val inReplyToStatusId: Long? = null,
    @JsonProperty("in_reply_to_user_id") val inReplyToUserId: Long? = null,
    @JsonProperty("in_reply_to_screen_name") val contributors: String? = null,
    @JsonProperty("is_quote_status") val isQuote: Boolean,
    val coordinates: Coordinates? = null,
    val entities: Entities,
    @JsonProperty("extended_entities") val extendedEntities: Entities? = null,
    @JsonProperty("extended_tweet") val extendedTweet: Text? = null
) {
    fun playerId(playerType: PlayerType): PlayerId =
        PlayerId(user.id, playerType)
}
