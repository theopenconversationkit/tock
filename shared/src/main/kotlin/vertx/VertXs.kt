/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.shared.vertx

import ai.tock.shared.Executor
import ai.tock.shared.debug
import ai.tock.shared.devEnvironment
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.provideOrDefault
import io.vertx.core.AsyncResult
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.slf4j.MDCContextMap
import mu.KotlinLogging
import mu.withLoggingContext
import java.time.Duration
import java.util.concurrent.Callable

private val logger = KotlinLogging.logger {}

/**
 * default vert.x options in Tock.
 */
var defaultVertxOptions = VertxOptions().apply {
    maxWorkerExecuteTime = 1000 * 60L * 1000 * 1000000
    if (devEnvironment) {
        warningExceptionTime = 1000L * 1000 * 1000000
    }
}

internal interface VertxProvider {

    fun vertx(): Vertx
}

internal object TockVertxProvider : VertxProvider {

    override fun vertx(): Vertx = Vertx.vertx(defaultVertxOptions)
}

private val internalVertx: Vertx by lazy {
    injector.provideOrDefault<VertxProvider> { TockVertxProvider }.vertx()
}

//used to avoid name collisions
internal val sharedVertx: Vertx get() = vertx

/**
 * The Tock [Vertx] entry point instance.
 */
val vertx: Vertx get() = internalVertx

/**
 * Execute a blocking task (with ordered false).
 */
fun <T> Vertx.blocking(blockingHandler: (Promise<T>) -> Unit, resultHandler: (AsyncResult<T>) -> Unit) {
    this.executeBlocking(
        { future: Promise<T> ->
            try {
                blockingHandler.invoke(future)
            } catch (throwable: Throwable) {
                logger.error(throwable) { throwable.message }
                future.fail(throwable)
            }
        },
        false,
        {
            try {
                resultHandler.invoke(it)
            } catch (e: Throwable) {
                logger.error(e) { e.message }
            }
        })
}

/**
 * Execute a blocking handler on route (with ordered false).
 */
fun Route.blocking(handler: (RoutingContext) -> Unit): Route =
    blockingHandler({
        try {
            handler(it)
        } catch (t: Throwable) {
            try {
                logger.error(t)
                it.fail(t)
            } catch (e: Throwable) {
                logger.debug(e)
            }
        }
    }, false)

internal fun vertxExecutor(): Executor {
    return object : Executor {

        override fun executeBlocking(delay: Duration, runnable: () -> Unit) {
            if (delay.isZero) {
                executeBlocking(runnable)
            } else {
                val loggingContext = MDCContext().contextMap
                vertx.setTimer(delay.toMillis()) {
                    invokeWithLoggingContext(loggingContext) {
                        executeBlocking(runnable)
                    }
                }
            }
        }

        override fun executeBlocking(runnable: () -> Unit) {
            val loggingContext = MDCContext().contextMap
            vertx.blocking<Unit>({
                invokeWithLoggingContext(loggingContext) {
                    invoke(runnable)
                    it.complete()
                }
            }, {})
        }

        override fun <T> executeBlocking(blocking: Callable<T>, result: (T?) -> Unit) {
            val loggingContext = MDCContext().contextMap
            vertx.blocking<T>({
                invokeWithLoggingContext(loggingContext) {
                    blocking.call()
                }
            }, {
                if (it.succeeded()) {
                    result.invoke(it.result())
                } else {
                    result.invoke(null)
                }
            })
        }

        override fun setPeriodic(initialDelay: Duration, delay: Duration, runnable: () -> Unit): Long {
            val loggingContext = MDCContext().contextMap
            return vertx.setTimer(initialDelay.toMillis()) {
                invokeWithLoggingContext(loggingContext) {
                    executeBlocking(runnable)
                    vertx.setPeriodic(delay.toMillis()) {
                        invokeWithLoggingContext(loggingContext) {
                            executeBlocking(runnable)
                        }
                    }
                }
            }
        }

        private fun invoke(runnable: () -> Unit) {
            try {
                runnable.invoke()
            } catch (throwable: Throwable) {
                logger.error(throwable)
            }
        }

        private fun invokeWithLoggingContext(loggingContext: MDCContextMap, runnable: () -> Unit) {
            runnable.let {
                loggingContext?.let { map ->
                    withLoggingContext(map, it)
                }?: invoke(it)
            }
        }
    }
}



