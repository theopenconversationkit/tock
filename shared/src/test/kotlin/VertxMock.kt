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

import io.mockk.mockk
import io.vertx.codegen.annotations.Nullable
import io.vertx.core.Context
import io.vertx.core.Deployable
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Timer
import io.vertx.core.Vertx
import io.vertx.core.WorkerExecutor
import io.vertx.core.datagram.DatagramSocket
import io.vertx.core.datagram.DatagramSocketOptions
import io.vertx.core.dns.DnsClient
import io.vertx.core.dns.DnsClientOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.file.FileSystem
import io.vertx.core.http.HttpClientAgent
import io.vertx.core.http.HttpClientBuilder
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.http.PoolOptions
import io.vertx.core.http.WebSocketClient
import io.vertx.core.http.WebSocketClientOptions
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetClientOptions
import io.vertx.core.net.NetServer
import io.vertx.core.net.NetServerOptions
import io.vertx.core.shareddata.SharedData
import io.vertx.core.spi.VerticleFactory
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

/**
 * A mock for [Vertx] interface used because mockk & Vertx do not play well together.
 */
class VertxMock : Vertx {
    override fun verticleFactories(): MutableSet<VerticleFactory> = mutableSetOf()

    override fun setPeriodic(
        delay: Long,
        handler: Handler<Long>?,
    ): Long = delay

    override fun getOrCreateContext(): Context = mockk()

    override fun createHttpServer(options: HttpServerOptions?): HttpServer = mockk()

    override fun createHttpServer(): HttpServer = mockk()

    override fun fileSystem(): FileSystem = mockk()

    override fun createDnsClient(
        port: Int,
        host: String?,
    ): DnsClient = mockk()

    override fun createDnsClient(): DnsClient = mockk()

    override fun createDnsClient(options: DnsClientOptions?): DnsClient = mockk()

    override fun cancelTimer(id: Long): Boolean = true

    override fun sharedData(): SharedData = mockk()

    override fun createSharedWorkerExecutor(name: String?): WorkerExecutor = mockk()

    override fun createSharedWorkerExecutor(
        name: String?,
        poolSize: Int,
    ): WorkerExecutor = mockk()

    override fun createSharedWorkerExecutor(
        name: String?,
        poolSize: Int,
        maxExecuteTime: Long,
    ): WorkerExecutor = mockk()

    override fun createSharedWorkerExecutor(
        name: String?,
        poolSize: Int,
        maxExecuteTime: Long,
        maxExecuteTimeUnit: TimeUnit?,
    ): WorkerExecutor = mockk()

    override fun isNativeTransportEnabled(): Boolean = false

    override fun setTimer(
        delay: Long,
        handler: Handler<Long>?,
    ): Long = delay

    override fun deploymentIDs(): MutableSet<String> = mutableSetOf()

    override fun registerVerticleFactory(factory: VerticleFactory?) {
    }

    override fun createDatagramSocket(options: DatagramSocketOptions?): DatagramSocket = mockk()

    override fun createDatagramSocket(): DatagramSocket = mockk()

    override fun isClustered(): Boolean = false

    override fun eventBus(): EventBus = mockk()

    override fun runOnContext(action: Handler<Void>?) {
    }

    override fun unregisterVerticleFactory(factory: VerticleFactory?) {
    }

    override fun exceptionHandler(handler: Handler<Throwable>?): Vertx = this

    override fun exceptionHandler(): Handler<Throwable> = mockk()

    override fun createNetServer(options: NetServerOptions?): NetServer = mockk()

    override fun createNetServer(): NetServer = mockk()

    override fun createNetClient(options: NetClientOptions?): NetClient = mockk()

    override fun createNetClient(): NetClient = mockk()

    override fun setPeriodic(
        initialDelay: Long,
        delay: Long,
        handler: Handler<Long>?,
    ): Long = 0

    override fun deployVerticle(
        name: String?,
        options: DeploymentOptions?,
    ): Future<String> = mockk()

    override fun close(): Future<Void> = mockk()

    override fun undeploy(deploymentID: String?): Future<Void> = mockk()

    override fun unavailableNativeTransportCause(): Throwable = mockk()

    override fun createWebSocketClient(options: WebSocketClientOptions): WebSocketClient = mockk()

    override fun deployVerticle(name: String?): Future<String> = mockk()

    override fun <T : Any?> executeBlocking(blockingCodeHandler: Callable<T>?): Future<T> = mockk()

    override fun createWebSocketClient(): WebSocketClient = mockk()

    override fun httpClientBuilder(): HttpClientBuilder = mockk()

    override fun timer(delay: Long): Timer = mockk()

    override fun timer(
        delay: Long,
        unit: TimeUnit,
    ): Timer = mockk()

    override fun deployVerticle(verticle: Deployable?): Future<String?> = mockk()

    override fun deployVerticle(
        verticle: Deployable?,
        options: DeploymentOptions?,
    ): Future<String?> = mockk()

    override fun deployVerticle(
        supplier: Supplier<out Deployable?>?,
        options: DeploymentOptions?,
    ): Future<String?>? {
        TODO("Not yet implemented")
    }

    override fun deployVerticle(
        verticleClass: Class<out Deployable?>?,
        options: DeploymentOptions?,
    ): Future<String?>? {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> executeBlocking(
        blockingCodeHandler: Callable<T?>?,
        ordered: Boolean,
    ): Future<@Nullable T?>? {
        return super.executeBlocking(blockingCodeHandler, ordered)
    }

    override fun createHttpClient(
        clientOptions: HttpClientOptions?,
        poolOptions: PoolOptions?,
    ): HttpClientAgent? {
        return super.createHttpClient(clientOptions, poolOptions)
    }

    override fun createHttpClient(clientOptions: HttpClientOptions?): HttpClientAgent? {
        return super.createHttpClient(clientOptions)
    }

    override fun createHttpClient(poolOptions: PoolOptions?): HttpClientAgent? {
        return super.createHttpClient(poolOptions)
    }

    override fun createHttpClient(): HttpClientAgent? {
        return super.createHttpClient()
    }
}
