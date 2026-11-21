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
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineRouterSupport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A [BotServer] that records the registrations, for the tests that do not need a real server.
 */
class TestBotServer(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) : BotServer {
    class Registration(
        val serviceIdentifier: String,
    ) : ServiceRegistration

    val registrations: MutableList<Registration> = mutableListOf()
    var configureCount: Int = 0
    var deployed: Boolean = false

    override val executionScope: ExecutionScope = CoroutineExecutionScope(scope)

    override fun registerServices(
        serviceIdentifier: String,
        installer: CoroutineRouterSupport.(Router) -> Any?,
    ): ServiceRegistration = Registration(serviceIdentifier).also { registrations += it }

    override fun registerConnectorServices(
        serviceIdentifier: String,
        installer: CoroutineRouterSupport.(Router) -> Unit,
    ): ServiceRegistration = Registration(serviceIdentifier).also { registrations += it }

    override fun unregisterServices(registration: ServiceRegistration) {
        registrations.remove(registration)
    }

    override fun configure() {
        configureCount++
    }

    override fun deploy(onComplete: (Throwable?) -> Unit) {
        deployed = true
        onComplete(null)
    }
}
