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

package ai.tock.bot.connector.web.sse

import ai.tock.bot.connector.web.WebConnectorResponseContract
import ai.tock.bot.connector.web.send.Button
import ai.tock.bot.connector.web.send.Footnote
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.connector.web.send.WebDeepLink
import ai.tock.bot.connector.web.send.WebImage
import ai.tock.bot.connector.web.send.WebMessageContract
import ai.tock.bot.connector.web.send.WebWidget
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
internal data class TestWebMessage(
    override val text: String? = null,
) : WebMessageContract {
    override val buttons: List<Button> = emptyList()
    override val card: WebCard? = null
    override val carousel: WebCarousel? = null
    override val widget: WebWidget? = null
    override val image: WebImage? = null
    override val version: String = "1"
    override val deepLink: WebDeepLink? = null
    override val footnotes: List<Footnote> = emptyList()
    override val actionId: String? = null
}

internal data class WebConnectorResponse(
    override val responses: List<WebMessageContract>,
    override val metadata: Map<String, String> = emptyMap(),
) : WebConnectorResponseContract {
    constructor(vararg messages: WebMessageContract) : this(listOf(*messages))
}
