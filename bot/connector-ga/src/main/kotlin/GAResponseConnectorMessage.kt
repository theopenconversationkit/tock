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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ga.model.response.GAExpectedInput
import ai.tock.bot.connector.ga.model.response.GAFinalResponse
import ai.tock.bot.engine.message.GenericMessage
import com.fasterxml.jackson.annotation.JsonIgnore

/**
 *
 */
data class GAResponseConnectorMessage(
    val expectUserResponse: Boolean = true,
    val expectedInputs: List<GAExpectedInput> = emptyList(),
    val finalResponse: GAFinalResponse? = null,
    val logoutEvent: Boolean = false
) : ConnectorMessage {

    constructor(input: GAExpectedInput) : this(expectedInputs = listOf(input))

    @Transient
    val expectedInput: GAExpectedInput? = expectedInputs.firstOrNull()

    override val connectorType: ConnectorType @JsonIgnore get() = gaConnectorType

    override fun toGenericMessage(): GenericMessage? = expectedInput?.toGenericMessage() ?: finalResponse?.toGenericMessage()
}
