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
package ai.tock.bot.engine.dialog

import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.EntityType
import ai.tock.nlp.entity.NumberValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 *
 */
class EntityStateValueTest {

    @Test
    fun changeValue_shouldAddToHistory_whenChangeValue() {
        val state = EntityStateValue(null)

        assertNull(state.value)
        assertFalse(state.history.isEmpty())
        assertTrue(state.previousValues.isEmpty())

        val entity = Entity(EntityType("test"), "role")

        val value1 = NumberValue(1)

        state.changeValue(entity, value1)

        assertEquals(value1, state.value?.value)
        assertEquals(2, state.history.size)
        assertEquals(value1, state.history[1].entityValue?.value)
        assertEquals(state.history[1].date, state.lastUpdate)
        assertFalse(state.previousValues.isEmpty())

        val updateDate = state.lastUpdate
        val value2 = NumberValue(2)
        Thread.sleep(1)
        state.changeValue(entity, value2)

        assertEquals(value2, state.value?.value)
        assertEquals(3, state.history.size)
        assertEquals(value1, state.history[1].entityValue?.value)
        assertEquals(state.history[1].date, updateDate)
        assertEquals(value2, state.history[2].entityValue?.value)
        assertEquals(state.history[2].date, state.lastUpdate)
        assertTrue(state.lastUpdate > updateDate)
        assertEquals(value1, state.previousValues[1].entityValue?.value)
        assertEquals(state.previousValues[1].date, updateDate)
        assertEquals(value1, state.penultimateValue?.entityValue?.value)
    }
}
