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

import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.message.Choice
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("web_url")
data class WebUrl(
    val label: String,
    val url: String,
    @JsonProperty("tco_url") val tcoUrl: String? = null,
) : CTA() {
    override fun toChoice(): Choice =
        Choice(
            SendChoice.EXIT_INTENT,
            mapOf(
                SendChoice.URL_PARAMETER to url,
                SendChoice.TITLE_PARAMETER to label,
            ),
        )
}
