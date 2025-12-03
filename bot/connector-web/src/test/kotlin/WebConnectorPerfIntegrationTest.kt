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

import ai.tock.shared.Dice
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers.ofString

class WebConnectorPerfIntegrationTest {
    @Test
    fun `testWebConnectorPerf`() {
        val startTest = System.currentTimeMillis()
        val users = 100
        val userIds = (0..users).map { Dice.newId() }
        val clients = (0..users).map { HttpClient.newHttpClient() }
        (0..20).map { call ->
            (0..users)
                .map { user ->
                    val request =
                        HttpRequest.newBuilder(URI("http://localhost:8080/io/app/new_assistant/web"))
                            .POST(ofString("{\"query\":\"yo\",\"userId\":\"${userIds[user]}\",\"locale\":\"fr\"}"))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .build()

                    val start = System.currentTimeMillis()
                    clients[user].sendAsync(request, ofString()).thenApply { response: HttpResponse<String> ->
                        val end = System.currentTimeMillis()
                        println("user: $user - call: $call - code: ${response.statusCode()} duration: ${end - start} ms")
                    }
                }
                .forEach {
                    it.join()
                }
        }
        val endTest = System.currentTimeMillis()
        println("Total duration: ${endTest - startTest} ms")
        assert(endTest - startTest < 30000) {
            "Test took too long: ${endTest - startTest} ms"
        }
    }
}
