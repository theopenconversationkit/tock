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

import ai.tock.bot.engine.action.SendChoice.Companion.EXIT_INTENT
import ai.tock.bot.engine.action.SendChoice.Companion.IMAGE_PARAMETER
import ai.tock.bot.engine.action.SendChoice.Companion.TITLE_PARAMETER
import ai.tock.bot.engine.action.SendChoice.Companion.URL_PARAMETER
import ai.tock.bot.engine.message.Choice
import ai.tock.shared.mapNotNullValues
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("url_button")
data class UrlButton(
    val title: String,
    val url: String,
    val imageUrl: String? = null
) : Button(ButtonType.web_url) {

    override fun toChoice(): Choice =
        Choice(
            EXIT_INTENT,
            mapNotNullValues(
                TITLE_PARAMETER to title,
                URL_PARAMETER to url,
                IMAGE_PARAMETER to imageUrl
            )
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UrlButton) return false
        if (title != other.title) return false
        if (url != other.url) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }
}
