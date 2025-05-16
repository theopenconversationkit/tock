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

import ai.tock.bot.engine.message.GenericMessage
import com.fasterxml.jackson.annotation.JsonProperty

data class MessageData(
    val text: String,
    val entities: Entities? = null,
    val ctas: List<CTA>? = null,
    val attachment: Attachment? = null,
    @JsonProperty("quick_reply") val quickReply: QuickReply? = null,
    @JsonProperty("quick_reply_response") val quickReplyResponse: QuickReplyResponse? = null
) {
    fun toGenericMessage(): GenericMessage {
        return GenericMessage(
            texts = mapOf("text" to text),
            choices = (quickReply?.toChoices() ?: emptyList()) + (ctas?.map { it.toChoice() } ?: emptyList())
        )
    }
}
