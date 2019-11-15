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

package ai.tock.bot.api.webhook

import com.fasterxml.jackson.module.kotlin.readValue
import ai.tock.bot.api.client.ClientBotDefinition
import ai.tock.bot.api.client.TockClientBus
import ai.tock.bot.api.client.toConfiguration
import ai.tock.bot.api.model.websocket.RequestData
import ai.tock.bot.api.model.websocket.ResponseData
import ai.tock.shared.jackson.mapper
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.makeDetailedHealthcheck
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

internal class WebhookVerticle(private val botDefinition: ClientBotDefinition) : WebVerticle() {

    override fun configure() {
        blocking(HttpMethod.POST, "/webhook") { context ->
            val content = context.bodyAsString
            val request: RequestData = mapper.readValue(content)
            if (request.botRequest != null) {
                val bus = TockClientBus(botDefinition, request) { response ->
                    context.response().end(mapper.writeValueAsString(ResponseData(request.requestId, response)))
                }
                bus.handle()
            } else if (request.configuration != null) {
                context.response().end(
                    mapper.writeValueAsString(
                        ResponseData(
                            request.requestId,
                            botConfiguration = botDefinition.toConfiguration()
                        )
                    )
                )
            } else {
                error("unknown request: $content")
            }
        }

    }

    override val defaultPort: Int = 8887

    override fun defaultHealthcheck(): (RoutingContext) -> Unit {
        return { it.response().end() }
    }

    override fun detailedHealthcheck(): (RoutingContext) -> Unit = makeDetailedHealthcheck()

}