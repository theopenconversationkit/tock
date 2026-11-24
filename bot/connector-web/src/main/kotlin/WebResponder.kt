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

package ai.tock.bot.connector.web

import ai.tock.shared.http.DeferredHttpResponse
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.ServerSentEvents
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.vertx.sendSseMessage
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Sends the answer of a web request, whatever the server.
 */
internal interface WebResponder {
    /** Answers the JSON of the response (or ends the events with it). */
    fun json(body: String)

    /** Sends a server-sent event (and starts the events if needed). */
    fun sse(data: String)

    /** Fails the request (500). */
    fun fail(throwable: Throwable)
}

/** The responder of a server independent route. */
internal class HttpWebResponder(
    private val response: DeferredHttpResponse,
) : WebResponder {
    override fun json(body: String) {
        response.complete(HttpResponse(headers = mapOf("Content-Type" to listOf("application/json")), body = body.toByteArray()))
    }

    override fun sse(data: String) {
        if (!response.isStreaming) {
            response.startStream(headers = ServerSentEvents.headers)
        }
        response.write(ServerSentEvents.message(data))
    }

    override fun fail(throwable: Throwable) {
        logger.error(throwable) { "web request failed" }
        response.complete(HttpResponse.text("Internal Server Error", 500))
    }
}

/** The responder of a Vert.x route: the former behaviour. */
internal class VertxWebResponder(
    private val context: RoutingContext,
) : WebResponder {
    override fun json(body: String) {
        context.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(body)
    }

    override fun sse(data: String) {
        context.response().sendSseMessage(data)
    }

    override fun fail(throwable: Throwable) {
        context.fail(throwable)
    }
}

/**
 * The authentication of the users of a web connector: a Vert.x handler bound by the project (its Vert.x routes
 * are kept), or a server independent one.
 */
internal sealed interface WebConnectorSecurity {
    /** The handler of the Vert.x routes. */
    class Vertx(
        val handler: WebSecurityHandler,
    ) : WebConnectorSecurity

    /** The security of the server independent routes. */
    class Http(
        val security: ai.tock.shared.security.auth.spi.HttpWebSecurity,
    ) : WebConnectorSecurity
}
