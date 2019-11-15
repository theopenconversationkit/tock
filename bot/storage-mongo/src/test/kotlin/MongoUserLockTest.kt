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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 */
class MongoUserLockTest : AbstractTest() {

    private val userId: String = "aaa"

    @AfterEach
    @BeforeEach
    fun cleanup() {
        MongoUserLock.deleteLock(userId)
    }

    @Test
    fun `take lock on a not locked user is ok`() {
        assertTrue(MongoUserLock.lock(userId))
    }

    @Test
    fun `take lock on a recent locked user is ko`() {
        MongoUserLock.lock(userId)
        assertFalse(MongoUserLock.lock(userId))

        MongoUserLock.releaseLock(userId)
        assertTrue(MongoUserLock.lock(userId))
    }

    @Test
    fun `take lock on a old locked user is ok`() {
        MongoUserLock.lock(userId)
        assertFalse(MongoUserLock.lock(userId))
        Thread.sleep(5100L)
        assertTrue(MongoUserLock.lock(userId))
    }
}