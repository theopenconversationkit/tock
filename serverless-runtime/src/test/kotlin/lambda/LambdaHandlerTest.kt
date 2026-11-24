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
import ai.tock.serverless.ServerlessBotLauncher
import ai.tock.serverless.ServerlessBotServer
import ai.tock.serverless.ServerlessRequestHandler
import ai.tock.serverless.queue.QueuedRequest
import ai.tock.serverless.queue.RequestQueueing
import ai.tock.shared.Executor
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.tockInternalInjector
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.util.Base64
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

/** Installed by [LambdaHandler], declared in the test resources. */
class TestLambdaApplication : ServerlessApplication {
    companion object {
        val echoed = CopyOnWriteArrayList<HttpRequest>()
        val taskRuns = AtomicInteger()
        val replayed = CopyOnWriteArrayList<String>()
        val queueing =
            RequestQueueing(
                queue = { },
                queuedPaths = setOf("/hook"),
                secret = "the secret of the Lambda test",
            )
    }

    override fun install() {
        injector.inject(Kodein { import(ServerlessBotLauncher.serverlessModule) })
        ServerlessRequestHandler.configure(queueing)
        ServerlessBotServer.registerRoutes(
            "lambda_test",
            HttpRoutes {
                post("/echo/:name") { request ->
                    echoed += request
                    HttpResponse.text("hello ${request.pathParameter("name")} ${request.bodyAsString}").withHeader("Set-Cookie", "a=1")
                }
                post("/hook") { request ->
                    replayed += request.bodyAsString
                    HttpResponse.status(if (request.bodyAsString == "fails") 500 else 200)
                }
            },
        )
        injector.provide<Executor>().setPeriodic("lambda_test_task", Duration.ofMinutes(1), Duration.ofMinutes(1)) { taskRuns.incrementAndGet() }
    }
}

class LambdaHandlerTest {
    companion object {
        /** The other tests of the module inject their own modules: they must not see the ones of this test. */
        @JvmStatic
        @AfterAll
        fun resetInjector() {
            tockInternalInjector = KodeinInjector()
            ServerlessRequestHandler.configure(null)
        }
    }

    private val mapper = ObjectMapper()
    private val handler = LambdaHandler()

    private fun invoke(event: String): Map<*, *> {
        val output = ByteArrayOutputStream()
        handler.handleRequest(event.byteInputStream(), output)
        return mapper.readValue(output.toByteArray(), Map::class.java)
    }

    @Test
    fun `GIVEN a request of a function URL WHEN it is handled THEN the route answers in the payload format 2_0`() {
        val response =
            invoke(
                """
                {"version":"2.0","rawPath":"/echo/julien","rawQueryString":"a=1&b=x%20y&a=2",
                 "headers":{"content-type":"text/plain","x-auth-token":"t"},"cookies":["c=1","d=2"],
                 "requestContext":{"http":{"method":"POST","sourceIp":"10.0.0.1"}},
                 "body":"${Base64.getEncoder().encodeToString("the body".toByteArray())}","isBase64Encoded":true}
                """.trimIndent(),
            )

        assertEquals(200, response["statusCode"])
        assertEquals("hello julien the body", String(Base64.getDecoder().decode(response["body"] as String)))
        assertEquals(listOf("a=1"), response["cookies"])
        val request = TestLambdaApplication.echoed.last()
        assertEquals(mapOf("a" to listOf("1", "2"), "b" to listOf("x y")), request.queryParameters)
        assertEquals("t", request.header("X-Auth-Token"))
        assertEquals("c=1; d=2", request.header("Cookie"))
        assertEquals("10.0.0.1", request.remoteAddress)
    }

    @Test
    fun `GIVEN an event of EventBridge Scheduler WHEN it is handled THEN the periodic task runs before the answer`() {
        val before = TestLambdaApplication.taskRuns.get()

        assertEquals(202, invoke("""{"tock_task":"lambda_test_task"}""")["statusCode"])
        assertEquals(before + 1, TestLambdaApplication.taskRuns.get())
        assertEquals(404, invoke("""{"tock_task":"unknown"}""")["statusCode"])
    }

    @Test
    fun `GIVEN an SQS event WHEN it is handled THEN the queued webhooks are replayed, and the failed ones reported to be delivered again`() {
        fun message(body: String) = TestLambdaApplication.queueing.message(QueuedRequest.of(HttpRequest(HttpMethod.POST, "/hook", body = body.toByteArray())))
        val event =
            mapper.writeValueAsString(
                mapOf(
                    "Records" to
                        listOf(
                            mapOf("messageId" to "m1", "eventSource" to "aws:sqs", "body" to message("queued")),
                            mapOf("messageId" to "m2", "eventSource" to "aws:sqs", "body" to message("fails")),
                            // ignored: a message without a valid signature is never delivered again
                            mapOf("messageId" to "m3", "eventSource" to "aws:sqs", "body" to "not signed"),
                        ),
                ),
            )

        val response = invoke(event)

        assertEquals(listOf("queued", "fails"), TestLambdaApplication.replayed)
        assertEquals(listOf(mapOf("itemIdentifier" to "m2")), response["batchItemFailures"])
    }
}
