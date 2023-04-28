/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.bean

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TickActionTest {

    @Test
    fun `given handler and trigger are null then action is not silent`() {
        assertFalse {
            TickAction(
                name = "action",
                handler = null,
                trigger = null,
                inputContextNames = emptySet(),
                outputContextNames = emptySet(),
                final = false
            ).isSilent()
        }
    }

    @Test
    fun `given handler and trigger are empty string then action is not silent`() {
        assertFalse {
            TickAction(
                name = "action",
                handler = "",
                trigger = "",
                inputContextNames = emptySet(),
                outputContextNames = emptySet(),
                final = false
            ).isSilent()
        }
    }

    @Test
    fun `given handler and trigger are blank string then action is not silent`() {
        assertFalse {
            TickAction(
                name = "action",
                handler = "   ",
                trigger = " ",
                inputContextNames = emptySet(),
                outputContextNames = emptySet(),
                final = false
            ).isSilent()
        }
    }

    @Test
    fun `given handler is not null or blank string then action is silent`() {
        assertTrue {
            TickAction(
                name = "action",
                handler = "handler",
                trigger = null,
                inputContextNames = emptySet(),
                outputContextNames = emptySet(),
                final = false
            ).isSilent()
        }
    }

    @Test
    fun `given trigger is not null or blank string then action is silent`() {
        assertTrue {
            TickAction(
                name = "action",
                handler = null,
                trigger = "trigger",
                inputContextNames = emptySet(),
                outputContextNames = emptySet(),
                final = false
            ).isSilent()
        }
    }
}