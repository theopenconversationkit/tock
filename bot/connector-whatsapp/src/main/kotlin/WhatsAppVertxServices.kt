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

package ai.tock.bot.connector.whatsapp

import ai.tock.bot.engine.ConnectorController
import ai.tock.shared.security.RequestFilter

/**
 * The Vert.x route of a [WhatsAppConnector] created with a Vert.x [RequestFilter]: the former behaviour.
 */
internal object WhatsAppVertxServices {
    fun register(
        connector: WhatsAppConnector,
        controller: ConnectorController,
        path: String,
        requestFilter: RequestFilter,
    ) {
        controller.registerServices(path) { router ->
            router.post(path).handler { context ->
                if (!requestFilter.accept(context.request())) {
                    context.response().setStatusCode(403).end()
                    return@handler
                }
                connector.handleWebhook(controller, context.body().asString() ?: "")
                context.response().end()
            }
        }
    }
}
