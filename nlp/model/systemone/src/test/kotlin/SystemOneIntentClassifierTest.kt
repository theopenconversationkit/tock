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

import ai.tock.nlp.core.Application
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.configuration.NlpModelConfiguration
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.service.engine.IntentModelHolder
import ai.tock.nlp.systemone.SystemOneConfiguration
import ai.tock.nlp.systemone.SystemOneEngineProvider
import ai.tock.nlp.systemone.SystemOneIntentClassifier
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SystemOneIntentClassifierTest {
    private val server = MockWebServer()

    @AfterEach
    fun shutdown() {
        server.shutdown()
    }

    @Test
    fun `provider uses intent classifier properties for the question instructions`() {
        server.start()
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "result": {
                        "answers": {
                          "intent": {
                            "type": "choice",
                            "choice": "app:greetings",
                            "probabilities": {"app:greetings": 0.7, "app:goodbye": 0.3}
                          }
                        }
                      },
                      "success": true,
                      "errors": []
                    }
                    """.trimIndent(),
                ).setResponseCode(200),
        )

        val endpointProperty = "tock_systemone_endpoint_url"
        val tokenProperty = "tock_systemone_api_token"
        val modelProperty = "tock_systemone_model"
        val previousProperties =
            listOf(endpointProperty, tokenProperty, modelProperty).associateWith(System::getProperty)
        System.setProperty(endpointProperty, server.url("/accounts/acc/ai/run/@cf/cloudflare/clef").toString())
        System.setProperty(tokenProperty, "test-token")
        System.setProperty(modelProperty, "clef")

        try {
            val instructions = "Choose the best matching intent based on the user's message."
            val intents =
                listOf(
                    Intent("app:greetings", emptyList()),
                    Intent("app:goodbye", emptyList()),
                )
            val application = Application("app", intents, setOf(Locale.ENGLISH))
            val configuration =
                NlpApplicationConfiguration(
                    intentConfiguration =
                        NlpModelConfiguration(
                            properties =
                                Properties().apply {
                                    setProperty(
                                        SystemOneIntentClassifier.INTENT_QUESTION_INSTRUCTIONS_PROPERTY,
                                        instructions,
                                    )
                                },
                        ),
                )
            val classifier =
                SystemOneEngineProvider().getIntentClassifier(
                    IntentModelHolder(application, "clef", configuration),
                )

            classifier.classifyIntent(
                IntentContext(application, Locale.ENGLISH, NlpEngineType.systemone),
                "hello",
                arrayOf("hello"),
            )

            val requestBody = server.takeRequest().body.readUtf8()
            assertEquals(true, requestBody.contains("\"instructions\":\"$instructions\""))
        } finally {
            previousProperties.forEach { (key, value) ->
                if (value == null) {
                    System.clearProperty(key)
                } else {
                    System.setProperty(key, value)
                }
            }
        }
    }

    @Test
    fun `classifyIntent ranks intents by descending probability and uses intent description as criteria`() {
        server.start()

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
                            "choice": "app:greetings",
                            "probabilities": {"app:greetings": 0.7, "app:goodbye": 0.3},
                            "confidence": 0.7
                          }
                        }
                      },
                      "success": true,
                      "errors": []
                    }
                    """.trimIndent(),
                ).setResponseCode(200),
        )

        val greetings = Intent("app:greetings", emptyList(), description = "say hello to the user")
        val goodbye = Intent("app:goodbye", emptyList(), description = "say goodbye to the user")
        val application = Application("app", listOf(greetings, goodbye), setOf(Locale.ENGLISH))
        val context = IntentContext(application, Locale.ENGLISH, NlpEngineType.systemone)

        val conf =
            SystemOneConfiguration(
                apiToken = "token",
                endpointUrl = server.url("/accounts/acc/ai/run/@cf/cloudflare/clef").toString(),
                model = "clef",
            )
        val classification = SystemOneIntentClassifier(conf).classifyIntent(context, "hi there", arrayOf("hi", "there"))

        assertEquals(true, classification.hasNext())
        assertEquals(greetings, classification.next())
        assertEquals(0.7, classification.probability())

        assertEquals(true, classification.hasNext())
        assertEquals(goodbye, classification.next())
        assertEquals(0.3, classification.probability())

        assertEquals(false, classification.hasNext())

        val sentCriteria = server.takeRequest().body.readUtf8()
        assertEquals(true, sentCriteria.contains("say hello to the user"))
        assertEquals(true, sentCriteria.contains("say goodbye to the user"))
    }

    @Test
    fun `classifyIntent sends null criteria for intents without a description`() {
        server.start()

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
                            "choice": "app:greetings",
                            "probabilities": {"app:greetings": 0.6, "app:goodbye": 0.4},
                            "confidence": 0.6
                          }
                        }
                      },
                      "success": true,
                      "errors": []
                    }
                    """.trimIndent(),
                ).setResponseCode(200),
        )

        // null and blank descriptions should both be sent as `null` - Clef's documented
        // convention for "no detail needed" for a choice option.
        val greetings = Intent("app:greetings", emptyList(), description = null)
        val goodbye = Intent("app:goodbye", emptyList(), description = "  ")
        val application = Application("app", listOf(greetings, goodbye), setOf(Locale.ENGLISH))
        val context = IntentContext(application, Locale.ENGLISH, NlpEngineType.systemone)
        val conf =
            SystemOneConfiguration(
                apiToken = "token",
                endpointUrl = server.url("/accounts/acc/ai/run/@cf/cloudflare/clef").toString(),
                model = "clef",
            )

        SystemOneIntentClassifier(conf).classifyIntent(context, "hi there", arrayOf("hi", "there"))

        val sentCriteria = server.takeRequest().body.readUtf8()
        assertEquals(true, sentCriteria.contains("\"app:greetings\":null"))
        assertEquals(true, sentCriteria.contains("\"app:goodbye\":null"))
    }

    @Test
    fun `classifyIntent returns an empty result when the application has no intents`() {
        val application = Application("app", emptyList(), setOf(Locale.ENGLISH))
        val context = IntentContext(application, Locale.ENGLISH, NlpEngineType.systemone)
        val conf =
            SystemOneConfiguration(
                apiToken = "token",
                endpointUrl = "http://localhost:1/accounts/acc/ai/run/@cf/cloudflare/clef",
                model = "clef",
            )

        val classification = SystemOneIntentClassifier(conf).classifyIntent(context, "hi", arrayOf("hi"))

        assertEquals(false, classification.hasNext())
        assertEquals(0.0, classification.probability())
    }

    @Test
    fun `classifyIntent short-circuits to probability 1 when the application has a single intent, without calling the API`() {
        val greetings = Intent("app:greetings", emptyList(), description = "say hello to the user")
        val application = Application("app", listOf(greetings), setOf(Locale.ENGLISH))
        val context = IntentContext(application, Locale.ENGLISH, NlpEngineType.systemone)
        // Unreachable base URL: if the classifier called the API, the test would fail with an exception.
        val conf =
            SystemOneConfiguration(
                apiToken = "token",
                endpointUrl = "http://localhost:1/accounts/acc/ai/run/@cf/cloudflare/clef",
                model = "clef",
            )

        val classification = SystemOneIntentClassifier(conf).classifyIntent(context, "hi", arrayOf("hi"))

        assertEquals(true, classification.hasNext())
        assertEquals(greetings, classification.next())
        assertEquals(1.0, classification.probability())
        assertEquals(false, classification.hasNext())
    }

    @Test
    fun `classifyIntent throws a clear error when the application has more than 255 intents`() {
        val intents = (1..256).map { Intent("app:intent$it", emptyList(), description = "intent $it") }
        val application = Application("app", intents, setOf(Locale.ENGLISH))
        val context = IntentContext(application, Locale.ENGLISH, NlpEngineType.systemone)
        val conf =
            SystemOneConfiguration(
                apiToken = "token",
                endpointUrl = "http://localhost:1/accounts/acc/ai/run/@cf/cloudflare/clef",
                model = "clef",
            )

        assertFailsWith<IllegalStateException> {
            SystemOneIntentClassifier(conf).classifyIntent(context, "hi", arrayOf("hi"))
        }
    }

    @Test
    fun `classifyIntent throws a clear error when the Clef response is missing the intent answer`() {
        server.start()

        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "result": {
                        "model": "clef",
                        "answers": {}
                      },
                      "success": true,
                      "errors": []
                    }
                    """.trimIndent(),
                ).setResponseCode(200),
        )

        val greetings = Intent("app:greetings", emptyList(), description = "say hello to the user")
        val goodbye = Intent("app:goodbye", emptyList(), description = "say goodbye to the user")
        val application = Application("app", listOf(greetings, goodbye), setOf(Locale.ENGLISH))
        val context = IntentContext(application, Locale.ENGLISH, NlpEngineType.systemone)
        val conf =
            SystemOneConfiguration(
                apiToken = "token",
                endpointUrl = server.url("/accounts/acc/ai/run/@cf/cloudflare/clef").toString(),
                model = "clef",
            )

        assertFailsWith<IllegalStateException> {
            SystemOneIntentClassifier(conf).classifyIntent(context, "hi there", arrayOf("hi", "there"))
        }
    }
}
