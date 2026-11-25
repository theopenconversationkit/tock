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

package ai.tock.bot.engine

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TockConnectorControllerAudioDownloadTest {
    private val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
    private val releaseStuckHandler = CountDownLatch(1)

    private fun url(path: String) = "http://localhost:${server.address.port}$path"

    private fun serve(
        path: String,
        body: ByteArray,
    ) {
        server.createContext(path) { exchange ->
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
    }

    @AfterEach
    fun stop() {
        releaseStuckHandler.countDown()
        server.stop(0)
    }

    @Test
    fun `GIVEN a file smaller than the limit WHEN downloading it THEN its content is returned`() {
        val content = ByteArray(100) { it.toByte() }
        serve("/small", content)
        server.start()

        assertContentEquals(content, TockConnectorController.downloadAudioFile(url("/small"), maxSize = 101, timeoutInMs = 1000))
    }

    @Test
    fun `GIVEN a file not smaller than the limit WHEN downloading it THEN null is returned`() {
        serve("/large", ByteArray(100))
        server.start()

        assertNull(TockConnectorController.downloadAudioFile(url("/large"), maxSize = 100, timeoutInMs = 1000))
    }

    @Test
    fun `GIVEN an unresponsive server WHEN downloading a file THEN the download times out`() {
        server.createContext("/stuck") {
            releaseStuckHandler.await(10, TimeUnit.SECONDS)
        }
        server.start()

        val start = System.nanoTime()
        assertFailsWith<SocketTimeoutException> {
            TockConnectorController.downloadAudioFile(url("/stuck"), timeoutInMs = 200)
        }
        assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) < 5000)
    }
}
