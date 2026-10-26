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

import ai.tock.nlp.systemone.SystemOneClient
import ai.tock.nlp.systemone.SystemOneConfiguration
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SystemOneClientTest {
    private val server = MockWebServer()

    @AfterEach
    fun shutdown() {
        server.shutdown()
    }

    private fun client(): SystemOneClient {
        server.start()
        return SystemOneClient(
            SystemOneConfiguration(
                apiToken = "token",
                endpointUrl = server.url("/custom/decision").toString(),
            ),
        )
    }

    @Test
    fun `run deserializes a successful choice answer`() {
        val c = client()
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "result": {
                        "model": "clef",
                        "answers": {
                          "intent": {
                            "type": "choice",
                            "choice": "greetings",
                            "probabilities": {"greetings": 0.9, "goodbye": 0.1},
                            "confidence": 0.9
                          }
                        }
                      },
                      "success": true,
                      "errors": []
                    }
                    """.trimIndent(),
                ).setResponseCode(200),
        )

        val result =
            c.run(
                SystemOneClient.RunRequest(
                    model = "clef",
                    state = "hello",
                    questions =
                        mapOf(
                            "intent" to
                                SystemOneClient.ChoiceQuestion(
                                    instructions = "pick an intent",
                                    criteria = mapOf("greetings" to "greetings", "goodbye" to "goodbye"),
                                ),
                        ),
                ),
            )

        assertEquals("greetings", result.answers["intent"]?.choice)
        assertEquals(0.9, result.answers["intent"]?.probabilities?.get("greetings"))
        assertTrue(result is SystemOneClient.RunResult.Cloudflare)

        val recorded = server.takeRequest()
        assertEquals("Bearer token", recorded.getHeader("Authorization"))
        assertEquals("/custom/decision", recorded.path)
        val requestBody = recorded.body.readUtf8()
        assertTrue(requestBody.contains("\"model\":\"clef\""))
        assertTrue(requestBody.contains("\"type\":\"choice\""))
    }

    @Test
    fun `run throws when the api reports an error`() {
        val c = client()
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """{"result": null, "success": false, "errors": [{"code": 1000, "message": "boom"}]}""",
                ).setResponseCode(200),
        )

        assertFailsWith<IllegalStateException> {
            c.run(
                SystemOneClient.RunRequest(
                    model = "clef",
                    state = "hello",
                    questions = emptyMap(),
                ),
            )
        }
    }

    @Test
    fun `run deserializes a direct Jev response`() {
        val c = client()
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "model": "jev-1.13.0",
                      "answers": {
                        "intent": {
                          "type": "choice",
                          "choice": "greetings",
                          "probabilities": {"greetings": 0.9, "goodbye": 0.1},
                          "confidence": 0.9
                        }
                      },
                      "usage": {"input_tokens": 42, "output_tokens": 3}
                    }
                    """.trimIndent(),
                ).setResponseCode(200),
        )

        val result =
            c.run(
                SystemOneClient.RunRequest(
                    model = "jev-latest",
                    state = "hello",
                    questions = emptyMap(),
                ),
            )

        assertTrue(result is SystemOneClient.RunResult.SystemOneApi)
        assertEquals("jev-1.13.0", result.model)
        assertEquals("greetings", result.answers["intent"]?.choice)
        assertTrue(result.usage != null)
    }

    @Test
    fun `run deserializes a direct Strands Decider response`() {
        val c = client()
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "model": "strands-decider",
                      "answers": {
                        "intent": {
                          "type": "choice",
                          "choice": "greetings",
                          "probabilities": {"greetings": 0.9, "goodbye": 0.1}
                        }
                      },
                      "usage": {"input_tokens": 42},
                      "latency_ms": 12.5
                    }
                    """.trimIndent(),
                ).setResponseCode(200),
        )

        val result =
            c.run(
                SystemOneClient.RunRequest(
                    model = "strands-decider",
                    state = "hello",
                    questions = emptyMap(),
                ),
            )

        assertTrue(result is SystemOneClient.RunResult.SystemOneApi)
        assertEquals(12.5, result.latencyMs)
        assertEquals("greetings", result.answers["intent"]?.choice)
    }

    @Test
    fun `healthcheck returns false on error`() {
        val c = client()
        server.enqueue(MockResponse().setResponseCode(500))
        assertEquals(false, c.healthcheck())
    }

    @Test
    fun `healthcheck returns true on success`() {
        val c = client()
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """{"result": {"model": "clef", "answers": {}}, "success": true, "errors": []}""",
                ).setResponseCode(200),
        )
        assertEquals(true, c.healthcheck())
    }
}
