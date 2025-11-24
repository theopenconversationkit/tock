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

package ai.tock.nlp.front.ioc

import ai.tock.nlp.front.service.alexa.AlexaCodecService
import ai.tock.nlp.front.shared.codec.alexa.AlexaFilter
import ai.tock.shared.defaultLocale
import ai.tock.shared.jackson.mapper
import ai.tock.shared.property
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.io.File
import java.util.Locale

/**
 *
 */
class FrontIocIntegrationTest {
    val invocationName = property("test_application_name", "")
    val applicationId = property("test_app_id", "")
    val rootFile = File(property("test_export_dir", ""))
    val localeToExport = Locale.forLanguageTag(property("test_locale", defaultLocale.toLanguageTag()))

    @BeforeEach
    fun before() {
        FrontIoc.setup()
    }

    @Test
    fun generateAlexaSchema() {
        val filterFile = File(rootFile, "alexa.json")
        val export =
            AlexaCodecService.exportIntentsSchema(
                invocationName,
                applicationId.toId(),
                localeToExport,
                if (filterFile.exists()) {
                    mapper.readValue(filterFile, AlexaFilter::class.java)
                } else {
                    null
                },
            )
        mapper.writeValue(File(rootFile, "schema.json"), export)
    }
}
