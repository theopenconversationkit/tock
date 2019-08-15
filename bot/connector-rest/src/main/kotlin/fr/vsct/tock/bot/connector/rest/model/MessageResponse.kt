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

package fr.vsct.tock.bot.connector.rest.model

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.message.GenericMessage
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.message.Sentence
import fr.vsct.tock.shared.Dice
import java.util.Locale

/**
 *
 */
internal data class MessageResponse(
    val messages: List<Message>,
    val applicationId: String,
    val userActionId: String = Dice.newId(),
    val userLocale: Locale? = null,
    val hasNlpStats: Boolean = false
) : ConnectorMessage {
    override val connectorType: ConnectorType = ConnectorType.rest

    override fun toGenericMessage(): GenericMessage? =
        messages.filterIsInstance<Sentence>().firstOrNull()?.messages?.firstOrNull()
}