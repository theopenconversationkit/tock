/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.storage.dynamodb

import ai.tock.bot.engine.user.LockLostException
import ai.tock.bot.engine.user.PlayerId
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.atomic.AtomicInteger
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
 * The user lock on DynamoDB Local, with leases of 1s (see [DynamoLocal]): the heartbeat runs every 500ms.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoUserLockTest {
    private val userId = "lockedUser"

    @BeforeAll
    fun start() {
        DynamoLocal.start()
    }

    @BeforeEach
    fun cleanup() {
        runBlocking { DynamoUserLock.deleteLock(userId) }
    }

    private fun currentLock() = DynamoStore.getItem(DynamoStore.runtimeTable, "LOCK#$userId", "L")

    @Test
    fun `GIVEN a free lock WHEN it is taken THEN it is not free until released`() {
        runBlocking {
            assertTrue(DynamoUserLock.tryLock(userId))
            assertFalse(DynamoUserLock.tryLock(userId))
            DynamoUserLock.releaseLock(userId)
            assertTrue(DynamoUserLock.tryLock(userId))
        }
    }

    @Test
    fun `GIVEN a taken lock WHEN its lease has expired THEN it can be taken`() {
        runBlocking {
            assertTrue(DynamoUserLock.tryLock(userId))
            delay(1100.milliseconds)
            assertTrue(DynamoUserLock.tryLock(userId))
        }
    }

    @Test
    fun `GIVEN default renewal WHEN op runs longer than the lock timeout THEN the lock is kept and released at the end`() {
        runBlocking {
            val result =
                async {
                    DynamoUserLock.withLock(userId) {
                        delay(1600.milliseconds)
                        "done"
                    }
                }
            delay(1200.milliseconds)
            assertFalse(DynamoUserLock.tryLock(userId), "the lease should have been renewed")
            assertEquals("done", result.await())
            assertTrue(DynamoUserLock.tryLock(userId), "the lock should have been released")
        }
    }

    @Test
    fun `GIVEN no renewal and abortOnLockLoss WHEN the lock is taken over THEN op is aborted and the new owner keeps the lock`() {
        runBlocking {
            val result =
                async {
                    runCatching {
                        DynamoUserLock.withLock(userId, abortOnLockLoss = true, maxLeaseRenewal = Duration.ZERO) {
                            delay(20.seconds)
                        }
                    }
                }
            delay(1200.milliseconds)
            assertTrue(DynamoUserLock.tryLock(userId))

            assertFailsWith<LockLostException> { result.await().getOrThrow() }
            val lock = assertNotNull(currentLock())
            assertEquals(true, lock["locked"]?.bool())
            assertNull(lock["lockId"])
        }
    }

    @Test
    fun `GIVEN no renewal and no abortOnLockLoss WHEN the lock is taken over THEN op completes without releasing the new owner lock`() {
        runBlocking {
            val result =
                async {
                    DynamoUserLock.withLock(userId, abortOnLockLoss = false, maxLeaseRenewal = Duration.ZERO) {
                        delay(1600.milliseconds)
                        "done"
                    }
                }
            delay(1200.milliseconds)
            assertTrue(DynamoUserLock.tryLock(userId))

            assertEquals("done", result.await())
            assertFalse(DynamoUserLock.tryLock(userId), "the lock of the new owner should not have been released")
        }
    }

    @Test
    fun `GIVEN a running op WHEN the caller is cancelled THEN the lock is released immediately`() {
        runBlocking {
            var released = false
            val job =
                launch {
                    DynamoUserLock.withLock(userId, postLockRelease = { released = true }) {
                        delay(20.seconds)
                    }
                }
            delay(300.milliseconds)
            job.cancelAndJoin()

            assertTrue(released)
            assertTrue(DynamoUserLock.tryLock(userId), "the lock should have been released")
        }
    }

    @Test
    fun `GIVEN concurrent operations on a user WHEN they run with the lock THEN they never overlap`() {
        runBlocking {
            val running = AtomicInteger()
            val maxRunning = AtomicInteger()
            (1..4)
                .map {
                    async {
                        DynamoUserLock.withLock(userId) {
                            maxRunning.accumulateAndGet(running.incrementAndGet(), ::maxOf)
                            delay(100.milliseconds)
                            running.decrementAndGet()
                        }
                    }
                }.awaitAll()
            assertEquals(1, maxRunning.get())
        }
    }

    @Test
    fun `GIVEN a taken lock WHEN the user is removed with its lock THEN the lock is free`() {
        runBlocking {
            assertTrue(DynamoUserLock.tryLock(userId))
            UserTimelineDynamoDAO.remove("app", PlayerId(userId), clearLock = true)
            assertNull(currentLock())
            assertTrue(DynamoUserLock.tryLock(userId))
        }
    }
}
