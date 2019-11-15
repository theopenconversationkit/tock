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

package ai.tock.bot.mongo

import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.mongo.UserTimelineCol.UserPreferencesWrapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 *
 */
class UserPreferencesWrapperTest : AbstractTest() {

    @Test
    fun testUserPreferencesEncryption() {
        val u = UserPreferences("a", "b", "c", picture = "z", gender = "e")
        val wrapper = UserPreferencesWrapper(u)

        assertTrue(wrapper.encrypted)
        assertNotEquals(u.firstName, wrapper.firstName)
        assertNotEquals(u.lastName, wrapper.lastName)
        assertNotEquals(u.email, wrapper.email)
        assertNotEquals(u.gender, wrapper.gender)
        assertNotEquals(u.picture, wrapper.picture)

        assertEquals(u, wrapper.toUserPreferences())
    }
}