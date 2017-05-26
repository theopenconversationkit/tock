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

package fr.vsct.tock.duckling.client

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import fr.vsct.tock.duckling.client.DucklingDimensions.timeDucklingDimension
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.merge.ValueDescriptor
import fr.vsct.tock.nlp.entity.date.DateEntityGrain
import fr.vsct.tock.nlp.entity.date.DateEntityValue
import fr.vsct.tock.nlp.entity.date.DateIntervalEntityValue
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import fr.vsct.tock.shared.injector
import org.junit.Test
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
internal class DatesMergeTest {

    companion object {
        val referenceTime: ZonedDateTime = ZonedDateTime.now()

        val context = EntityCallContextForEntity(
                EntityType("duckling:datetime"),
                Locale.FRENCH,
                NlpEngineType.opennlp,
                referenceTime)

        val tomorrow = ValueDescriptor(
                DateEntityValue(
                        referenceTime.plusDays(1),
                        DateEntityGrain.day
                ),
                "demain",
                false,
                12)

        val twoDaysAfter = ValueDescriptor(
                DateEntityValue(
                        referenceTime.plusDays(2),
                        DateEntityGrain.day
                ),
                "2 jours après",
                false,
                12,
                0.8)

        val evening = ValueDescriptor(
                DateIntervalEntityValue(
                        DateEntityValue(referenceTime.withHour(18), DateEntityGrain.hour),
                        DateEntityValue(referenceTime.withHour(23), DateEntityGrain.hour)
                ),
                "en soirée",
                false,
                2)

        val tomorrowInTheEvening = DateIntervalEntityValue(
                DateEntityValue(referenceTime.plusDays(1).withHour(18), DateEntityGrain.hour),
                DateEntityValue(referenceTime.plusDays(1).withHour(23), DateEntityGrain.hour)
        )

        val inThreeDays =
                DateEntityValue(
                        referenceTime.plusDays(3),
                        DateEntityGrain.day
                )

        val parser: Parser = mock {
            on {
                parse(
                        eq(Locale.FRENCH.language),
                        eq(timeDucklingDimension),
                        eq((tomorrow.value as DateEntityValue).date),
                        eq("2 jours après")
                )
            } doReturn (listOf(
                    ValueWithRange(0, 0, inThreeDays, timeDucklingDimension)))

            on {
                parse(
                        eq(Locale.FRENCH.language),
                        eq(timeDucklingDimension),
                        eq(referenceTime),
                        eq("en soirée demain")
                )
            } doReturn (listOf(
                    ValueWithRange(0, 0, tomorrowInTheEvening, timeDucklingDimension)))


            on {
                parse(
                        eq(Locale.FRENCH.language),
                        eq(timeDucklingDimension),
                        eq((tomorrow.value as DateEntityValue).date.truncatedTo(ChronoUnit.DAYS)),
                        eq("en soirée")
                )
            } doReturn (listOf(
                    ValueWithRange(0, 0, tomorrowInTheEvening, timeDucklingDimension)))

        }


        init {
            injector.inject(Kodein {
                bind<Parser>() with provider { parser }
            })
        }
    }

    @Test
    fun merge_shouldReturnsTheConcatenatedValue_withTwoValuesOfDifferentGrain() {
        val result = DatesMerge.merge(context, listOf(tomorrow, evening))
        assertEquals("en soirée demain", result?.content)
    }

    @Test
    fun merge_shouldReturnsTheBetterProbability_withTwoValuesOfSameGrain() {
        val result = DatesMerge.merge(context, listOf(twoDaysAfter, tomorrow))
        assertEquals("demain", result?.content)
    }

    @Test
    fun merge_shouldReturnsTheValueWithTheInitialDateValueAsReference_whenInitialValueIsOfGreaterGrain() {
        val result = DatesMerge.merge(context, listOf(tomorrow.copy(initial = true), evening))
        assertEquals("en soirée", result?.content)
        assertEquals(tomorrowInTheEvening, result?.value)
    }

    @Test
    fun merge_shouldReturnsTheValueWithAdditionalValue_whenNewValueMatchesAdditionalRegex() {
        val result = DatesMerge.merge(context, listOf(tomorrow.copy(initial = true), twoDaysAfter))
        assertEquals(inThreeDays, result?.value)
    }
}