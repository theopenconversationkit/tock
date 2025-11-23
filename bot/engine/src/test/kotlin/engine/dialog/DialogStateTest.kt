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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class DialogStateTest {
    @Test()
    fun `setContextValue returns an error if value is a list`() {
        assertThrows<IllegalStateException> {
            DialogState().setContextValue("test", emptyList<String>())
        }
    }

    @Test()
    fun `setContextValue returns an error if value is a map`() {
        assertThrows<IllegalStateException> {
            DialogState().setContextValue("test", mapOf<String, String>())
        }
    }

    @Test()
    fun `setContextValue removes an existing value if value parameter is null`() {
        val state = DialogState()
        state.setContextValue("test", "a")
        assertEquals(state.context["test"], "a")
        state.setContextValue("test", null)
        assertTrue(state.context.isEmpty())
    }
}
