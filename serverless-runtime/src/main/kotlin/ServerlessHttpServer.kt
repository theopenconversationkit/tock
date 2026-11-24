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

package ai.tock.serverless

import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

/**
 * A minimal HTTP server of the JDK for the serverless process: Cloud Run, or AWS Lambda with the Lambda Web
 * Adapter, forward the requests to it. Each request is handled by [ServerlessRequestHandler].
 */
class ServerlessHttpServer(
    port: Int,
) {
    private val logger = KotlinLogging.logger {}
    private val server: HttpServer =
        HttpServer.create(InetSocketAddress(port), 0).apply {
            executor = Executors.newCachedThreadPool()
            createContext("/") { exchange -> handle(exchange) }
        }

    /** The port the server listens to (useful when started on port 0). */
    val port: Int get() = server.address.port

    fun start(): ServerlessHttpServer = apply { server.start() }

    fun stop() {
        server.stop(0)
    }

    private fun handle(exchange: HttpExchange) {
        try {
            val method = HttpMethod.entries.firstOrNull { it.name == exchange.requestMethod }
            val response =
                if (method == null) {
                    ai.tock.shared.http.HttpResponse
                        .status(405)
                } else {
                    ServerlessRequestHandler.handle(
                        HttpRequest(
                            method = method,
                            path = exchange.requestURI.rawPath,
                            headers = exchange.requestHeaders.mapValues { it.value.toList() },
                            queryParameters = queryParameters(exchange.requestURI.rawQuery),
                            rawQuery = exchange.requestURI.rawQuery,
                            body = exchange.requestBody.use { it.readAllBytes() },
                            remoteAddress = exchange.remoteAddress?.address?.hostAddress,
                        ),
                    )
                }
            response.headers.forEach { (name, values) -> values.forEach { exchange.responseHeaders.add(name, it) } }
            if (response.body.isEmpty()) {
                exchange.sendResponseHeaders(response.status, -1)
            } else {
                exchange.sendResponseHeaders(response.status, response.body.size.toLong())
                exchange.responseBody.use { it.write(response.body) }
            }
        } catch (t: Throwable) {
            logger.error(t) { "${exchange.requestMethod} ${exchange.requestURI} failed" }
            runCatching { exchange.sendResponseHeaders(500, -1) }
        } finally {
            exchange.close()
        }
    }

    private fun queryParameters(rawQuery: String?): Map<String, List<String>> =
        rawQuery
            ?.split("&")
            ?.filter { it.isNotEmpty() }
            ?.map { parameter ->
                val (name, value) = parameter.split("=", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                URLDecoder.decode(name, StandardCharsets.UTF_8) to URLDecoder.decode(value, StandardCharsets.UTF_8)
            }?.groupBy({ it.first }, { it.second })
            ?: emptyMap()
}
