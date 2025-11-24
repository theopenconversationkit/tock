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
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import com.amazon.speech.speechlet.IntentRequest
import mu.KotlinLogging

/**
 * The user message sent by Alexa.
 */
data class AlexaInputMessage(
    /**
     * The Alexa [IntentRequest] json serialized.
     */
    val intentRequest: String,
) : ConnectorMessage {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    constructor(intentRequest: IntentRequest) : this(
        try {
            mapper.writeValueAsString(intentRequest)
        } catch (e: Exception) {
            logger.error(e)
            "{}"
        },
    )

    override val connectorType: ConnectorType = alexaConnectorType

    override fun toGenericMessage(): GenericMessage {
        return GenericMessage(
            texts = mapOf("json" to intentRequest),
        )
    }
}
