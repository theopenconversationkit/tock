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

import ai.tock.shared.coroutines.CoroutineExecutionScope
import ai.tock.shared.coroutines.ExecutionScope
import ai.tock.shared.vertx.vertx
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineRouterSupport

/**
 * The [BotServer] of a long-running process: the [BotVerticle].
 */
internal class VertxBotServer(
    internal val verticle: BotVerticle = BotVerticle(),
) : BotServer {
    override val executionScope: ExecutionScope = CoroutineExecutionScope(verticle)

    override fun registerServices(
        serviceIdentifier: String,
        installer: CoroutineRouterSupport.(Router) -> Any?,
    ): ServiceRegistration = verticle.registerServices(serviceIdentifier, installer)

    override fun registerConnectorServices(
        serviceIdentifier: String,
        installer: CoroutineRouterSupport.(Router) -> Unit,
    ): ServiceRegistration =
        verticle.registerServices(serviceIdentifier) { router ->
            // healthcheck
            router.get("$serviceIdentifier/healthcheck").handler {
                it.response().end()
            }
            installer(router)
        }

    override fun unregisterServices(registration: ServiceRegistration) {
        verticle.unregisterServices(registration as BotVerticle.ServiceInstaller)
    }

    override fun configure() {
        verticle.configure()
    }

    override fun deploy(onComplete: (Throwable?) -> Unit) {
        vertx.deployVerticle(verticle).onComplete {
            onComplete(if (it.succeeded()) null else it.cause() ?: IllegalArgumentException())
        }
    }
}

/**
 * Found by [BotServerFactory.load].
 */
internal class VertxBotServerFactory : BotServerFactory {
    override fun create(): BotServer = VertxBotServer()
}
