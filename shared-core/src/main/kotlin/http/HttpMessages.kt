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
 * @property rawQuery the query string as received (undecoded), when known: the signatures of some webhooks are
 * computed on it
 */
class HttpRequest(
    val method: HttpMethod,
    val path: String,
    headers: Map<String, List<String>> = emptyMap(),
    val queryParameters: Map<String, List<String>> = emptyMap(),
    val pathParameters: Map<String, String> = emptyMap(),
    val body: ByteArray = ByteArray(0),
    val remoteAddress: String? = null,
    val rawQuery: String? = null,
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
    fun withPathParameters(parameters: Map<String, String>): HttpRequest = HttpRequest(method, path, headers, queryParameters, parameters, body, remoteAddress, rawQuery)
}

/**
 * Writes the chunks of a streamed [HttpResponse] (server-sent events...).
 */
fun interface HttpBodyWriter {
    /** Writes and flushes [chunk]: fails when the client is gone. */
    suspend fun write(chunk: ByteArray)
}

/**
 * An HTTP response, independent of the server that sends it.
 *
 * @property stream when set, the body is streamed (chunked) by this function instead of [body]: the servers that
 * cannot stream (a buffered AWS Lambda response) send all the chunks at its end
 */
class HttpResponse(
    val status: Int = 200,
    val headers: Map<String, List<String>> = emptyMap(),
    val body: ByteArray = ByteArray(0),
    val stream: (suspend (HttpBodyWriter) -> Unit)? = null,
) {
    /** A copy of this response with one more header value. */
    fun withHeader(
        name: String,
        value: String,
    ): HttpResponse = HttpResponse(status, headers + (name to (headers[name].orEmpty() + value)), body, stream)

    /** A copy of this response whose [stream] runs [after] once it has ended (or failed). */
    fun afterStream(after: suspend () -> Unit): HttpResponse =
        stream?.let { original ->
            HttpResponse(status, headers, body) { writer ->
                try {
                    original(writer)
                } finally {
                    after()
                }
            }
        } ?: this

    /** The body, or all the chunks of the [stream]. */
    suspend fun bufferedBody(): ByteArray {
        val original = stream ?: return body
        val buffer = java.io.ByteArrayOutputStream()
        original { chunk -> buffer.write(chunk) }
        return buffer.toByteArray()
    }

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
