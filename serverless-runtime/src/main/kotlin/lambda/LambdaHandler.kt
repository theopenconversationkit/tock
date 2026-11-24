/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.serverless.lambda

import ai.tock.serverless.ServerlessApplication
import ai.tock.serverless.ServerlessRequestHandler
import ai.tock.shared.Loader
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.property
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import java.io.InputStream
import java.io.OutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * The handler of an AWS Lambda function, without the Lambda libraries nor the Lambda Web Adapter:
 * `ai.tock.serverless.lambda.LambdaHandler::handleRequest`.
 *
 * - an HTTP request of a function URL or of an API Gateway HTTP API (payload format 2.0) is dispatched to the
 *   routes, and its work is awaited;
 * - an SQS event (`Records` from `aws:sqs`) is processed by the queue of the webhooks, with the partial batch
 *   failures (`ReportBatchItemFailures`);
 * - an EventBridge Scheduler event whose input is `{"tock_task": "<name>"}` runs the periodic task.
 *
 * The [ServerlessApplication]s are installed once, when the class is initialized.
 */
class LambdaHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val mapper = ObjectMapper()
        private val queuePath = property("tock_serverless_queue_path", "/_queue")

        init {
            val applications = Loader.loadServices<ServerlessApplication>()
            if (applications.isEmpty()) {
                logger.warn { "no ServerlessApplication declared in META-INF/services: nothing is installed" }
            }
            applications.forEach { it.install() }
        }
    }

    fun handleRequest(
        input: InputStream,
        output: OutputStream,
    ) {
        val event = mapper.readTree(input)
        val result: Any =
            when {
                event.has("requestContext") && event.path("requestContext").has("http") -> {
                    toEvent(ServerlessRequestHandler.handle(toRequest(event)))
                }

                event
                    .path("Records")
                    .firstOrNull()
                    ?.path("eventSource")
                    ?.asText() == "aws:sqs" -> {
                    sqs(event)
                }

                event.has("tock_task") -> {
                    mapOf("statusCode" to ServerlessRequestHandler.runScheduledTask(event.path("tock_task").asText()).status)
                }

                else -> {
                    logger.error { "unsupported Lambda event: ${event.fieldNames().asSequence().toList()}" }
                    mapOf("statusCode" to 400)
                }
            }
        mapper.writeValue(output, result)
    }

    private fun sqs(event: JsonNode): JsonNode {
        val queueing = ServerlessRequestHandler.queueing ?: error("an SQS event without queue of the webhooks: install the bots with a RequestQueueing")
        val response = queueing.processSqsEvent(HttpRequest(HttpMethod.POST, "$queuePath/sqs", body = mapper.writeValueAsBytes(event)))
        return mapper.readTree(response.body)
    }

    /** An HTTP request of the payload format 2.0. */
    internal fun toRequest(event: JsonNode): HttpRequest {
        val http = event.path("requestContext").path("http")
        val headers = mutableMapOf<String, List<String>>()
        event.path("headers").fields().forEach { (name, value) -> headers[name] = listOf(value.asText()) }
        // the cookies are not in the headers of the format 2.0
        event.path("cookies").takeIf { it.isArray && !it.isEmpty }?.let { cookies -> headers["cookie"] = listOf(cookies.joinToString("; ") { it.asText() }) }
        val body =
            event.path("body").takeIf { it.isTextual }?.asText()?.let {
                if (event.path("isBase64Encoded").asBoolean()) Base64.getDecoder().decode(it) else it.toByteArray(StandardCharsets.UTF_8)
            } ?: ByteArray(0)
        return HttpRequest(
            method = HttpMethod.valueOf(http.path("method").asText().uppercase()),
            path = event.path("rawPath").asText("/"),
            headers = headers,
            queryParameters = queryParameters(event.path("rawQueryString").asText("")),
            body = body,
            remoteAddress = http.path("sourceIp").takeIf { it.isTextual }?.asText(),
        )
    }

    private fun queryParameters(query: String): Map<String, List<String>> =
        query
            .split('&')
            .filter { it.isNotEmpty() }
            .map { parameter ->
                val name = parameter.substringBefore('=')
                val value = if ('=' in parameter) parameter.substringAfter('=') else ""
                URLDecoder.decode(name, StandardCharsets.UTF_8) to URLDecoder.decode(value, StandardCharsets.UTF_8)
            }.groupBy({ it.first }, { it.second })

    /** The response of the payload format 2.0: the body in base 64, the header values joined. */
    internal fun toEvent(response: HttpResponse): Map<String, Any> {
        val setCookies =
            response.headers.entries
                .filter { it.key.equals("Set-Cookie", ignoreCase = true) }
                .flatMap { it.value }
        return mapOf(
            "statusCode" to response.status,
            "headers" to response.headers.filterKeys { !it.equals("Set-Cookie", ignoreCase = true) }.mapValues { it.value.joinToString(",") },
            "cookies" to setCookies,
            "body" to Base64.getEncoder().encodeToString(response.body),
            "isBase64Encoded" to true,
        )
    }
}
