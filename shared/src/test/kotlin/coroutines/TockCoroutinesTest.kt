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

package ai.tock.shared.coroutines

import ai.tock.shared.SimpleExecutor
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Duration.ofSeconds
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTockCoroutines::class)
class TockCoroutinesTest {
    private val threadPoolName = "tock-coroutines-test-pool"
    private val executor = spyk(SimpleExecutor(10, threadPoolName))

    @Test
    fun `correctly dispatch coroutine jobs via executor`() {
        var timestamp1: Instant? = null
        var timestamp2: Instant? = null
        val threadNames = mutableListOf<String>()
        runBlocking {
            executor.launchCoroutine {
                threadNames += Thread.currentThread().name
                launch {
                    threadNames += Thread.currentThread().name
                    delay(1000)
                    threadNames += Thread.currentThread().name
                    timestamp1 = Instant.now()
                }
                timestamp2 = Instant.now()
            }.join()
        }
        assertNotNull(timestamp1)
        assertNotNull(timestamp2)
        assertTrue { Duration.between(timestamp2, timestamp1) >= ofSeconds(1) }
        // 3 tasks should have been executed: launchCoroutine, launch, and the instructions after delay
        verify(exactly = 3) { executor.execute(any()) }
        for (threadName in threadNames) {
            assertEquals(
                threadPoolName,
                threadName.take(threadPoolName.length),
                "Expected thread $threadName to be part of test threadpool",
            )
        }
    }

    @Test
    fun `fireAndForget does not wait to finish the task`(): Unit =
        runBlocking {
            var finished = false
            val time = Instant.now()
            fireAndForget {
                delay(1000)
                finished = true
            }
            assertTrue { Instant.now() < time + ofSeconds(1) }
            delay(1500)
            assertTrue { finished }
        }

    @Test
    fun `fireAndForgetIO does not wait to finish the task`(): Unit =
        runBlocking {
            var finished = false
            val time = Instant.now()
            fireAndForgetIO {
                delay(1000)
                finished = true
            }
            assertTrue { Instant.now() < time + ofSeconds(1) }
            delay(1500)
            assertTrue { finished }
        }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `waitForIO does not block the caller thread`() {
        val ticks = AtomicInteger(0)
        val result =
            waitForIOCorouting(ticks) { f ->
                waitForIO {
                    f()
                }
            }
        assertTrue(
            "Caller dispatcher made no progress while IO was running (ticks=${ticks.get()})",
            ticks.get() >= 1,
        )
        assertEquals("OK", result)
    }

    @Test
    fun `waitForCoroutineIO does block the caller thread`() {
        val ticks = AtomicInteger(0)
        val result =
            waitForIOCorouting(ticks) { f ->
                waitForCoroutineIO {
                    f()
                }
            }
        assertTrue(
            "Caller dispatcher made progress while IO was running (ticks=${ticks.get()})",
            ticks.get() == 0,
        )
        assertEquals("OK", result)
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun waitForIOCorouting(
        ticks: AtomicInteger,
        f: suspend (suspend () -> String) -> String,
    ): String =
        runBlocking {
            // Single-thread dispatcher to make the test deterministic: if blocked, nothing else runs.
            val callerDispatcher = newSingleThreadContext("caller-thread")
            try {
                withContext(callerDispatcher) {
                    // Signal completed inside the IO block once it has started.
                    val ioStarted = CompletableDeferred<Unit>()
                    // Signal to stop the ticker after IO completes.
                    val stop = CompletableDeferred<Unit>()

                    // Launch code BEFORE the function call; it starts working only when ioStarted is completed.
                    val tickerJob =
                        launch {
                            // Wait until the IO block signals it has started (and is sleeping on Dispatchers.IO).
                            ioStarted.await()
                            // While IO is in progress, we keep making progress on the caller dispatcher.
                            while (isActive && !stop.isCompleted) {
                                ticks.incrementAndGet()
                                // Suspend briefly to let the event loop schedule fairly.
                                delay(10)
                            }
                        }

                    // Call the function under test. This coroutine will suspend and hop to Dispatchers.IO.
                    val result =
                        f {
                            // Signal that we've switched to IO and are about to simulate blocking work.
                            ioStarted.complete(Unit)
                            // Simulate blocking I/O (e.g., JDBC, network, file).
                            Thread.sleep(200)
                            "OK"
                        }

                    // IO is done; stop the ticker and wait for it to finish.
                    stop.complete(Unit)
                    tickerJob.cancelAndJoin()

                    return@withContext result
                }
            } finally {
                callerDispatcher.close()
            }
        }
}
