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

import ai.tock.shared.http.DeferredHttpResponse
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VertxHttpRoutesTest {
    private lateinit var vertx: Vertx
    private var port: Int = 0
    private val client = HttpClient.newHttpClient()

    private val routes =
        HttpRoutes {
            post("/webhook/:id") { request ->
                HttpResponse
                    .json(
                        """{"id":"${request.pathParameter("id")}","q":"${request.queryParameter("q")}",""" +
                            """"signature":"${request.header("x-hub-signature")}","body":"${request.bodyAsString}"}""",
                        status = 201,
                    ).withHeader("X-Tock", "1")
            }
            get("/failure") { error("boom") }
            get("/stream") {
                val response = DeferredHttpResponse()
                response.startStream(prelude = "first\n")
                Thread {
                    Thread.sleep(500)
                    response.write("second\n")
                    response.close()
                }.start()
                response.await()
            }
        }

    @BeforeEach
    fun start() {
        vertx = Vertx.vertx()
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.install(routes, CoroutineScope(vertx.dispatcher()))
        port =
            vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(0)
                .toCompletionStage()
                .toCompletableFuture()
                .get()
                .actualPort()
    }

    @AfterEach
    fun stop() {
        vertx
            .close()
            .toCompletionStage()
            .toCompletableFuture()
            .get()
    }

    @Test
    fun `GIVEN installed routes WHEN a request is received THEN the handler gets the request and its response is sent`() {
        val response =
            client.send(
                HttpRequest
                    .newBuilder(URI("http://localhost:$port/webhook/42?q=x"))
                    .header("X-Hub-Signature", "sha1=abc")
                    .POST(HttpRequest.BodyPublishers.ofString("hello"))
                    .build(),
                BodyHandlers.ofString(),
            )

        assertEquals(201, response.statusCode())
        assertEquals("""{"id":"42","q":"x","signature":"sha1=abc","body":"hello"}""", response.body())
        assertEquals("application/json; charset=utf-8", response.headers().firstValue("Content-Type").get())
        assertEquals("1", response.headers().firstValue("X-Tock").get())
    }

    @Test
    fun `GIVEN a handler that fails WHEN it is called THEN the router answers 500`() {
        val response =
            client.send(HttpRequest.newBuilder(URI("http://localhost:$port/failure")).build(), BodyHandlers.ofString())

        assertEquals(500, response.statusCode())
    }

    @Test
    fun `GIVEN a streamed response WHEN it is read THEN each chunk reaches the client when it is written`() {
        val start = System.currentTimeMillis()
        val response = client.send(HttpRequest.newBuilder(URI("http://localhost:$port/stream")).build(), BodyHandlers.ofInputStream())
        val reader = response.body().bufferedReader()

        assertEquals("first", reader.readLine())
        val first = System.currentTimeMillis() - start
        assertEquals("second", reader.readLine())
        assertTrue(first < 400, "first chunk after $first ms")
    }
}
