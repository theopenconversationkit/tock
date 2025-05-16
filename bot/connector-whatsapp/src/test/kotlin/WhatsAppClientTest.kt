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

package ai.tock.bot.connector.whatsapp

import ai.tock.bot.connector.whatsapp.WhatsAppClient.LoginResponse
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals

/**
 *
 */
class WhatsAppClientTest {

    @Test
    fun `LoginResponse is deserialized`() {
        val client = WhatsAppClient("http://test", "login", "password")
        val response = """{
                "users": [{
                    "token": "eyJhbGciOHlXVCJ9.eyJ1c2VyIjoNTIzMDE2Nn0.mEoF0COaO00Z1cANo",
                    "expires_after": "2018-03-01 15:29:26+00:00"
                }]
            }"""
        val r: LoginResponse = client.clientMapper.readValue(response)
        assertEquals(
            "eyJhbGciOHlXVCJ9.eyJ1c2VyIjoNTIzMDE2Nn0.mEoF0COaO00Z1cANo",
            r.users.first().token
        )
        assertEquals(
            OffsetDateTime.of(LocalDateTime.parse("2018-03-01T15:29:26"), ZoneOffset.UTC),
            r.users.first().expiresAfter
        )
    }
}
