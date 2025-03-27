/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import java.util.concurrent.Executor

/**
 * Manage async tasks.
 */
interface Executor : Executor {

    /**
     * Schedules a blocking task for execution on another thread.
     *
     * This method returns immediately, without waiting for the task to finish.
     *
     * @param delay the time from now to delay execution
     * @param runnable the task to run
     */
    fun executeBlocking(delay: Duration, runnable: () -> Unit)

    /**
     * Schedules a blocking task for execution on another thread.
     *
     * This method returns immediately, without waiting for the task to finish.
     *
     * The returned future will schedule any followup async task on this executor.
     *
     * @param delay the time from now to delay execution
     * @param task the task to run
     * @return a [CompletableFuture] that is asynchronously completed with the given [task]
     */
    fun <T> executeBlockingTask(delay: Duration = Duration.ZERO, task: () -> T): CompletableFuture<T>

    /**
     * Schedules a blocking task for execution on another thread.
     *
     * This method returns immediately, without waiting for the task to finish.
     *
     * @param runnable the task to run
     */
    fun executeBlocking(runnable: () -> Unit)

    /**
     * Execute a task to another thread.
     * If an exception is thrown by the blocking function, null is passed to the result function.
     *
     * This method returns immediately, without waiting for the task to finish.
     *
     * @param blocking the task to run
     * @param result the result handler
     *
     */
    fun <T> executeBlocking(blocking: Callable<T>, result: (T?) -> Unit)

    /**
     * Schedules a periodic task.
     *
     * This method returns immediately, without waiting for the task to execute.
     *
     * @param delay the delay before the first call, and between each following call
     * @param runnable the task to run
     */
    fun setPeriodic(delay: Duration, runnable: () -> Unit): Long = setPeriodic(delay, delay, runnable)

    /**
     * Schedules a periodic task.
     *
     * This method returns immediately, without waiting for the task to execute.
     *
     * @param initialDelay the delay before first call
     * @param delay the delay between each other call
     * @param runnable the task to run
     */
    fun setPeriodic(initialDelay: Duration, delay: Duration, runnable: () -> Unit): Long

    override fun execute(command: Runnable) {
        executeBlocking { command.run() }
    }

    /**
     * Returns an incomplete [CompletableFuture] which uses this executor for async methods that do not specify
     * another executor
     */
    fun <T> newIncompleteFuture(): CompletableFuture<T> = ExecutableFuture(this)

    open class ExecutableFuture<T>(private val executor: Executor) : CompletableFuture<T>() {
        override fun <U : Any?> newIncompleteFuture(): CompletableFuture<U> {
            return ExecutableFuture(executor)
        }

        override fun defaultExecutor(): Executor {
            return executor
        }
    }
}
