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

package ai.tock.shared.http

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * A route: a method, a path and its handler.
 *
 * The path uses the Vert.x syntax: `:name` matches one segment and extracts it as a path parameter,
 * a trailing `*` matches any remaining segments. As with Vert.x, the requested path is percent-decoded segment by
 * segment, so path parameters are decoded (a `+` stays a `+`).
 */
class HttpRoute(
    val method: HttpMethod,
    val path: String,
    val handler: HttpHandler,
) {
    private val segments: List<String> = split(path)

    /**
     * Returns the path parameters if this route matches the request, null otherwise.
     */
    fun match(
        method: HttpMethod,
        path: String,
    ): Map<String, String>? = if (method == this.method) matchPath(path) else null

    /** Returns the path parameters if the path of this route matches [path], whatever the method. */
    internal fun matchPath(path: String): Map<String, String>? {
        val requested = split(path).map(::decode)
        val parameters = mutableMapOf<String, String>()
        segments.forEachIndexed { index, segment ->
            if (segment == "*" && index == segments.lastIndex) return parameters
            val value = requested.getOrNull(index) ?: return null
            when {
                segment.startsWith(":") -> parameters[segment.substring(1)] = value
                segment != value -> return null
            }
        }
        return if (requested.size == segments.size) parameters else null
    }

    private fun split(path: String): List<String> = path.split("/").filter { it.isNotEmpty() }

    private fun decode(segment: String): String =
        if ('%' in segment) {
            try {
                URLDecoder.decode(segment.replace("+", "%2B"), StandardCharsets.UTF_8)
            } catch (_: IllegalArgumentException) {
                // a malformed escape is kept as received
                segment
            }
        } else {
            segment
        }
}

/**
 * The routes of a service, declared independently of the server that exposes them:
 *
 * ```
 * HttpRoutes {
 *     post("/webhook/:id") { request -> HttpResponse.text("ok") }
 * }
 * ```
 */
class HttpRoutes(
    init: HttpRoutes.() -> Unit = {},
) {
    private val declared: MutableList<HttpRoute> = mutableListOf()

    /** The routes, in declaration order. */
    val routes: List<HttpRoute> get() = declared.toList()

    init {
        init()
    }

    fun route(
        method: HttpMethod,
        path: String,
        handler: HttpHandler,
    ) {
        declared += HttpRoute(method, path, handler)
    }

    fun get(
        path: String,
        handler: HttpHandler,
    ) = route(HttpMethod.GET, path, handler)

    fun post(
        path: String,
        handler: HttpHandler,
    ) = route(HttpMethod.POST, path, handler)

    fun put(
        path: String,
        handler: HttpHandler,
    ) = route(HttpMethod.PUT, path, handler)

    fun delete(
        path: String,
        handler: HttpHandler,
    ) = route(HttpMethod.DELETE, path, handler)

    /**
     * Finds the first route that matches the request, and returns the request with its path parameters.
     * As with Vert.x, a HEAD request without HEAD route is served by the GET route.
     * Used by the servers that do not route by themselves, such as a serverless function.
     */
    fun find(request: HttpRequest): Pair<HttpRoute, HttpRequest>? =
        find(request, request.method)
            ?: if (request.method == HttpMethod.HEAD) find(request, HttpMethod.GET) else null

    private fun find(
        request: HttpRequest,
        method: HttpMethod,
    ): Pair<HttpRoute, HttpRequest>? =
        declared.firstNotNullOfOrNull { route ->
            route.match(method, request.path)?.let { route to request.withPathParameters(it) }
        }

    /**
     * Handles the request with the first matching route. Otherwise answers, as Vert.x does, 405 with an `Allow`
     * header when the path matches routes of other methods, and 404 when it matches none.
     * A HEAD request served by a GET route gets its status and headers, without body.
     */
    suspend fun handle(request: HttpRequest): HttpResponse {
        val (route, matched) = find(request) ?: return notFoundOrNotAllowed(request)
        val response = route.handler(matched)
        return if (request.method == HttpMethod.HEAD && route.method != HttpMethod.HEAD) {
            HttpResponse(response.status, response.headers)
        } else {
            response
        }
    }

    private fun notFoundOrNotAllowed(request: HttpRequest): HttpResponse {
        val allowed = declared.filter { it.matchPath(request.path) != null }.map { it.method }.toSet()
        return if (allowed.isEmpty()) {
            HttpResponse.status(404)
        } else {
            HttpResponse(405, mapOf("Allow" to listOf((allowed + if (HttpMethod.GET in allowed) setOf(HttpMethod.HEAD) else emptySet()).joinToString(", "))))
        }
    }
}
