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

package ai.tock.bot.admin.message

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.translator.I18nLabelValue

/**
 * An aggregation of [Message]s used in [SentenceConfiguration].
 * This a usually "generic" view of [ConnectorMessage].
 */
data class SentenceElementConfiguration(
    val connectorType: ConnectorType = ConnectorType.none,
    val attachments: List<AttachmentConfiguration> = emptyList(),
    val choices: List<ChoiceConfiguration> = emptyList(),
        //a qualified text map (ie "title" to "Ok computer", "subtitle" to "please listen")
    val texts: Map<String, I18nLabelValue> = emptyMap(),
    val locations: List<LocationConfiguration> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val subElements: List<SentenceSubElementConfiguration> = emptyList(),
    @Transient private val connectorMessage: ConnectorMessage? = null) {

    internal fun findConnectorMessage() = connectorMessage

}