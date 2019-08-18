/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.connector.web

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.media.MediaCard
import fr.vsct.tock.bot.connector.media.MediaCarousel
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.GenericMessage
import fr.vsct.tock.bot.engine.message.GenericMessage.Companion.TEXT_PARAM
import fr.vsct.tock.shared.mapNotNullValues

data class WebButton(val title: String, val payload: String? = null) {

    fun toChoice(): Choice =
        if (payload == null) {
            Choice.fromText(title)
        } else {
            SendChoice.decodeChoiceId(payload).let { (intent, params) ->
                Choice(intent, params + (SendChoice.TITLE_PARAMETER to title))
            }
        }
}

@JsonInclude(NON_EMPTY)
data class WebMessage(
    val text: String? = null,
    val buttons: List<WebButton> = emptyList(),
    val card: MediaCard? = null,
    val carousel: MediaCarousel? = null) : ConnectorMessage {

    @get:JsonIgnore
    override val connectorType: ConnectorType = webConnectorType

    override fun toGenericMessage(): GenericMessage? =
        card?.toGenericMessage()
            ?: carousel?.toGenericMessage()
            ?: GenericMessage(
                connectorType = webConnectorType,
                texts = mapNotNullValues(TEXT_PARAM to text),
                choices = buttons.map { it.toChoice() }
            )
}

internal data class WebConnectorResponse(val responses: List<WebMessage>)