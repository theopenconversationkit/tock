/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.api.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.nlp.api.client.model.dump.ApplicationDump
import org.junit.Test
import java.util.UUID
import kotlin.test.assertTrue


/**
 *
 */
class NlpClientIntegrationTest {

    val dumpStream = NlpClient::class.java.getResourceAsStream("/dump.json")

    @Test
    fun testImportNlpDump() {
        assertTrue(TockNlpClient("http://localhost:8880").importNlpDump(dumpStream).body()!!)
    }

    @Test
    fun testImportNlpPlainDump() {
        val dump = jacksonObjectMapper()
                .findAndRegisterModules()
                .registerModule(JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue<ApplicationDump>(dumpStream)
                .run {
                    copy(
                            application = application.copy(
                                    _id = UUID.randomUUID().toString(),
                                    name = UUID.randomUUID().toString())
                    )
                }
        assertTrue(TockNlpClient("http://localhost:8880").importNlpPlainDump(dump).body()!!)
    }
}