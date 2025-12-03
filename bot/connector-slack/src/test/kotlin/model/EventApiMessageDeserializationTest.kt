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

package ai.tock.bot.connector.slack.model

import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class EventApiMessageDeserializationTest {
    @Test
    fun `simple message can be deserialized`() {
        val json = """
            {
              "token": "SKzpkB0ZewSudSZkswR2oCFP",
              "team_id": "T4HDEzX4L",
              "api_app_id": "ADzJT4FT3",
              "event": {
                "type": "message",
                "user": "U4J3zCZ9C",
                "text": "Hello World",
                "client_msg_id": "59zfbf73-841d-4fd5-9b1b-32fbbb0a06be",
                "ts": "1538685148.000200",
                "channel": "CD5zV4RL0",
                "event_ts": "1538685148.000200",
                "channel_type": "channel"
              },
              "type": "event_callback",
              "event_id": "EvD8zV15L6",
              "event_time": 1538685148,
              "authed_users": [
                "UD64DE3z1"
              ]
            }"""
        val message: EventApiMessage = mapper.readValue(json)
        assertTrue { message is CallbackEvent }
        assertEquals("Hello World", (message as CallbackEvent).event.text)
    }

    @Test
    fun `url verification can be deserialized`() {
        val json = """
            {
                "token": "Jhj5dZrVaK7ZwHHjRyZWjbDl",
                "challenge": "3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P",
                "type": "url_verification"
            }"""
        val message: EventApiMessage = mapper.readValue(json)
        assertTrue { message is UrlVerificationEvent }
        assertEquals(
            "3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P",
            (message as UrlVerificationEvent).challenge,
        )
    }
}
