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

package ai.tock.serverless.pubsub

import ai.tock.serverless.queue.QueuedRequest
import ai.tock.serverless.queue.RequestQueueing
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.databind.JsonNode
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PubSubRequestQueueTest {
    private val published = CopyOnWriteArrayList<Pair<String, JsonNode>>()
    private val authorizations = CopyOnWriteArrayList<String?>()
    private var tokenRequests = 0
    private var publishStatus = 200
    private lateinit var server: HttpServer

    private val processed = CopyOnWriteArrayList<HttpRequest>()
    private val queueing =
        RequestQueueing({ error("not used") }, setOf("/whatsapp"), "the secret of the test queue", process = {
            processed += it
            HttpResponse.status()
        })

    @BeforeEach
    fun start() {
        // the Pub/Sub API and the metadata server
        server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/v1/") { exchange ->
                    published += exchange.requestURI.path to mapper.readTree(exchange.requestBody.readAllBytes())
                    authorizations += exchange.requestHeaders.getFirst("Authorization")
                    val answer = """{"messageIds":["1"]}""".toByteArray()
                    exchange.sendResponseHeaders(publishStatus, answer.size.toLong())
                    exchange.responseBody.use { it.write(answer) }
                }
                createContext("/computeMetadata/v1/instance/service-accounts/default/token") { exchange ->
                    tokenRequests++
                    val answer =
                        (
                            if (exchange.requestHeaders.getFirst("Metadata-Flavor") == "Google") {
                                """{"access_token":"token-$tokenRequests","expires_in":3599,"token_type":"Bearer"}"""
                            } else {
                                ""
                            }
                        ).toByteArray()
                    exchange.sendResponseHeaders(if (answer.isEmpty()) 403 else 200, answer.size.toLong().coerceAtLeast(-1))
                    exchange.responseBody.use { it.write(answer) }
                }
                start()
            }
    }

    @AfterEach
    fun stop() {
        server.stop(0)
    }

    private val host get() = "localhost:${server.address.port}"

    private fun webhook(body: String) = HttpRequest(HttpMethod.POST, "/whatsapp", body = body.toByteArray())

    @Test
    fun `GIVEN a webhook WHEN it is published THEN the pushed message is processed by the consumer`() {
        val queue =
            PubSubRequestQueue("projects/p/topics/webhooks", { queueing }, "http://$host", MetadataServerToken(host), orderingKeys = true)

        queue.send(QueuedRequest.of(webhook("""{"entry":[1]}""")))
        queue.send(QueuedRequest.of(webhook("""{"entry":[2]}""")))

        assertEquals(listOf("/v1/projects/p/topics/webhooks:publish", "/v1/projects/p/topics/webhooks:publish"), published.map { it.first })
        assertEquals(listOf("Bearer token-1", "Bearer token-1"), authorizations.toList(), "the token is cached")
        val message = published.first().second["messages"][0]
        assertEquals("/whatsapp", message["orderingKey"].asText())

        // what the push subscription posts
        val push = mapper.writeValueAsBytes(mapOf("message" to mapOf("data" to message["data"].asText(), "messageId" to "1"), "subscription" to "s"))
        assertEquals(204, queueing.processPubSubPush(HttpRequest(HttpMethod.POST, "/_queue/pubsub", body = push)).status)
        assertEquals("""{"entry":[1]}""", processed.single().bodyAsString)
    }

    @Test
    fun `GIVEN the emulator WHEN a webhook is published THEN no token is sent, and a failure is reported`() {
        val queue = PubSubRequestQueue("projects/p/topics/webhooks", { queueing }, "http://$host", accessToken = { null })

        queue.send(QueuedRequest.of(webhook("{}")))
        assertEquals(listOf<String?>(null), authorizations.toList())
        assertEquals(null, published.single().second["messages"][0]["orderingKey"])

        publishStatus = 500
        assertFailsWith<IllegalStateException> { queue.send(QueuedRequest.of(webhook("{}"))) }
    }
}
