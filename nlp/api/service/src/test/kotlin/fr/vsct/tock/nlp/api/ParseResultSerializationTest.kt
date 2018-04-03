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

package fr.vsct.tock.nlp.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.entity.date.DateEntityGrain
import fr.vsct.tock.nlp.entity.date.DateEntityValue
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.jackson.mapper
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
        val parseResult = ParseResult(
            "test",
            "namespace",
            defaultLocale,
            listOf(
                ParsedEntityValue(
                    0,
                    1,
                    Entity(EntityType("type"), "role"),
                    DateEntityValue(ZonedDateTime.of(2017, 4, 1, 0, 0, 0, 0, ZoneId.of("UTC")), DateEntityGrain.day)
                )
            ),
            1.0,
            1.0,
            "sentence",
            mapOf("test2" to 2.0)
        )
        val s = mapper.writeValueAsString(parseResult)
        assertEquals(
            """{"intent":"test","intentNamespace":"namespace","language":"$defaultLocale","entities":[{"start":0,"end":1,"entity":{"entityType":{"name":"type","subEntities":[]},"role":"role"},"value":{"@type":"dateEntity","date":"2017-04-01T00:00:00Z[UTC]","grain":"day"},"evaluated":false,"subEntities":[],"probability":1.0,"mergeSupport":false}],"intentProbability":1.0,"entitiesProbability":1.0,"retainedQuery":"sentence","otherIntentsProbabilities":{"test2":2.0}}""",
            s
        )

        assertEquals(parseResult, mapper.readValue(s))
    }
}