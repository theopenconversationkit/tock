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

import ai.tock.bot.engine.user.LockLostException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.getCollection
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
            assertTrue(MongoUserLock.tryLock(userId))
        }
    }

    @Test
    fun `take lock on a recent locked user is ko`() {
        runBlocking {
            MongoUserLock.tryLock(userId)
            assertFalse(MongoUserLock.tryLock(userId))

            MongoUserLock.releaseLock(userId)
            assertTrue(MongoUserLock.tryLock(userId))
        }
    }

    @Test
    fun `take lock on a old locked user is ok`() {
        runBlocking {
            MongoUserLock.tryLock(userId)
            assertFalse(MongoUserLock.tryLock(userId))
            Thread.sleep(5100L)
            assertTrue(MongoUserLock.tryLock(userId))
        }
    }

    // the following tests rely on the default lock timeout (5s), so the heartbeat runs every 2.5s

    private val lockCol by lazy { MongoBotConfiguration.asyncDatabase.getCollection<MongoUserLock.UserLock>().coroutine }

    private suspend fun currentLock(): MongoUserLock.UserLock? = lockCol.findOneById(userId)

    @Test
    fun `GIVEN default renewal WHEN op runs longer than the lock timeout THEN the lock is kept and released at the end`() {
        runBlocking {
            val result =
                async {
                    MongoUserLock.withLock(userId) {
                        delay(6.seconds)
                        "done"
                    }
                }
            delay(5500.milliseconds)
            assertFalse(MongoUserLock.tryLock(userId), "the lease should have been renewed")
            assertEquals("done", result.await())
            assertTrue(MongoUserLock.tryLock(userId), "the lock should have been released")
        }
    }

    @Test
    fun `GIVEN no renewal and abortOnLockLoss WHEN the lock is taken over THEN op is aborted and the new owner keeps the lock`() {
        runBlocking {
            val result =
                async {
                    runCatching {
                        MongoUserLock.withLock(userId, abortOnLockLoss = true, maxLeaseRenewal = Duration.ZERO) {
                            delay(20.seconds)
                        }
                    }
                }
            delay(500.milliseconds)
            val initialDate = assertNotNull(currentLock()).date
            // the heartbeat has run once, without renewing the lease
            delay(2500.milliseconds)
            assertEquals(initialDate, assertNotNull(currentLock()).date)

            // the lease has expired: another owner takes over
            delay(2200.milliseconds)
            assertTrue(MongoUserLock.tryLock(userId))

            assertFailsWith<LockLostException> { result.await().getOrThrow() }
            // the lock of the new owner has not been released
            val lock = assertNotNull(currentLock())
            assertTrue(lock.locked)
            assertNull(lock.lockId)
        }
    }

    @Test
    fun `GIVEN no renewal and no abortOnLockLoss WHEN the lock is taken over THEN op completes without releasing the new owner lock`() {
        runBlocking {
            val result =
                async {
                    MongoUserLock.withLock(userId, abortOnLockLoss = false, maxLeaseRenewal = Duration.ZERO) {
                        delay(8.seconds)
                        "done"
                    }
                }
            delay(5200.milliseconds)
            assertTrue(MongoUserLock.tryLock(userId))

            assertEquals("done", result.await())
            val lock = assertNotNull(currentLock())
            assertTrue(lock.locked)
            assertNull(lock.lockId)
        }
    }

    @Test
    fun `GIVEN a max lease renewal WHEN op runs longer THEN the lease is no longer renewed and expires`() {
        runBlocking {
            val result =
                async {
                    MongoUserLock.withLock(userId, abortOnLockLoss = false, maxLeaseRenewal = 3.seconds) {
                        delay(20.seconds)
                    }
                }
            try {
                // renewed at 2.5s, not at 5s: the lease is valid until 7.5s
                delay(6.seconds)
                assertFalse(MongoUserLock.tryLock(userId), "the lease should have been renewed once")
                delay(2.seconds)
                assertTrue(MongoUserLock.tryLock(userId), "the lease should have expired")
            } finally {
                result.cancel()
            }
        }
    }
}
