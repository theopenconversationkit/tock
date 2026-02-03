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

import ai.tock.bot.connector.web.sse.channel.ChannelDAO
import ai.tock.bot.connector.web.sse.channel.ChannelEvent
import ai.tock.bot.connector.web.sse.channel.SseChannels
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.auth.spi.TOCK_USER_ID
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.regex.Pattern
import io.mockk.verify as verifyMockk

@ExtendWith(VertxExtension::class)
class SseEndpointTest {
    private val channelDAO: ChannelDAO = mockk()
    private val channels = SseChannels(channelDAO)
    private val endpoint = SseEndpoint(mapper, channels)
    private val webSecurityHandler: WebSecurityHandler =
        mockk {
            every { handle(any()) } answers { firstArg<RoutingContext>().next() }
        }

    private val basePath = "/api/bot"
    private val connectorId = "test-connector"
    private val userId = "integration-test-user"
    private val port = 8888

    private lateinit var client: HttpClient

    @BeforeEach
    fun VertxTestContext.setup(vertx: Vertx) {
        every { channelDAO.listenChanges(any()) } just runs
        every { channelDAO.handleMissedEvents(any(), any(), any()) } just runs
        every { channelDAO.save(any()) } just runs

        client = vertx.createHttpClient(HttpClientOptions().setDefaultPort(port).setDefaultHost("localhost"))

        val router = Router.router(vertx)
        endpoint.configureRoute(router, basePath, connectorId, webSecurityHandler)

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(port)
            .onComplete(succeedingThenComplete())
    }

    @Test
    fun VertxTestContext.`SSE endpoint establishes connection and receives events`() {
        val text1 = "Hello from SSE!"
        val text2 = "Second message"
        val message1 = WebConnectorResponse(TestWebMessage(text1))
        val message2 = WebConnectorResponse(TestWebMessage(text2))

        val checkpoint = checkpoint(1)

        // Connect to SSE endpoint
        client.request(HttpMethod.GET, "$basePath/sse?${SseEndpoint.USER_ID_QUERY_PARAM}=$userId")
            .compose(HttpClientRequest::send)
            .onComplete(
                succeeding { httpResponse ->
                    assertEquals(200, httpResponse.statusCode())
                    assertEquals("text/event-stream;charset=UTF-8", httpResponse.getHeader("Content-Type"))

                    var bodyStr = ""

                    // Read SSE stream
                    httpResponse.handler { buffer ->
                        val chunk = buffer.toString(Charsets.UTF_8)
                        bodyStr += chunk

                        // When we receive the second message, check the stream
                        if (chunk.contains(text2)) {
                            if (!bodyStr.contains(
                                    """
                                |event: message
                                |data: ${Pattern.quote(mapper.writeValueAsString(message1))}
                                |.*
                                |event: message
                                |data: ${Pattern.quote(mapper.writeValueAsString(message2))}
                                |
                                    """.trimMargin().toRegex(),
                                )
                            ) {
                                failNow("Incorrect SSE stream:\n$bodyStr")
                            }
                            checkpoint.flag()
                        } else {
                            println("received: $chunk")
                        }
                    }

                    endpoint.sendResponse(connectorId, userId, message1)
                    endpoint.sendResponse(connectorId, userId, message2)
                },
            )
    }

    @Test
    fun VertxTestContext.`SSE endpoint rejects request without userId`() {
        client.request(HttpMethod.GET, "$basePath/sse")
            .compose(HttpClientRequest::send)
            .onComplete(
                succeeding { response ->
                    assertEquals(400, response.statusCode())
                    completeNow()
                },
            )
    }

    @Test
    fun VertxTestContext.`SSE endpoint accepts userId from custom security handler`() {
        every { webSecurityHandler.handle(any()) } answers {
            val context = firstArg<RoutingContext>()
            context.put(TOCK_USER_ID, "test")
            context.next()
        }
        client.request(HttpMethod.GET, "$basePath/sse")
            .compose(HttpClientRequest::send)
            .onComplete(
                succeeding { response ->
                    assertEquals(200, response.statusCode())
                    assertEquals("text/event-stream;charset=UTF-8", response.getHeader("Content-Type"))
                    completeNow()
                },
            )
    }

    @Test
    fun VertxTestContext.`sendResponse saves to database when no local connection exists`() {
        val message = WebConnectorResponse(TestWebMessage("Offline message"))

        endpoint.sendResponse(connectorId, "offline-user", message)
            .onComplete {
                verifyMockk { channelDAO.save(any()) }
                completeNow()
            }
    }

    @Test
    fun VertxTestContext.`multiple clients can connect and receive messages independently`() {
        val user1 = "user-1"
        val user2 = "user-2"
        val checkpointUser1 = checkpoint()
        val checkpointUser2 = checkpoint()
        val listenerSlot = slot<ChannelEvent.Handler>()
        every { channelDAO.listenChanges(capture(listenerSlot)) } just runs

        // Connect first client
        val future1 =
            client.request(HttpMethod.GET, "$basePath/sse?${SseEndpoint.USER_ID_QUERY_PARAM}=$user1")
                .compose(HttpClientRequest::send)
                .onComplete(
                    succeeding { response1 ->
                        response1.handler { buffer ->
                            val data = buffer.toString()
                            if (data.contains("Message for user 1")) {
                                checkpointUser1.flag()
                            }
                        }
                    },
                )

        // Connect second client
        val future2 =
            client.request(HttpMethod.GET, "$basePath/sse?${SseEndpoint.USER_ID_QUERY_PARAM}=$user2")
                .compose(HttpClientRequest::send)
                .onComplete(
                    succeeding { response2 ->
                        response2.handler { buffer ->
                            val data = buffer.toString()
                            if (data.contains("Message for user 2")) {
                                checkpointUser2.flag()
                            }
                        }
                    },
                )

        Future.all(future1, future2).onComplete { _ ->
            // Send messages to different users
            listenerSlot.captured.invoke(
                ChannelEvent(connectorId, user1, WebConnectorResponse(TestWebMessage("Message for user 1"))),
            )
            listenerSlot.captured.invoke(
                ChannelEvent(connectorId, user2, WebConnectorResponse(TestWebMessage("Message for user 2"))),
            )
        }
    }
}
