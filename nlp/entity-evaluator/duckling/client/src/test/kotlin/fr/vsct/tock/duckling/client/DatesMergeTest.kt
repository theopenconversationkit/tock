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
import fr.vsct.tock.duckling.client.DucklingDimensions.timeDucklingDimension
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.merge.ValueDescriptor
import fr.vsct.tock.nlp.entity.date.DateEntityGrain
import fr.vsct.tock.nlp.entity.date.DateEntityValue
import fr.vsct.tock.nlp.entity.date.DateIntervalEntityValue
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import fr.vsct.tock.shared.injector
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.ZoneId
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
            "test",
            referenceTime
        )

        val tomorrow = ValueDescriptor(
            DateEntityValue(
                referenceTime.plusDays(1),
                DateEntityGrain.day
            ),
            "demain",
            false,
            12
        )

        val nextMonth = ValueDescriptor(
            DateEntityValue(
                referenceTime.plusMonths(1),
                DateEntityGrain.day
            ),
            "le mois prochain",
            false,
            12
        )

        val twoDaysAfter = ValueDescriptor(
            DateEntityValue(
                referenceTime.plusDays(2),
                DateEntityGrain.day
            ),
            "2 jours Après",
            false,
            12,
            0.8
        )

        val endAfternoon = ValueDescriptor(
            DateIntervalEntityValue(
                DateEntityValue(referenceTime.withHour(17), DateEntityGrain.hour),
                DateEntityValue(referenceTime.withHour(19), DateEntityGrain.hour)
            ),
            "en fin  d'après-midi ",
            false,
            2
        )

        val evening = ValueDescriptor(
            DateIntervalEntityValue(
                DateEntityValue(referenceTime.withHour(18), DateEntityGrain.hour),
                DateEntityValue(referenceTime.withHour(23), DateEntityGrain.hour)
            ),
            "en soirée",
            false,
            2
        )

        val tomorrowInTheEvening = DateIntervalEntityValue(
            DateEntityValue(referenceTime.plusDays(1).withHour(18), DateEntityGrain.hour),
            DateEntityValue(referenceTime.plusDays(1).withHour(23), DateEntityGrain.hour)
        )

        val tomorrowAt8 = ValueDescriptor(
            DateEntityValue(
                referenceTime.plusDays(1).withHour(20),
                DateEntityGrain.hour
            ),
            "demain à 20h",
            false,
            2
        )

        val changeDayOfMonth = ValueDescriptor(
            DateEntityValue(
                referenceTime.plusDays(1).withHour(20),
                DateEntityGrain.hour
            ),
            "le 20",
            false,
            2
        )

        val changeDayOfWeek = ValueDescriptor(
            DateEntityValue(
                referenceTime.plusDays(1).withHour(20),
                DateEntityGrain.hour
            ),
            "le jeudi",
            false,
            2
        )

        val inThreeDays =
            DateEntityValue(
                referenceTime.plusDays(3),
                DateEntityGrain.day
            )

        val parser: Parser = mockk(relaxed = true)

        init {

            every {
                parser.parse(
                    eq(Locale.FRENCH.language),
                    eq(timeDucklingDimension),
                    eq((tomorrow.value as DateEntityValue).date),
                    eq("2 jours Après")
                )
            } answers { listOf(ValueWithRange(0, 0, inThreeDays, timeDucklingDimension)) }

            every {
                parser.parse(
                    eq(Locale.FRENCH.language),
                    eq(timeDucklingDimension),
                    eq(referenceTime),
                    eq("en soirée demain")
                )
            } answers { listOf(ValueWithRange(0, 0, tomorrowInTheEvening, timeDucklingDimension)) }

            every {
                parser.parse(
                    eq(Locale.FRENCH.language),
                    eq(timeDucklingDimension),
                    eq((tomorrow.value as DateEntityValue).date.truncatedTo(ChronoUnit.DAYS)),
                    eq("en soirée")
                )
            } answers { listOf(ValueWithRange(0, 0, tomorrowInTheEvening, timeDucklingDimension)) }

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

    @Test
    fun merge_shouldReturnsTheValueOnly_whenInitialValueIsOfGreaterGrainButNewValueContainsAlsoInitialValueGrain() {
        val result = DatesMerge.merge(context, listOf(tomorrow.copy(initial = true), tomorrowAt8))
        assertEquals(tomorrowAt8.value, result?.value)
    }

    @Test
    fun merge_shouldReturnsTheValueWithTheInitialDateValueAsReference_whenInitialValueIsOfSameGrainButGrainFromNowIsGreater() {
        val result = DatesMerge.merge(context, listOf(tomorrowAt8.copy(initial = true), evening))
        assertEquals(tomorrowInTheEvening, result?.value)
    }

    @Test
    fun `mergeGrain returns non additional merge for 'en fin d'après-midi'(fr)`() {
        val r = DatesMerge.mergeGrain(Locale.FRENCH, tomorrow, endAfternoon)
        assertEquals(DatesMerge.MergeGrain(false, DateEntityGrain.day), r)
    }

    @Test
    fun `merge with change day of month returns the new day of month with the right month`() {
        val r = DatesMerge.merge(context, listOf(nextMonth.copy(initial = true), changeDayOfMonth))
        assertEquals((nextMonth.value as DateEntityValue).date.month, (r?.value as DateEntityValue).date.month)
        assertEquals(20, (r.value as DateEntityValue).date.dayOfMonth)
    }

    @Test
    fun `merge with change day of week returns the new day of week`() {
        val r = DatesMerge.merge(context, listOf(nextMonth.copy(initial = true), changeDayOfWeek))
        assertEquals(DayOfWeek.THURSDAY, (r?.value as DateEntityValue).date.dayOfWeek)
    }

    @Test
    fun `merge with change day of month returns the next month occurrence with the right month`() {
        val referenceDate = ZonedDateTime.of(2000, 1, 25, 1, 1, 1, 1, ZoneId.systemDefault())
        val context = EntityCallContextForEntity(
            EntityType("duckling:datetime"),
            Locale.FRENCH,
            NlpEngineType.opennlp,
            "test",
            referenceDate
        )
        val tomorrow = ValueDescriptor(
            DateEntityValue(
                referenceDate.plusDays(1),
                DateEntityGrain.day
            ),
            "demain",
            false,
            12
        )

        val r = DatesMerge.merge(
            context, listOf(
                tomorrow.copy(initial = true), changeDayOfMonth.copy(
                    value = DateEntityValue(
                        referenceDate.plusMonths(1).withDayOfMonth(20),
                        DateEntityGrain.day
                    )
                )
            )
        )
        assertEquals((tomorrow.value as DateEntityValue).date.month + 1, (r?.value as DateEntityValue).date.month)
        assertEquals(20, (r.value as DateEntityValue).date.dayOfMonth)
    }
}