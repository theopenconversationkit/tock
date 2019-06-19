/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.api.webhook

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.api.client.ClientBotDefinition
import fr.vsct.tock.bot.api.client.TockClientBus
import fr.vsct.tock.bot.api.model.BotResponse
import fr.vsct.tock.bot.api.model.ResponseContext
import fr.vsct.tock.bot.api.model.UserRequest
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class WebhookVerticle(val botDefinition: ClientBotDefinition) : WebVerticle() {

    override fun configure() {
        blocking(HttpMethod.POST, "/webhook") { context ->
            val request: UserRequest = mapper.readValue(context.bodyAsString)
            val bus = TockClientBus(botDefinition, request) { messages ->
                context.response().end(mapper.writeValueAsString(
                    BotResponse(messages, ResponseContext(request.context.requestId))
                ))
            }
            bus.handle()
        }

    }

    override val defaultPort: Int = 8887

    override fun healthcheck(): (RoutingContext) -> Unit = {
        it.response().end()
    }

}