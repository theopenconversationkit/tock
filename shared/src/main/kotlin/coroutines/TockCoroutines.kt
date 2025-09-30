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
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogger
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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
    block: suspend CoroutineScope.() -> Unit
): Job {
    return CoroutineScope(asCoroutineDispatcher()).launch(context, start, block)
}

/**
 * fire and forget a suspend block in the [Dispatchers.IO] scope.
 * Exceptions are logged.
 */
fun fireAndForgetIO(block: suspend () -> Unit) =
    CoroutineScope(Dispatchers.IO).launch {
        try {
            block()
        } catch (e: Exception) {
            val logger: KLogger = KotlinLogging.logger { }
            logger.error("Uncaught exception in a fire-and-forget-blocking-io coroutine", e)
        }
    }

/**
 * fire and forget a suspend block in the current coroutine scope.
 * Exceptions are logged.
 */
fun fireAndForget(block: suspend () -> Unit) =
    CoroutineScope(EmptyCoroutineContext).launch {
        try {
            block()
        } catch (e: Exception) {
            val logger: KLogger = KotlinLogging.logger { }
            logger.error("Uncaught exception in a fire-and-forget-blocking-io coroutine", e)
        }
    }

/**
 * Starts a coroutine and waits for the result. [Dispatchers.IO] is used.
 */
fun <T> waitForCoroutineIO(block: suspend CoroutineScope.() -> T): T = runBlocking(context = Dispatchers.IO, block = block)

