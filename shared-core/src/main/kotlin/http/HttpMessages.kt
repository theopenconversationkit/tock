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

import java.nio.charset.StandardCharsets
import java.util.TreeMap

/**
 * The HTTP methods supported by [HttpRoutes].
 */
enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
}

/**
 * An HTTP request, independent of the server that received it (Vert.x, a serverless function...).
 *
 * @property method the method
 * @property path the path, without the query string
 * @property headers the headers: [header] reads them case-insensitively
 * @property queryParameters the query parameters
 * @property pathParameters the parameters extracted from the route path (`/webhook/:id`)
 * @property body the raw body, empty when there is none
 * @property remoteAddress the address of the client, when known
 */
class HttpRequest(
    val method: HttpMethod,
    val path: String,
    headers: Map<String, List<String>> = emptyMap(),
    val queryParameters: Map<String, List<String>> = emptyMap(),
    val pathParameters: Map<String, String> = emptyMap(),
    val body: ByteArray = ByteArray(0),
    val remoteAddress: String? = null,
) {
    val headers: Map<String, List<String>> = TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER).apply { putAll(headers) }

    /** The body decoded as UTF-8. */
    val bodyAsString: String get() = String(body, StandardCharsets.UTF_8)

    /** The first value of the header, read case-insensitively. */
    fun header(name: String): String? = headers[name]?.firstOrNull()

    /** The first value of the query parameter. */
    fun queryParameter(name: String): String? = queryParameters[name]?.firstOrNull()

    /** The value of the path parameter. */
    fun pathParameter(name: String): String? = pathParameters[name]

    /** A copy of this request with the given path parameters. */
    fun withPathParameters(parameters: Map<String, String>): HttpRequest = HttpRequest(method, path, headers, queryParameters, parameters, body, remoteAddress)
}

/**
 * An HTTP response, independent of the server that sends it.
 */
class HttpResponse(
    val status: Int = 200,
    val headers: Map<String, List<String>> = emptyMap(),
    val body: ByteArray = ByteArray(0),
) {
    /** A copy of this response with one more header value. */
    fun withHeader(
        name: String,
        value: String,
    ): HttpResponse = HttpResponse(status, headers + (name to (headers[name].orEmpty() + value)), body)

    companion object {
        /** An empty response with the given status, 200 by default. */
        fun status(status: Int = 200): HttpResponse = HttpResponse(status)

        /** A text response. */
        fun text(
            text: String,
            status: Int = 200,
            contentType: String = "text/plain; charset=utf-8",
        ): HttpResponse = HttpResponse(status, mapOf("Content-Type" to listOf(contentType)), text.toByteArray(StandardCharsets.UTF_8))

        /** A JSON response, from an already serialized body. */
        fun json(
            json: String,
            status: Int = 200,
        ): HttpResponse = text(json, status, "application/json; charset=utf-8")
    }
}

/**
 * Handles an [HttpRequest]. It may suspend: the server decides on which thread it runs.
 */
typealias HttpHandler = suspend (HttpRequest) -> HttpResponse
