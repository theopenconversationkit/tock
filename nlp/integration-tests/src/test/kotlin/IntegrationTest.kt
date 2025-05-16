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

package ai.tock.nlp.integration

import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.entity.date.DateEntityGrain
import ai.tock.nlp.entity.date.DateEntityValue
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.ParsedEntityValue
import ai.tock.nlp.front.shared.parser.QueryContext
import ai.tock.shared.defaultNamespace
import ai.tock.shared.defaultZoneId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class IntegrationTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeClass() {
            IntegrationConfiguration.init(NlpEngineType.opennlp)
        }
    }

    @Test
    fun testOpenNlpSimpleRequest() {
        val result = FrontClient.parse(
            ParseQuery(
                listOf("I want to go to Paris tomorrow"),
                defaultNamespace,
                "test",
                QueryContext(Locale.ENGLISH, "clientTest")
            )
        )
        println(result)
        assertEquals("travel", result.intent)
        assertEquals(2, result.entities.size)
        assertEquals(
            ParsedEntityValue(
                16,
                21,
                Entity(EntityType("$defaultNamespace:locality"), "locality"),
                null,
                probability = 0.30666386016073854
            ),
            result.firstValue("locality")
        )
        assertEquals(
            ParsedEntityValue(
                22, 30, Entity(EntityType("duckling:datetime"), "datetime"),
                DateEntityValue(
                    ZonedDateTime.now().withZoneSameInstant(defaultZoneId).plusDays(1).withHour(0).withMinute(0).withSecond(
                        0
                    ).withNano(0).withFixedOffsetZone(),
                    DateEntityGrain.day
                ),
                true, probability = 0.6447195532270447, mergeSupport = true
            ),
            result.firstValue("datetime")
        )
    }
}
