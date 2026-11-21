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

import ai.tock.bot.engine.BotServer
import ai.tock.bot.engine.BotServerFactory
import ai.tock.bot.engine.ServiceRegistration
import ai.tock.shared.coroutines.ExecutionScope
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineRouterSupport
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The [BotServer] of a serverless process: it collects the [HttpRoutes] of the connectors, and
 * [ServerlessRequestHandler] dispatches the requests to them.
 *
 * The Vert.x services are not supported: a connector that still registers a Vert.x router fails to start,
 * with a message that names it. The router types of the signatures are erased: Vert.x is never loaded.
 */
object ServerlessBotServer : BotServer {
    private class Registration(
        val serviceIdentifier: String,
        val routes: HttpRoutes,
    ) : ServiceRegistration

    private val registrations: MutableList<Registration> = CopyOnWriteArrayList()

    override val executionScope: ExecutionScope = TrackingExecutionScope()

    override fun registerServices(
        serviceIdentifier: String,
        installer: CoroutineRouterSupport.(Router) -> Any?,
    ): ServiceRegistration = unsupported(serviceIdentifier)

    override fun registerConnectorServices(
        serviceIdentifier: String,
        installer: CoroutineRouterSupport.(Router) -> Unit,
    ): ServiceRegistration = unsupported(serviceIdentifier)

    private fun unsupported(serviceIdentifier: String): Nothing =
        throw UnsupportedOperationException(
            "The services $serviceIdentifier need a Vert.x router: their connector does not declare HttpRoutes, " +
                "it can not run in a serverless process",
        )

    override fun registerConnectorRoutes(
        serviceIdentifier: String,
        routes: HttpRoutes,
    ): ServiceRegistration {
        val healthcheck = HttpRoutes { get("$serviceIdentifier/healthcheck") { HttpResponse.status() } }
        return Registration(serviceIdentifier, HttpRoutes { (healthcheck.routes + routes.routes).forEach { route(it.method, it.path, it.handler) } })
            .also { registrations += it }
    }

    override fun unregisterServices(registration: ServiceRegistration) {
        registrations.remove(registration)
    }

    override fun configure() {
        // the routes are dispatched as soon as they are registered
    }

    override fun deploy(onComplete: (Throwable?) -> Unit) {
        // the HTTP server, or the function runtime, is started by the launcher
        onComplete(null)
    }

    /** The registered services, in registration order. */
    val serviceIdentifiers: List<String> get() = registrations.map { it.serviceIdentifier }

    /** Handles the request with the first registered route that matches it, or answers 404. */
    suspend fun dispatch(request: HttpRequest): HttpResponse =
        registrations
            .firstNotNullOfOrNull { it.routes.find(request) }
            ?.let { (route, matched) -> route.handler(matched) }
            ?: HttpResponse.status(404)
}

/**
 * Found by [BotServerFactory.load], when tock-bot-engine (and its Vert.x server) is not on the classpath.
 */
class ServerlessBotServerFactory : BotServerFactory {
    override fun create(): BotServer = ServerlessBotServer
}
