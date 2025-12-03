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

package ai.tock.bot.connector.messenger.model.send

import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.message.Choice
import ai.tock.shared.mapNotNullValues
import com.fasterxml.jackson.annotation.JsonProperty

data class TextQuickReply(
    val title: String,
    val payload: String,
    @JsonProperty("image_url") val imageUrl: String? = null,
) : QuickReply(QuickReplyContentType.text) {
    override fun toChoice(): Choice? {
        return SendChoice.decodeChoiceId(payload)
            .let { (intent, params) ->
                Choice(
                    intent,
                    params +
                        mapNotNullValues(
                            SendChoice.TITLE_PARAMETER to title,
                            SendChoice.IMAGE_PARAMETER to imageUrl,
                        ),
                )
            }
    }
}
