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

package ai.tock.bot.connector.alcmeon

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.messenger.model.send.Message
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppSendBotMessage
import com.fasterxml.jackson.annotation.JsonIgnore

sealed class AlcmeonConnectorMessageResponse : ConnectorMessage {
    override val connectorType: ConnectorType @JsonIgnore get() = alcmeonConnectorType
    abstract val exit: AlcmeonConnectorMessageExit?
    abstract val variables: Map<String, String>

    data class AlcmeonConnectorMessageWhatsappResponse(
        val messages: List<AlcmeonConnectorMessageOut<WhatsAppSendBotMessage>>,
        override val exit: AlcmeonConnectorMessageExit? = null,
        override val variables: Map<String, String> = emptyMap(),
    ) : AlcmeonConnectorMessageResponse()

    data class AlcmeonConnectorMessageFacebookResponse(
        val messages: List<AlcmeonConnectorMessageOut<Message>>,
        override val exit: AlcmeonConnectorMessageExit? = null,
        override val variables: Map<String, String> = emptyMap(),
    ) : AlcmeonConnectorMessageResponse()
}


data class AlcmeonConnectorMessageOut<T>(val body: T, val delay_ms: Int = 0)

data class AlcmeonConnectorMessageExit(val reason: String, val delay_ms: Int = 0)
