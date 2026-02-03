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

package ai.tock.bot.connector.web.sse

import ai.tock.bot.connector.web.WebConnectorResponseContract
import ai.tock.bot.connector.web.sse.channel.SseChannels
import ai.tock.shared.security.auth.spi.TOCK_USER_ID
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.vertx.sendSseMessage
import ai.tock.shared.vertx.setupSSE
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Future
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router
import mu.KotlinLogging

class SseEndpoint(internal val responseSerializer: ObjectMapper) {
    companion object {
        const val USER_ID_QUERY_PARAM = "userId"
    }

    private val logger = KotlinLogging.logger {}
    private val channels = SseChannels()

    fun configureRoute(
        router: Router,
        basePath: String,
        connectorId: String,
        webSecurityHandler: WebSecurityHandler,
    ) {
        router.get("$basePath/sse")
            .handler(webSecurityHandler)
            .handler { context ->
                try {
                    val userId = context.get<String>(TOCK_USER_ID) ?: context.queryParams()[USER_ID_QUERY_PARAM]
                    channels.register(context.response(), connectorId, userId)
                } catch (t: Throwable) {
                    context.fail(t)
                }
            }
    }

    fun sendResponse(
        applicationId: String,
        recipientId: String,
        response: WebConnectorResponseContract,
    ): Future<Unit> = channels.send(applicationId, recipientId, response)

    private fun SseChannels.register(
        response: HttpServerResponse,
        appId: String,
        userId: String,
    ) {
        initListeners()
        val channel =
            register(appId, userId) { msg ->
                logger.debug { "send response from channel: $msg" }
                response.sendSseMessage(responseSerializer.writeValueAsString(msg))
            }
        response.setupSSE { unregister(channel) }
        sendMissedEvents(channel)
    }
}
