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

package ai.tock.bot.connector.openai

import ai.tock.shared.http.DeferredHttpResponse
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.ServerSentEvents
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.vertx.sendSseMessage
import ai.tock.shared.vertx.setupSSE
import io.vertx.core.Future
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext

/** The end of the server-sent events of a chat completion. */
private const val DONE = "data: [DONE]\n\n"

/**
 * Sends the answer of a chat completion, whatever the server.
 */
internal interface OpenAIResponder {
    /** Answers the JSON of a chat completion. */
    fun json(body: String)

    /** Sends a chunk of a streamed chat completion. */
    fun sse(data: String)

    /** Ends the streamed chat completion. */
    fun end()

    /** Starts a streamed chat completion: its server-sent events. */
    fun startStream()

    /** Answers an empty 200. */
    fun empty()

    /** Fails the request (500). */
    fun fail(throwable: Throwable)
}

/** The responder of a server independent route. */
internal class HttpOpenAIResponder(
    private val response: DeferredHttpResponse,
) : OpenAIResponder {
    override fun json(body: String) {
        response.complete(HttpResponse(headers = mapOf("Content-Type" to listOf("application/json")), body = body.toByteArray()))
    }

    override fun sse(data: String) {
        response.write(ServerSentEvents.message(data))
    }

    override fun end() {
        response.write(DONE)
        response.close()
    }

    override fun startStream() {
        ServerSentEvents.start(response, keepAlive = false)
    }

    override fun empty() {
        response.complete(HttpResponse.status())
    }

    override fun fail(throwable: Throwable) {
        response.complete(HttpResponse.text("Internal Server Error", 500))
    }
}

/** The responder of a Vert.x route: the former behaviour. */
internal class VertxOpenAIResponder(
    private val context: RoutingContext,
) : OpenAIResponder {
    override fun json(body: String) {
        context.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(body)
    }

    override fun sse(data: String) {
        context.response().sendSseMessage(data)
    }

    override fun end() {
        context.response().apply { Future.all(write(DONE), end()) }
    }

    override fun startStream() {
        context.response().setupSSE(addEndHandler = true, keepAlive = false)
    }

    override fun empty() {
        context.end()
    }

    override fun fail(throwable: Throwable) {
        context.fail(throwable)
    }
}

/**
 * The authentication of the users of the connector: a Vert.x handler bound by the project (its Vert.x routes are
 * kept), or a server independent one.
 */
internal sealed interface OpenAIConnectorSecurity {
    class Vertx(
        val handler: WebSecurityHandler,
    ) : OpenAIConnectorSecurity

    class Http(
        val security: ai.tock.shared.security.auth.spi.HttpWebSecurity,
    ) : OpenAIConnectorSecurity
}
