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

package ai.tock.shared

import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals

class ExecutorTest {
    private val scheduled = mutableListOf<Pair<Duration, Duration>>()

    private val executor =
        object : Executor {
            override fun executeBlocking(
                delay: Duration,
                runnable: () -> Unit,
            ) = runnable()

            override fun <T> executeBlockingTask(
                delay: Duration,
                task: () -> T,
            ): CompletableFuture<T> = CompletableFuture.completedFuture(task())

            override fun executeBlocking(runnable: () -> Unit) = runnable()

            override fun <T> executeBlocking(
                blocking: Callable<T>,
                result: (T?) -> Unit,
            ) = result(blocking.call())

            override fun setPeriodic(
                initialDelay: Duration,
                delay: Duration,
                runnable: () -> Unit,
            ): Long {
                scheduled += initialDelay to delay
                return 1
            }
        }

    @Test
    fun `GIVEN an executor that does not know the names WHEN a named task is scheduled THEN it is scheduled as an unnamed one`() {
        val id = executor.setPeriodic("model_build", Duration.ofSeconds(1), Duration.ofMinutes(1)) {}

        assertEquals(1, id)
        assertEquals(listOf(Duration.ofSeconds(1) to Duration.ofMinutes(1)), scheduled)
    }
}
