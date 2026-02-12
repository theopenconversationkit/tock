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

package ai.tock.shared.vertx

import ai.tock.shared.Executor
import ai.tock.shared.debug
import ai.tock.shared.devEnvironment
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.intProperty
import ai.tock.shared.longProperty
import ai.tock.shared.provideOrDefault
import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.VertxOptions.DEFAULT_WORKER_POOL_SIZE
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.slf4j.MDCContextMap
import mu.KotlinLogging
import mu.withLoggingContext
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger {}

/**
 * default vert.x options in Tock.
 */
var defaultVertxOptions =
    VertxOptions().apply {
        maxWorkerExecuteTime = 1000 * 60L * 1000 * 1000000
        workerPoolSize = intProperty("tock_vertx_worker_pool_size", DEFAULT_WORKER_POOL_SIZE)
        if (devEnvironment) {
            warningExceptionTime = 1000L * 1000 * 1000000
        }
    }

private val sseKeepaliveDelay = longProperty("tock_web_sse_keepalive_delay", 10)

internal interface VertxProvider {
    fun vertx(): Vertx
}

internal object TockVertxProvider : VertxProvider {
    override fun vertx(): Vertx =
        Vertx.vertx(defaultVertxOptions).apply {
            exceptionHandler { t ->
                logger.error(t)
            }
        }
}

private val internalVertx: Vertx by lazy {
    injector.provideOrDefault<VertxProvider> { TockVertxProvider }.vertx()
}

// used to avoid name collisions
internal val sharedVertx: Vertx get() = vertx

/**
 * The Tock [Vertx] entry point instance.
 */
val vertx: Vertx get() = internalVertx

/**
 * Execute a blocking task (with ordered false).
 */
fun <T> Vertx.blocking(
    blockingHandler: (Promise<T>) -> Unit,
    resultHandler: (AsyncResult<T>) -> Unit,
) {
    val p: Promise<T> = Promise.promise()
    var res: T? = null
    var err: Throwable? = null
    p.future().onComplete { ar ->
        if (ar.succeeded()) {
            res = ar.result()
        } else {
            err = ar.cause()
        }
    }
    this.executeBlocking<T>(
        {
            try {
                blockingHandler.invoke(p)
            } catch (throwable: Throwable) {
                logger.error(throwable) { throwable.message }
                p.fail(throwable)
            } finally {
                p.tryFail("call not completed")
            }
            err?.let { throw it }
            res
        },
        false,
    ).onComplete { ar ->
        try {
            resultHandler.invoke(ar)
        } catch (e: Throwable) {
            logger.error(e) { e.message }
        }
    }
}

/**
 * Execute a blocking handler on route (with ordered = false).
 */
fun Route.blocking(handler: (RoutingContext) -> Unit): Route =
    blockingHandler({ rc ->
        try {
            handler(rc)
        } catch (t: Throwable) {
            try {
                logger.error(t)
                rc.fail(t)
            } catch (e: Throwable) {
                logger.debug(e)
            }
        }
    }, false)

private val VERTX_MIN_DELAY = Duration.ofMillis(1)

internal fun vertxExecutor(): Executor {
    return object : Executor {
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
            val future = newIncompleteFuture<T>()
            if (delay < VERTX_MIN_DELAY) {
                future.completeAsync(task)
            } else {
                val loggingContext = MDCContext().contextMap
                vertx.setTimer(delay.toMillis()) {
                    invokeWithLoggingContext(loggingContext) {
                        executeBlocking(task) { future.complete(it) }
                    }
                }
            }
            return future
        }

        override fun executeBlocking(runnable: () -> Unit) {
            val loggingContext = MDCContext().contextMap
            vertx.blocking<Unit>(
                { promise ->
                    invokeWithLoggingContext(loggingContext) {
                        catchableRunnable(runnable).invoke()
                        promise.tryComplete()
                    }
                },
                { _: AsyncResult<Unit> -> },
            )
        }

        override fun <T> executeBlocking(
            blocking: Callable<T>,
            result: (T?) -> Unit,
        ) {
            val loggingContext = MDCContext().contextMap
            vertx.blocking<T>(
                { promise ->
                    invokeWithLoggingContext(loggingContext) {
                        try {
                            val v = blocking.call()
                            promise.complete(v)
                        } catch (e: Throwable) {
                            logger.error(e)
                            promise.fail(e)
                        } finally {
                            promise.tryFail("call not completed")
                        }
                    }
                },
                { ar: AsyncResult<T> ->
                    if (ar.succeeded()) {
                        result.invoke(ar.result())
                    } else {
                        result.invoke(null)
                    }
                },
            )
        }

        override fun setPeriodic(
            initialDelay: Duration,
            delay: Duration,
            runnable: () -> Unit,
        ): Long {
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

        private fun catchableRunnable(runnable: () -> Unit): () -> Unit =
            {
                try {
                    runnable.invoke()
                } catch (throwable: Throwable) {
                    logger.error(throwable)
                }
            }

        private fun invokeWithLoggingContext(
            loggingContext: MDCContextMap,
            runnable: () -> Unit,
        ) {
            val r = catchableRunnable(runnable)
            loggingContext.let { context ->
                if (context == null) {
                    r.invoke()
                } else {
                    withLoggingContext(context, body = r)
                }
            }
        }
    }
}

fun HttpServerResponse.setupSSE(
    addEndHandler: Boolean = false,
    keepAlive: Boolean = true,
    closeHandler: () -> Unit = {},
): CompositeFuture {
    isChunked = true
    headers().apply {
        add("Content-Type", "text/event-stream;charset=UTF-8")
        add("Connection", "keep-alive")
        add("Cache-Control", "no-cache")
        add("X-Accel-Buffering", "no")
    }
    val timerId =
        if (keepAlive) {
            vertx.setPeriodic(Duration.ofSeconds(sseKeepaliveDelay).toMillis()) {
                sendSsePing()
            }
        } else {
            null
        }
    if (addEndHandler) {
        endHandler {
            closeHandler()
            if (timerId != null) {
                vertx.cancelTimer(timerId)
            }
        }
    }
    closeHandler {
        closeHandler()
        if (timerId != null) {
            vertx.cancelTimer(timerId)
        }
    }
    return sendSsePing()
}

fun HttpServerResponse.sendSsePing(): CompositeFuture =
    Future.all(
        write("event: ping\n"),
        write("data: 1\n\n"),
    )

fun HttpServerResponse.sendSseMessage(data: String): CompositeFuture =
    Future.all(
        write("event: message\n"),
        write("data: $data\n\n"),
    )
