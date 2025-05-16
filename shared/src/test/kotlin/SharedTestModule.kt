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

import ai.tock.shared.cache.TockCache
import ai.tock.shared.security.NoOpTockUserListener
import ai.tock.shared.security.TockUserListener
import ai.tock.shared.security.mongo.DefaultMongoCredentialsProvider
import ai.tock.shared.security.mongo.MongoCredentialsProvider
import ai.tock.shared.vertx.VertxProvider
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.github.salomonbrys.kodein.singleton
import com.mongodb.client.MongoClient
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.vertx.core.Vertx
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.KFlapdoodle
import org.litote.kmongo.reactivestreams.KFlapdoodleReactiveStreams
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS

private val logger = KotlinLogging.logger {}

/**
 * Mocked vertx.
 */
val mockedVertx: Vertx by lazy { mockk<Vertx>(relaxed = true) }

/**
 * Shared test module to be imported by tests using Ioc.
 */
val sharedTestModule = Kodein.Module {
    bind<Executor>() with provider { TestExecutor }
    bind<TockCache>() with provider { NoOpCache }
    bind<TockUserListener>() with provider { NoOpTockUserListener }
    bind<MongoCredentialsProvider>() with provider { DefaultMongoCredentialsProvider }

    try {
        clearMocks(mockedVertx)
        bind<VertxProvider>() with singleton {
            mockk<VertxProvider>() {
                every { vertx() } returns mockedVertx
            }
        }
    } catch (e: Throwable) {
        // vertx not in classpath : ignore
        logger.trace("vertx is not present in classpath")
    }

    try {
        bind<MongoClient>() with singleton {
            try {
                // init kmongo configuration for persistence tests
                TockKMongoConfiguration.configure()
                KFlapdoodle.mongoClient
            } catch (t: Throwable) {
                logger.trace("error during KMongo configuration", t)
                mockk<MongoClient>(relaxed = true)
            }
        }
    } catch (t: Throwable) {
        logger.trace("sync mongo driver is not present in classpath")
    }
    try {
        bind<com.mongodb.reactivestreams.client.MongoClient>() with singleton {
            try {
                // init kmongo configuration for persistence tests
                TockKMongoConfiguration.configure(true)
                KFlapdoodleReactiveStreams.mongoClient
            } catch (t: Throwable) {
                logger.trace("error during KMongo configuration", t)
                mockk<com.mongodb.reactivestreams.client.MongoClient>(relaxed = true)
            }
        }
    } catch (t: Throwable) {
        logger.trace("async mongo driver is not present in classpath")
    }
}

private object NoOpCache : TockCache {
    private val map: MutableMap<Pair<Id<*>, String>, Any> = mutableMapOf()

    override fun <T> get(id: Id<T>, type: String): T? {
        @Suppress("UNCHECKED_CAST")
        return map[id to type] as T?
    }

    override fun <T : Any> put(id: Id<T>, type: String, data: T) {
        map[id to type] = data
    }

    override fun <T> getAll(type: String): Map<Id<T>, Any> {
        return map
            .entries
            .filter { it.key.second == type }
            .map {
                @Suppress("UNCHECKED_CAST")
                it.key.first as Id<T> to it.value
            }
            .toMap()
    }

    override fun <T> remove(id: Id<T>, type: String) {
        map - id to type
    }
}

/**
 * A simple executor that uses [nbThreads] - useful for concurrency tests.
 */
class SimpleExecutor(private val nbThreads: Int) : Executor {

    private val executor = Executors.newScheduledThreadPool(nbThreads)

    override fun executeBlocking(delay: Duration, runnable: () -> Unit) {
        executor.schedule(runnable, delay.toMillis(), MILLISECONDS)
    }

    override fun <T> executeBlockingTask(delay: Duration, task: () -> T): CompletableFuture<T> {
        return newIncompleteFuture<T>().apply {
            executor.schedule({
                complete(task())
            }, delay.toMillis(), MILLISECONDS)
        }
    }

    override fun executeBlocking(runnable: () -> Unit) {
        executor.schedule(runnable, 0L, MILLISECONDS)
    }

    override fun <T> executeBlocking(blocking: Callable<T>, result: (T?) -> Unit) {
        val future = executor.schedule(blocking, 0, MILLISECONDS)
        future.get().apply { result(this) }
    }

    override fun setPeriodic(initialDelay: Duration, delay: Duration, runnable: () -> Unit): Long {
        executor.scheduleWithFixedDelay(runnable, initialDelay.toMillis(), delay.toMillis(), MILLISECONDS)
        return 0L
    }
}

private object TestExecutor : Executor {

    override fun executeBlocking(delay: Duration, runnable: () -> Unit) {
        runnable.invoke()
    }

    override fun <T> executeBlockingTask(delay: Duration, task: () -> T): CompletableFuture<T> = CompletableFuture.completedFuture(
        task.invoke()
    )

    override fun executeBlocking(runnable: () -> Unit) {
        runnable.invoke()
    }

    override fun <T> executeBlocking(blocking: Callable<T>, result: (T?) -> Unit) {
        result.invoke(blocking.call())
    }

    override fun setPeriodic(initialDelay: Duration, delay: Duration, runnable: () -> Unit): Long {
        Executors.newCachedThreadPool().submit(runnable)
        return 0L
    }
}
