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

package ai.tock.nlp.api.client

import ai.tock.nlp.api.client.model.NlpLogCountQuery
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpQueryContext
import ai.tock.nlp.api.client.model.dump.ApplicationDump
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class NlpClientIntegrationTest {
    val dumpStream = NlpClient::class.java.getResourceAsStream("/dump.json")
    val applicationNamespace = "test"
    val applicationName = "test"
    val unknownApplicationName = "unknown"

    @Test
    fun testImportNlpDump() {
        assertTrue(TockNlpClient("http://localhost:8880").importNlpDump(dumpStream))
    }

    @Test
    fun testImportNlpPlainDump() {
        val dump =
            jacksonObjectMapper()
                .findAndRegisterModules()
                .registerModule(JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue<ApplicationDump>(dumpStream)
                .run {
                    copy(
                        application =
                            application.copy(
                                _id = UUID.randomUUID().toString(),
                                name = UUID.randomUUID().toString(),
                            ),
                    )
                }
        assertTrue(TockNlpClient("http://localhost:8880").importNlpPlainDump(dump))
    }

    @Test
    fun testGetIntentsByNamespaceAndName() {
        assertEquals(
            2,
            TockNlpClient("http://localhost:8880").getIntentsByNamespaceAndName(
                applicationNamespace,
                applicationName,
            )!!.size,
        )
    }

    @Test
    fun testGetIntentsByNamespaceAndNameWithUnknownApplicationName() {
        assertEquals(
            0,
            TockNlpClient("http://localhost:8880").getIntentsByNamespaceAndName(
                applicationNamespace,
                unknownApplicationName,
            )!!.size,
        )
    }

    @Test
    fun testBotOpenData() {
        val client = TockNlpClient("http://localhost:8888")
        for (i in 0..10000) {
            client.parse(
                NlpQuery(
                    listOf("Bonjour"),
                    "app",
                    "bot_open_data",
                    NlpQueryContext(Locale.FRENCH),
                ),
            )
        }
    }

    @Test
    fun testLogsCount() {
        assertEquals(
            2,
            TockNlpClient("http://localhost:8888").logsCount(
                NlpLogCountQuery(
                    applicationNamespace,
                    applicationName,
                    Locale.FRENCH,
                    size = Integer.MAX_VALUE,
                ),
            )!!.size,
        )
    }
}
