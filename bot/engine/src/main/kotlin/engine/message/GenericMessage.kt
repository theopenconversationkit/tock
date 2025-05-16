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

package ai.tock.bot.engine.message

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType.Companion.none

/**
 * An aggregation of [Message]s used in [Sentence].
 * This is usually a "generic" view of [ConnectorMessage].
 */
data class GenericMessage(
    val connectorType: ConnectorType = ConnectorType.none,
    val attachments: List<Attachment> = emptyList(),
    val choices: List<Choice> = emptyList(),
    // a qualified text map (ie "title" to "Ok computer", "subtitle" to "please listen")
    val texts: Map<String, String> = emptyMap(),
    val locations: List<Location> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val subElements: List<GenericElement> = emptyList(),
    @Transient private val connectorMessage: ConnectorMessage? = null
) {

    companion object {
        /**
         * A [texts] parameter for title.
         */
        const val TITLE_PARAM = "title"

        /**
         * A [texts] parameter for subtitle.
         */
        const val SUBTITLE_PARAM = "subtitle"

        /**
         * A [texts] parameter for text content.
         */
        const val TEXT_PARAM = "text"
    }

    constructor(
        connectorMessage: ConnectorMessage,
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
        connectorMessage
    )

    internal fun findConnectorMessage() = connectorMessage

    internal fun obfuscate(): GenericMessage =
        if (connectorMessage == null) {
            this
        } else {
            connectorMessage.obfuscate().toGenericMessage()
                ?: GenericMessage(
                    connectorMessage.obfuscate(),
                    attachments,
                    choices,
                    texts,
                    locations,
                    metadata,
                    subElements
                )
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenericMessage

        if (connectorType != other.connectorType && connectorType != none && other.connectorType != none) return false
        if (attachments != other.attachments) return false
        if (choices != other.choices) return false
        if (texts != other.texts) return false
        if (locations != other.locations) return false
        if (metadata != other.metadata) return false
        if (subElements != other.subElements) return false

        return true
    }

    override fun hashCode(): Int {
        var result = attachments.hashCode()
        result = 31 * result + choices.hashCode()
        result = 31 * result + texts.hashCode()
        result = 31 * result + locations.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + subElements.hashCode()
        return result
    }
}
