/*
 * Copyright (C) 2017/2024 e-voyageurs technologies
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

package ai.tock.bot.connector.mattermost.model

import ai.tock.bot.engine.message.GenericMessage
import com.fasterxml.jackson.annotation.JsonProperty

data class MattermostMessageIn(
    @get:JsonProperty("channel_id")
    val channelId: String,
    @get:JsonProperty("channel_name")
    val channelName: String,
    @get:JsonProperty("team_domain")
    val teamDomain: String,
    @get:JsonProperty("team_id")
    val teamId: String,
    @get:JsonProperty("post_id")
    val postId: String? = null,
    var text: String,
    val timestamp: Long? = null,
    val token: String,
    @get:JsonProperty("trigger_word")
    val triggerWord: String? = "",
    @get:JsonProperty("user_id")
    val userId: String,
    @get:JsonProperty("user_name")
    val userName: String
) : MattermostConnectorMessage() {

    fun getRealMessage(): String {
        return this.text.replace("${this.triggerWord} ", "")
    }

    override fun toGenericMessage(): GenericMessage =
        GenericMessage(
            texts = mapOf(GenericMessage.TEXT_PARAM to text)
        )
}