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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.web.send.Button
import ai.tock.bot.connector.web.send.Footnote
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.connector.web.send.WebDeepLink
import ai.tock.bot.connector.web.send.WebImage
import ai.tock.bot.connector.web.send.WebMessageContent
import ai.tock.bot.connector.web.send.WebMessageContract
import ai.tock.bot.connector.web.send.WebWidget
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.shared.mapNotNullValues
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class WebMessage(
    override val text: String? = null,
    override val buttons: List<Button> = emptyList(),
    override val card: WebCard? = null,
    override val carousel: WebCarousel? = null,
    override val widget: WebWidget? = null,
    override val image: WebImage? = null,
    override val version: String = "1",
    override val deepLink: WebDeepLink? = null,
    override val footnotes: List<Footnote> = emptyList(),
) : WebMessageContract, WebConnectorMessage {
    constructor(content: WebMessageContent) : this(
        content.text,
        content.buttons,
        content.card,
        content.carousel,
        content.widget,
        content.image,
        content.version,
        content.deepLink,
        content.footnotes,
    )

    @get:JsonIgnore
    override val connectorType: ConnectorType = webConnectorType

    override fun toGenericMessage(): GenericMessage =
        card?.toGenericMessage()
            ?: carousel?.toGenericMessage()
            ?: widget?.toGenericMessage()
            ?: image?.toGenericMessage()
            ?: GenericMessage(
                connectorType = webConnectorType,
                texts = mapNotNullValues(GenericMessage.TEXT_PARAM to text),
                choices = buttons.map { it.toChoice() },
            )
}
