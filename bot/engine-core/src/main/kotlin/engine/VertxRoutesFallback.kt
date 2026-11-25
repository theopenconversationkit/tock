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

import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.vertx.http.install

/**
 * The default [ConnectorController.registerRoutes], for the controllers that only implement
 * [ConnectorController.coRegisterServices] (a project wrapper, a test double): the routes are installed on its
 * Vert.x router, so it only works with Vert.x.
 */
internal object VertxRoutesFallback {
    fun register(
        controller: ConnectorController,
        serviceIdentifier: String,
        routes: HttpRoutes,
    ) {
        controller.coRegisterServices(serviceIdentifier) { router ->
            router.get("$serviceIdentifier/healthcheck").handler { it.response().end() }
            router.install(routes, this)
        }
    }
}
