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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests are disabled because they call the real, billed Cloudflare Workers AI API.
 * Set a real endpoint URL / API token (e.g. via `tock_systemone_endpoint_url` / `tock_systemone_api_token`
 * system properties or environment variables) and remove @Disabled to really execute it.
 */
class SystemOneClientIntegrationTest {
    @Disabled
    @Test
    fun testRunChoiceQuestion() {
        val client = SystemOneClient(SystemOneConfiguration())
        val result =
            client.run(
                SystemOneClient.RunRequest(
                    model = "clef-flash",
                    state = "I want to book a train from Paris to Marseille tomorrow at 6pm",
                    questions =
                        mapOf(
                            "intent" to
                                SystemOneClient.ChoiceQuestion(
                                    instructions = "Select the intent that best matches the user message.",
                                    criteria =
                                        mapOf(
                                            "search_by_od" to "search a train between two cities",
                                            "greetings" to "greet the user",
                                        ),
                                ),
                        ),
                ),
            )
        println(result)
        assertEquals("search_by_od", result.answers["intent"]?.choice)
    }

    @Test
    @Disabled
    fun testHealthcheck() {
        val client = SystemOneClient(SystemOneConfiguration())
        assertEquals(true, client.healthcheck())
    }
}
