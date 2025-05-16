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

package ai.tock.bot.mongo

import ai.tock.bot.engine.user.UserState
import ai.tock.bot.mongo.UserTimelineCol.UserStateWrapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 *
 */
class UserStateWrapperTest : AbstractTest() {

    @Test
    fun constructor_shouldEncryptProperty_whenEncryptedFlagIsSet() {
        val userState = UserState()
        userState.setUnlimitedFlag("test1", "a")
        userState.setUnlimitedFlag("test8", "b")
        val wrapper = UserStateWrapper(userState)

        assertTrue(wrapper.flags["test1"]!!.encrypted)
        assertFalse(wrapper.flags["test8"]!!.encrypted)

        assertNotEquals(userState.flags["test1"]!!.value, wrapper.flags["test1"]!!.value)
        assertEquals(userState.flags["test8"]!!.value, wrapper.flags["test8"]!!.value)

        assertEquals(userState.flags["test1"], wrapper.flags["test1"]!!.toTimeBoxedFlag())
        assertEquals(userState.flags["test8"], wrapper.flags["test8"]!!.toTimeBoxedFlag())
    }
}
