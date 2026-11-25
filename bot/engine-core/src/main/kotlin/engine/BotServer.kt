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

package ai.tock.bot.engine

import ai.tock.shared.InternalTockApi
import ai.tock.shared.coroutines.ExecutionScope
import ai.tock.shared.http.HttpRoutes
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineRouterSupport
import java.util.ServiceLoader

/**
 * The server that exposes the bots: it installs the services of the connectors, and runs the processing of
 * the events they receive.
 *
 * tock-bot-engine provides the Vert.x implementation. The router types only appear in lambda parameters,
 * erased in the bytecode: an implementation without Vert.x never loads them.
 */
@InternalTockApi
interface BotServer {
    /**
     * Where the connector controllers launch the processing of the received events.
     */
    val executionScope: ExecutionScope

    /**
     * Registers services: they are installed by the next [configure] (or the deployment).
     */
    fun registerServices(
        serviceIdentifier: String,
        installer: CoroutineRouterSupport.(Router) -> Any?,
    ): ServiceRegistration

    /**
     * Registers the services of a connector: like [registerServices], with a `<serviceIdentifier>/healthcheck` route.
     */
    fun registerConnectorServices(
        serviceIdentifier: String,
        installer: CoroutineRouterSupport.(Router) -> Unit,
    ): ServiceRegistration

    /**
     * Registers the server independent routes of a connector, with a `<serviceIdentifier>/healthcheck` route.
     */
    fun registerConnectorRoutes(
        serviceIdentifier: String,
        routes: HttpRoutes,
    ): ServiceRegistration

    /**
     * Removes registered services.
     */
    fun unregisterServices(registration: ServiceRegistration)

    /**
     * Installs the services registered since the last call.
     */
    fun configure()

    /**
     * Starts the server, then calls [onComplete] with null if it succeeded, the failure cause otherwise.
     */
    fun deploy(onComplete: (Throwable?) -> Unit)
}

/**
 * A handle on registered services, to remove them.
 */
@InternalTockApi
interface ServiceRegistration

/**
 * Creates the [BotServer], found with the [ServiceLoader].
 */
@InternalTockApi
interface BotServerFactory {
    fun create(): BotServer

    companion object {
        /**
         * The server of the first factory found on the classpath.
         */
        fun load(): BotServer =
            ServiceLoader.load(BotServerFactory::class.java).firstOrNull()?.create()
                ?: error("No BotServer found: add tock-bot-engine to the classpath")
    }
}
