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

package ai.tock.bot.connector.alexa

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.action.SendAttachment.AttachmentType
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.bot.engine.message.GenericMessage.Companion.TEXT_PARAM
import ai.tock.bot.engine.message.GenericMessage.Companion.TITLE_PARAM
import ai.tock.shared.mapNotNullValues
import com.amazon.speech.ui.Card
import com.amazon.speech.ui.StandardCard

/**
 * An alexa message.
 */
data class AlexaMessage(
    /**
     * Does Alexa has to quit the skill?
     */
    val end: Boolean,
    /**
     * Is there a card to send?
     */
    val card: Card? = null,
    /**
     * Is there a reprompt?
     */
    val reprompt: String? = null,
) : ConnectorMessage {
    override val connectorType: ConnectorType = alexaConnectorType

    override fun toGenericMessage(): GenericMessage {
        return GenericMessage(
            attachments =
                listOfNotNull(
                    card?.run {
                        if (this is StandardCard) {
                            Attachment(image.smallImageUrl, AttachmentType.image)
                        } else {
                            null
                        }
                    },
                ),
            texts =
                mapNotNullValues(
                    ::reprompt.name to reprompt,
                    TITLE_PARAM to card?.title,
                    TEXT_PARAM to (card as? StandardCard)?.text,
                ),
            metadata = mapOf(::end.name to end.toString()),
        )
    }
}
