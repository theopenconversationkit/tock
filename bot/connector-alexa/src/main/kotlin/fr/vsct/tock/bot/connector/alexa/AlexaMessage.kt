/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.alexa

import com.amazon.speech.ui.Card
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType

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
    val reprompt: String? = null
) : ConnectorMessage {

    override val connectorType: ConnectorType = alexaConnectorType

}