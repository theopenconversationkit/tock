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

package ai.tock.bot.connector.alcmeon

import ai.tock.bot.engine.ConnectorController
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

/**
 * The Vert.x routes of an [AlcmeonConnector] created with a Vert.x handler of the signature: the former behaviour.
 */
internal object AlcmeonVertxServices {
    fun register(
        connector: AlcmeonConnector,
        controller: ConnectorController,
        path: String,
        description: String,
        authorisationHandler: Handler<RoutingContext>,
    ) {
        controller.registerServices(path) { router ->
            router
                .get("$path/description")
                .handler { context -> context.response().end(description) }
            listOf("$path/start", "$path/handle-event").forEach { route ->
                router
                    .post(route)
                    .handler(authorisationHandler)
                    .handler { context ->
                        connector.handleMessage(checkNotNull(context.body().asString()), controller) { backend ->
                            AlcmeonConnectorCallback(connector.connectorId, backend, context)
                        }
                    }
            }
        }
    }
}
