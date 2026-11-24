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
import ai.tock.shared.Executor
import mu.KotlinLogging
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * The [Executor] of a serverless process: the tasks run on a thread pool, and the tasks submitted while a
 * request is handled are tracked by its [RequestWork], so that the request waits for them.
 *
 * The periodic tasks are not scheduled, as a frozen process would never run them: they are registered in
 * [periodicTasks], and an external scheduler triggers them (see [ServerlessRequestHandler]).
 */
class TrackingExecutor(
    threads: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(4),
) : Executor {
    private val logger = KotlinLogging.logger {}
    private val threadCount = AtomicInteger()
    private val pool: ScheduledExecutorService =
        Executors.newScheduledThreadPool(threads) { runnable ->
            Thread(runnable, "tock-serverless-${threadCount.incrementAndGet()}").apply { isDaemon = true }
        }

    /**
     * The periodic tasks, by name. The unnamed ones are named after the class and the method that register them
     * (`translator_statscache`, `messengerconnector_registercheckwebhook`), with `_2`, `_3`... for the next ones of the same caller:
     * the name stays the same from one start to the next, so that an external scheduler can trigger it.
     */
    val periodicTasks: MutableMap<String, () -> Unit> = ConcurrentHashMap()

    override fun executeBlocking(
        delay: Duration,
        runnable: () -> Unit,
    ) {
        executeBlockingTask(delay, runnable)
    }

    override fun <T> executeBlockingTask(
        delay: Duration,
        task: () -> T,
    ): CompletableFuture<T> {
        val work = RequestWork.current.get()
        val future = newIncompleteFuture<T>()
        work?.track(future)
        pool.schedule(
            {
                try {
                    future.complete(if (work == null) task() else work.runTracked(task))
                } catch (t: Throwable) {
                    logger.error(t) { "task failed" }
                    future.completeExceptionally(t)
                }
            },
            delay.toMillis(),
            TimeUnit.MILLISECONDS,
        )
        return future
    }

    override fun executeBlocking(runnable: () -> Unit) {
        executeBlockingTask(Duration.ZERO, runnable)
    }

    override fun <T> executeBlocking(
        blocking: Callable<T>,
        result: (T?) -> Unit,
    ) {
        // as the Vert.x executor: null is passed to the result handler when the call fails
        executeBlockingTask { blocking.call() }.whenComplete { value, _ -> result(value) }
    }

    override fun setPeriodic(
        initialDelay: Duration,
        delay: Duration,
        runnable: () -> Unit,
    ): Long = setPeriodic(callerName(), initialDelay, delay, runnable)

    /** The name of an unnamed periodic task: its caller, outside of the executor. */
    private fun callerName(): String {
        val caller =
            StackWalker.getInstance().walk { frames ->
                frames
                    .filter { frame -> frame.className != TrackingExecutor::class.java.name && !frame.className.startsWith("ai.tock.shared.Executor") }
                    .findFirst()
                    .orElse(null)
            }
        val base =
            if (caller == null) {
                "periodic"
            } else {
                // Translator$statsCache$2.invoke: the lambda of a member (lower case) is named after the member
                val parts =
                    caller.className
                        .substringAfterLast('.')
                        .split('$')
                        .dropLastWhile { it.isEmpty() || it.all(Char::isDigit) }
                val last = parts.last()
                val (owner, member) =
                    if (parts.size > 1 && last.first().isLowerCase()) {
                        parts[parts.size - 2] to last
                    } else {
                        last to
                            caller.methodName
                                .removePrefix("<")
                                .removeSuffix(">")
                                .substringBefore('$')
                                // the lambda of a lazy property compiled by K2: <property>_delegate$lambda$0
                                .removeSuffix("_delegate")
                                .ifEmpty { "lambda" }
                    }
                "${owner}_$member".lowercase().replace(Regex("[^a-z0-9_]"), "_")
            }
        return generateSequence(1) { it + 1 }.map { if (it == 1) base else "${base}_$it" }.first { it !in periodicTasks }
    }

    override fun setPeriodic(
        name: String,
        initialDelay: Duration,
        delay: Duration,
        runnable: () -> Unit,
    ): Long {
        logger.info { "periodic task $name registered: an external scheduler must trigger it every $delay" }
        periodicTasks[name] = runnable
        return periodicTasks.size.toLong()
    }
}
