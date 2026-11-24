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

package ai.tock.serverless

import ai.tock.serverless.RequestWork.Companion.runTracked
import kotlinx.coroutines.delay
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RequestWorkTest {
    private val executor = TrackingExecutor(2)
    private val scope = TrackingExecutionScope()

    @Test
    fun `GIVEN work that starts more work, with delays WHEN the request waits THEN all of it is done`() {
        val done = CopyOnWriteArrayList<String>()
        val work = RequestWork()

        work.runTracked {
            executor.executeBlocking {
                scope.launch {
                    delay(100)
                    executor.executeBlocking(Duration.ofMillis(200)) { done += "delayed task" }
                    done += "coroutine"
                }
                done += "task"
            }
        }
        work.awaitIdle(5_000)

        assertEquals(setOf("task", "coroutine", "delayed task"), done.toSet())
    }

    @Test
    fun `GIVEN work started outside of a request WHEN a request waits THEN it does not wait for it`() {
        val work = RequestWork()
        executor.executeBlocking { Thread.sleep(2_000) }

        val start = System.currentTimeMillis()
        work.awaitIdle(5_000)

        assertTrue(System.currentTimeMillis() - start < 1_000)
    }

    @Test
    fun `GIVEN work longer than the timeout WHEN the request waits THEN it gives up`() {
        val work = RequestWork()
        work.runTracked { executor.executeBlocking { Thread.sleep(2_000) } }

        assertFailsWith<TimeoutException> { work.awaitIdle(100) }
    }

    @Test
    fun `GIVEN periodic tasks WHEN they are scheduled THEN they are registered by name instead of being run`() {
        var runs = 0
        executor.setPeriodic("i18n_stats", Duration.ofSeconds(1), Duration.ofSeconds(10)) { runs++ }
        executor.setPeriodic(Duration.ofSeconds(1)) { runs++ }

        Thread.sleep(1_500)

        assertEquals(0, runs)
        // the unnamed one is named after its caller: the same name at each start
        assertTrue(
            "i18n_stats" in executor.periodicTasks && "requestworktest_given_periodic_tasks_when_they_are_scheduled_then_they_are_registered_by_name_instead_of_being_run" in executor.periodicTasks,
            executor.periodicTasks.keys.toString(),
        )
    }

    private class LazyTask(
        executor: TrackingExecutor,
    ) {
        val cache: Int by lazy {
            executor.setPeriodic(Duration.ofSeconds(10)) { }
            0
        }
    }

    @Test
    fun `GIVEN unnamed periodic tasks registered by a lazy member WHEN they are registered twice THEN they are named after the member`() {
        val executor = TrackingExecutor(threads = 1)
        LazyTask(executor).cache
        LazyTask(executor).cache

        assertEquals(setOf("lazytask_cache", "lazytask_cache_2"), executor.periodicTasks.keys)
    }
}
