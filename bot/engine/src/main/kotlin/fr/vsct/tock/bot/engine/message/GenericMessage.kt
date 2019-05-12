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

package fr.vsct.tock.bot.engine.message

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType

@Deprecated("now GenericMessage", replaceWith = ReplaceWith("GenericMessage", "fr.vsct.tock.bot.engine.message.GenericMessage"))
typealias SentenceElement = GenericMessage

/**
 * An aggregation of [Message]s used in [Sentence].
 * This is usually a "generic" view of [ConnectorMessage].
 */
data class GenericMessage(
    val connectorType: ConnectorType = ConnectorType.none,
    val attachments: List<Attachment> = emptyList(),
    val choices: List<Choice> = emptyList(),
    //a qualified text map (ie "title" to "Ok computer", "subtitle" to "please listen")
    val texts: Map<String, String> = emptyMap(),
    val locations: List<Location> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val subElements: List<GenericElement> = emptyList(),
    @Transient private val connectorMessage: ConnectorMessage? = null
) {

    constructor(connectorMessage: ConnectorMessage,
                attachments: List<Attachment> = emptyList(),
                choices: List<Choice> = emptyList(),
                texts: Map<String, String> = emptyMap(),
                locations: List<Location> = emptyList(),
                metadata: Map<String, String> = emptyMap(),
                subElements: List<GenericElement> = emptyList()
    ) : this(
        connectorMessage.connectorType,
        attachments,
        choices,
        texts,
        locations,
        metadata,
        subElements,
        connectorMessage) {
    }

    internal fun findConnectorMessage() = connectorMessage

}