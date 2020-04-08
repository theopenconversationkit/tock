/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.bot.connector.web.send

import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.message.Choice
import ai.tock.shared.mapNotNullValues
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("quick_reply")
data class QuickReply(val title: String, val payload: String?, val imageUrl: String?) : Button(ButtonType.quick_reply) {

    override fun toChoice(): Choice =
        if (payload == null) {
            Choice.fromText(title)
        } else {
            SendChoice.decodeChoiceId(payload).let { (intent, params) ->
                Choice(
                    intent,
                    params + mapNotNullValues(
                        SendChoice.TITLE_PARAMETER to title,
                        SendChoice.IMAGE_PARAMETER to imageUrl
                    )
                )
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QuickReply) return false
        if (title != other.title) return false
        if (payload != other.payload) return false
        if (imageUrl != other.imageUrl) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + (payload?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        return result
    }
}
