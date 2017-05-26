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

package fr.vsct.tock.nlp.integration

import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.NlpEngineType.Companion.opennlp
import fr.vsct.tock.nlp.entity.date.DateEntityGrain
import fr.vsct.tock.nlp.entity.date.DateEntityValue
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue
import fr.vsct.tock.nlp.front.shared.parser.QueryContext
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import org.junit.BeforeClass
import org.junit.Test
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class IntegrationTest {

    companion object {
        @BeforeClass @JvmStatic
        fun beforeClass() {
            IntegrationConfiguration.init()
        }
    }

    @Test
    fun testOpenNlpSimpleRequest() {
        val result = FrontClient.parse(ParseQuery(listOf("I want to go to Paris tomorrow"), "vsc", "test", QueryContext(Locale.ENGLISH, "clientTest", engineType = opennlp)))
        println(result)
        assertEquals("travel", result.intent)
        assertEquals(2, result.entities.size)
        assertEquals(ParsedEntityValue(16, 21, Entity(EntityType("vsc:locality"), "locality"), null), result.firstValue("locality"))
        assertEquals(ParsedEntityValue(22, 30, Entity(EntityType("duckling:datetime"), "datetime"),
                DateEntityValue(
                        ZonedDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).withFixedOffsetZone(),
                        DateEntityGrain.day), true), result.firstValue("datetime"))


    }

}