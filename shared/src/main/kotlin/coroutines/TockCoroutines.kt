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

import ai.tock.shared.Executor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KLogger
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


private val logger: KLogger = KotlinLogging.logger { }

/**
 * Launches a new coroutine without blocking the current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * The coroutine and its suspensions will be dispatched using this executor.
 *
 * @see CoroutineScope.launch
 */
@ExperimentalTockCoroutines
fun Executor.launchCoroutine(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
): Job {
    return CoroutineScope(asCoroutineDispatcher()).launch(context, start, block)
}

/**
 * fire and forget a suspend block on [Dispatchers.IO].
 * Exceptions are logged.
 *
 * !! Same pitfalls as [GlobalScope]
 */
fun fireAndForgetIO(block: suspend () -> Unit): Job =
    CoroutineScope(Dispatchers.IO).launch {
        try {
            block()
        } catch (e: Exception) {
            logger.error("Uncaught exception in a fire-and-forget-blocking-io coroutine", e)
        }
    }

/**
 * fire and forget a suspend block on the default dispatcher.
 * Exceptions are logged.
 *
 * !! Same pitfalls as [GlobalScope]
 */
fun fireAndForget(block: suspend () -> Unit): Job =
    CoroutineScope(EmptyCoroutineContext).launch {
        try {
            block()
        } catch (e: Exception) {
            logger.error("Uncaught exception in a fire-and-forget coroutine", e)
        }
    }

/**
 * Starts a coroutine and waits for the result. Current thread is blocked. [Dispatchers.IO] is used.
 * Useful if you are note in a coroutine context.
 */
fun <T> waitForCoroutineIO(block: suspend CoroutineScope.() -> T): T = runBlocking(context = Dispatchers.IO, block = block)

/**
 * Starts a coroutine and waits for the result. Current thread is not blocked. [Dispatchers.IO] is used.
 * Useful if you are in a coroutine context.
 */
suspend fun <T> waitForIO(block: suspend CoroutineScope.() -> T): T = withContext(context = Dispatchers.IO, block = block)
