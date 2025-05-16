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

import ai.tock.bot.api.model.message.bot.CustomMessage
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.shared.jackson.ConstrainedValueWrapper
import ai.tock.shared.jackson.addConstrainedTypes
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class BotResponseTest {

    data class Message1(val b: Boolean) : ConnectorMessage {
        override val connectorType: ConnectorType = ConnectorType("m1")
    }

    data class Message2(val b: Boolean) : ConnectorMessage {
        override val connectorType: ConnectorType = ConnectorType("m2")
    }

    @BeforeEach
    fun beforeEach() {
        addConstrainedTypes(setOf(Message1::class))
    }

    @Test
    fun `BotResponse with CustomMessage is deserialized if message is registered`() {
        val initial = CustomMessage(ConstrainedValueWrapper(Message1(true)))
        val json = mapper.writeValueAsString(initial)
        val m = mapper.readValue<CustomMessage>(json)
        assertEquals(initial, m)
    }

    @Test
    fun `BotResponse with CustomMessage throw an error if message is not registered`() {
        val initial = CustomMessage(ConstrainedValueWrapper(Message2(true)))
        val json = mapper.writeValueAsString(initial)
        assertThrows<AssertionError> {
            mapper.readValue<CustomMessage>(json)
        }
    }
}
