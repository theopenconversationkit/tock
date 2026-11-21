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

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * The work started while a request is handled: the tasks of the [TrackingExecutor] and the coroutines of the
 * [TrackingExecutionScope]. A serverless process is frozen once the response is sent, so the request waits
 * for this work, including the work it starts itself, before answering.
 */
class RequestWork {
    private val pending = ConcurrentLinkedQueue<CompletableFuture<*>>()

    /** Registers [future] as work of this request. */
    fun track(future: CompletableFuture<*>) {
        pending += future
    }

    /**
     * Waits until all the tracked work is done, the work tracked while waiting included.
     *
     * @throws TimeoutException after [timeoutMs]
     */
    fun awaitIdle(timeoutMs: Long) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (true) {
            pending.removeIf { it.isDone }
            val running = pending.toList()
            if (running.isEmpty()) return
            val remaining = deadline - System.currentTimeMillis()
            if (remaining <= 0) throw TimeoutException("${running.size} tasks still running")
            try {
                CompletableFuture.allOf(*running.toTypedArray()).get(remaining, TimeUnit.MILLISECONDS)
            } catch (e: TimeoutException) {
                throw e
            } catch (_: Exception) {
                // a failed task is done: its error has been logged by the executor
            }
        }
    }

    companion object {
        /** The work of the request handled by the current thread, if any. */
        internal val current: ThreadLocal<RequestWork?> = ThreadLocal()

        /** Runs [block] as part of this request: the work it starts is tracked. */
        fun <T> RequestWork.runTracked(block: () -> T): T {
            val previous = current.get()
            current.set(this)
            try {
                return block()
            } finally {
                current.set(previous)
            }
        }
    }
}
