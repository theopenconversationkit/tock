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
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
        assertTrue { Duration.between(timestamp2, timestamp1) >= Duration.ofSeconds(1) }
        // 3 tasks should have been executed: launchCoroutines, launch, and the instructions after delay
        verify(exactly = 3) { executor.execute(any()) }
        for (threadName in threadNames) {
            assertEquals(
                threadPoolName,
                threadName.take(threadPoolName.length),
                "Expected thread $threadName to be part of test threadpool"
            )
        }
    }
}
