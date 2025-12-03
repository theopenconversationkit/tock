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

package ai.tock.bot.connector.slack.model.old

import ai.tock.bot.connector.slack.model.SlackConnectorMessage
import ai.tock.bot.engine.message.GenericMessage

data class SlackMessageIn(
    val token: String,
    val team_id: String,
    val team_domain: String,
    val channel_id: String,
    val channel_name: String,
    val timestamp: Number,
    val user_id: String,
    val user_name: String,
    var text: String,
    val trigger_word: String?,
) : SlackConnectorMessage() {
    fun getRealMessage(): String {
        return this.text.replace("${this.trigger_word} ", "")
    }

    override fun toGenericMessage(): GenericMessage = GenericMessage(texts = mapOf(::text.name to text))
}
