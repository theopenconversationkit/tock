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

import kotlinx.coroutines.runBlocking
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class MongoUserLockTest : AbstractTest() {

    private val userId: String = "aaa"

    @AfterEach
    @BeforeEach
    fun cleanup() {
        runBlocking {
            MongoUserLock.deleteLock(userId)
        }
    }

    @Test
    fun `take lock on a not locked user is ok`() {
        runBlocking {
            assertTrue(MongoUserLock.lock(userId))
        }
    }

    @Test
    fun `take lock on a recent locked user is ko`() {
        runBlocking {
            MongoUserLock.lock(userId)
            assertFalse(MongoUserLock.lock(userId))

            MongoUserLock.releaseLock(userId)
            assertTrue(MongoUserLock.lock(userId))
        }
    }

    @Test
    fun `take lock on a old locked user is ok`() {
        runBlocking {
            MongoUserLock.lock(userId)
            assertFalse(MongoUserLock.lock(userId))
            Thread.sleep(5100L)
            assertTrue(MongoUserLock.lock(userId))
        }
    }
}
