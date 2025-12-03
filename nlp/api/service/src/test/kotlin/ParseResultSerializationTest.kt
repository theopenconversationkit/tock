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

package ai.tock.nlp.api

import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.entity.date.DateEntityGrain
import ai.tock.nlp.entity.date.DateEntityValue
import ai.tock.nlp.front.shared.parser.ParseResult
import ai.tock.nlp.front.shared.parser.ParsedEntityValue
import ai.tock.shared.defaultLocale
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals

/**
 *
 */
class ParseResultSerializationTest {
    @BeforeEach
    fun before() {
        mapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
    }

    @AfterEach
    fun after() {
        mapper.disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
    }

    @Test
    fun testEntityValueDeserializationAndSerialization() {
        val parseResult =
            ParseResult(
                "test",
                "namespace",
                defaultLocale,
                listOf(
                    ParsedEntityValue(
                        0,
                        1,
                        Entity(EntityType("type"), "role"),
                        DateEntityValue(ZonedDateTime.of(2017, 4, 1, 0, 0, 0, 0, ZoneId.of("Z")), DateEntityGrain.day),
                    ),
                ),
                emptyList(),
                1.0,
                1.0,
                "sentence",
                mapOf("test2" to 2.0),
            )
        val s = mapper.writeValueAsString(parseResult)
        @Suppress("ktlint:standard:max-line-length")
        assertEquals(
            """{"intent":"test","intentNamespace":"namespace","language":"$defaultLocale","entities":[{"start":0,"end":1,"entity":{"entityType":{"name":"type","subEntities":[],"dictionary":false,"obfuscated":false},"role":"role"},"value":{"@type":"dateEntity","date":"2017-04-01T00:00:00Z","grain":"day"},"evaluated":false,"subEntities":[],"probability":1.0,"mergeSupport":false}],"notRetainedEntities":[],"intentProbability":1.0,"entitiesProbability":1.0,"retainedQuery":"sentence","otherIntentsProbabilities":{"test2":2.0},"originalIntentsProbabilities":{}}""",
            s,
        )

        assertEquals(parseResult, mapper.readValue(s))
    }
}
