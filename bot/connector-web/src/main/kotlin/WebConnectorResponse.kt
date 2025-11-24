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
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.bot.engine.message.GenericMessage.Companion.TEXT_PARAM
import ai.tock.shared.mapNotNullValues
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY

@Deprecated("Use the new WebMessage implementation v1")
@JsonInclude(NON_EMPTY)
data class OldWebMessage(
    val text: String? = null,
    val buttons: List<WebButton> = emptyList(),
    val card: MediaCard? = null,
    val carousel: MediaCarousel? = null,
) : WebConnectorMessage {
    @get:JsonIgnore
    override val connectorType: ConnectorType = webConnectorType

    override fun toGenericMessage(): GenericMessage? =
        card?.toGenericMessage()
            ?: carousel?.toGenericMessage()
            ?: GenericMessage(
                connectorType = webConnectorType,
                texts = mapNotNullValues(TEXT_PARAM to text),
                choices = buttons.map { it.toChoice() },
            )
}

@Deprecated("Use the WebBuilders methods to create buttons")
data class WebButton(val title: String, val payload: String? = null, val imageUrl: String? = null) {
    fun toChoice(): Choice =
        if (payload == null) {
            Choice.fromText(title)
        } else {
            SendChoice.decodeChoiceId(payload).let { (intent, params) ->
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

internal data class WebConnectorResponse(
    override val responses: List<WebMessage>,
    override val metadata: Map<String, String> = emptyMap(),
) : WebConnectorResponseContract
