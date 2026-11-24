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

package ai.tock.shared.vertx.http

import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ai.tock.shared.http.HttpMethod as TockHttpMethod
import io.vertx.core.http.HttpMethod as VertxHttpMethod

/**
 * Mounts [routes] on this router.
 *
 * Each handler runs in a coroutine of [scope], usually the verticle that owns the router: like the handlers
 * of a coroutine router, it must not block, and switches to another dispatcher for blocking work.
 * The body is read from the [io.vertx.ext.web.handler.BodyHandler] that [ai.tock.shared.vertx.WebVerticle] installs.
 *
 * @return the created Vert.x routes, so that the caller can remove them
 */
fun Router.install(
    routes: HttpRoutes,
    scope: CoroutineScope,
): List<Route> =
    routes.routes.map { route ->
        route(VertxHttpMethod.valueOf(route.method.name), route.path).handler { context ->
            scope.launch {
                try {
                    context.sendStreamed(route.handler(context.toHttpRequest()))
                } catch (t: Throwable) {
                    context.fail(t)
                }
            }
        }
    }

/**
 * Converts the Vert.x request to a server independent [HttpRequest].
 */
fun RoutingContext.toHttpRequest(): HttpRequest {
    val request = request()
    return HttpRequest(
        method = TockHttpMethod.valueOf(request.method().name()),
        path = request.path(),
        headers = request.headers().names().associateWith { request.headers().getAll(it) },
        queryParameters = queryParams().names().associateWith { queryParams().getAll(it) },
        pathParameters = pathParams().toMap(),
        body = body()?.buffer()?.bytes ?: ByteArray(0),
        remoteAddress = request.remoteAddress()?.host(),
        rawQuery = request.query(),
    )
}

/**
 * Sends a server independent [HttpResponse].
 */
fun RoutingContext.send(response: HttpResponse) {
    val vertxResponse = response().setStatusCode(response.status)
    response.headers.forEach { (name, values) -> vertxResponse.putHeader(name, values) }
    vertxResponse.end(Buffer.buffer(response.body))
}

/**
 * Sends a server independent [HttpResponse], whose [HttpResponse.stream] is written chunk by chunk.
 */
suspend fun RoutingContext.sendStreamed(response: HttpResponse) {
    val stream = response.stream ?: return send(response)
    val vertxResponse = response().setStatusCode(response.status)
    vertxResponse.isChunked = true
    response.headers.forEach { (name, values) -> vertxResponse.putHeader(name, values) }
    try {
        stream { chunk -> vertxResponse.write(Buffer.buffer(chunk)).coAwait() }
    } finally {
        if (!vertxResponse.ended() && !vertxResponse.closed()) vertxResponse.end()
    }
}
