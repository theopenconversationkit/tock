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

import ai.tock.shared.Executor
import ai.tock.shared.http.DeferredHttpResponse
import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A streamed response of the serverless runtime: the chunks reach the client at once, the work is awaited at the end. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerlessStreamingTest {
    private lateinit var server: ServerlessHttpServer
    private val workDone = AtomicBoolean()

    @BeforeAll
    fun start() {
        injector.inject(Kodein { import(ServerlessBotLauncher.serverlessModule) })
        ServerlessRequestHandler.configure(null)
        ServerlessBotServer.registerRoutes(
            "streaming_test",
            HttpRoutes {
                get("/stream") {
                    val response = DeferredHttpResponse()
                    response.startStream(prelude = "first\n")
                    // the work of the request feeds the stream, then goes on after its end
                    injector.provide<Executor>().executeBlocking {
                        Thread.sleep(500)
                        response.write("second\n")
                        response.close()
                        Thread.sleep(300)
                        workDone.set(true)
                    }
                    response.await()
                }
            },
        )
        server = ServerlessHttpServer(0).start()
    }

    @AfterAll
    fun stop() {
        server.stop()
        tockInternalInjector = KodeinInjector()
    }

    @Test
    fun `GIVEN a streamed response WHEN it is read THEN the first chunk comes at once, and the response ends after the work`() {
        val start = System.currentTimeMillis()
        val response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI("http://localhost:${server.port}/stream")).build(), BodyHandlers.ofInputStream())
        val reader = response.body().bufferedReader()

        assertEquals("first", reader.readLine())
        val first = System.currentTimeMillis() - start
        assertEquals("second", reader.readLine())
        assertEquals(null, reader.readLine())

        assertTrue(first < 400, "first chunk after $first ms")
        assertTrue(workDone.get(), "the response ends once the work of the request is done")
    }
}
