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

package ai.tock.genai.orchestratorclient.services.impl

import ai.tock.genai.orchestratorclient.requests.RAGRequest
import ai.tock.genai.orchestratorclient.responses.RAGResponse
import ai.tock.genai.orchestratorclient.responses.TokenEvent
import ai.tock.genai.orchestratorclient.services.RAGStreamListener
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Manual integration test for RAG streaming.
 *
 * To run this test:
 * 1. Start the Python server: cd gen-ai/orchestrator-server && poetry run uvicorn gen_ai_orchestrator.main:app --reload
 * 2. Run: mvn exec:java -pl gen-ai/orchestrator-client -Dexec.mainClass="ai.tock.genai.orchestratorclient.services.impl.RAGServiceStreamingIntegrationTestKt"
 */
class RAGServiceStreamingIntegrationTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("RAG Streaming Integration Test")
            println("================================\n")

            val service = RAGServiceImpl()
            val tokenList = mutableListOf<String>()
            val finalResponse = AtomicReference<RAGResponse?>(null)
            val error = AtomicReference<Throwable?>(null)
            val completed = AtomicBoolean(false)

            // Load RAGRequest from JSON file in test resources
            val mapper =
                jacksonObjectMapper()
                    .findAndRegisterModules()
                    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

            val resourceUrl =
                RAGServiceStreamingIntegrationTest::class.java
                    .getResource("/test_rag.json")
            if (resourceUrl == null) {
                println("ERROR: test_rag.json not found in resources!")
                return
            }

            val request: RAGRequest = mapper.readValue(resourceUrl)
            println("Loaded RAGRequest from test_rag.json")
            println("Question: ${request.questionAnsweringPrompt?.inputs?.get("question")}")
            println("Index: ${request.documentIndexName}\n")

            println("Starting RAG stream...")

            service.ragStream(
                query = request,
                debug = true,
                listener =
                    object : RAGStreamListener {
                        override fun onToken(token: TokenEvent) {
                            print(token.content)
                            tokenList.add(token.content)
                        }

                        override fun onAnswer(response: RAGResponse) {
                            println("\n\n✓ ANSWER received")
                            println("  Status: ${response.answer.status}")
                            println("  Confidence: ${response.answer.confidenceScore}")
                            println("  Footnotes: ${response.footnotes.size}")
                            finalResponse.set(response)
                        }

                        override fun onError(err: Throwable) {
                            println("\n✗ ERROR: ${err.message}")
                            err.printStackTrace()
                            error.set(err)
                        }

                        override fun onComplete() {
                            println("✓ Stream closed")
                            completed.set(true)
                        }
                    },
            )

            println("\n================================")
            println("Test Results:")
            println("  Tokens received: ${tokenList.size}")
            println("  Full text (${tokenList.joinToString("").length} chars):")
            println("    ${tokenList.joinToString("")}")
            println("  Status: ${finalResponse.get()?.answer?.status}")
            if (error.get() != null) {
                println("  Error: ${error.get()?.message}")
            }
            println("  Completed: ${completed.get()}")
        }
    }
}
