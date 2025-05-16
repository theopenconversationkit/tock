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

package ai.tock.duckling.client

import ai.tock.duckling.client.DucklingDimensions.TIME_DIMENSION
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.merge.ValueDescriptor
import ai.tock.nlp.entity.date.DateEntityGrain
import ai.tock.nlp.entity.date.DateEntityValue
import ai.tock.nlp.entity.date.DateIntervalEntityValue
import ai.tock.nlp.model.EntityCallContextForEntity
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
                referenceTime.plusDays(1),
                DateEntityGrain.hour
            ),
            "le 20",
            false,
            2
        )

        val changeDayOfWeekMorning = ValueDescriptor(
            DateEntityValue(
                referenceTime.plusDays(1).withHour(10),
                DateEntityGrain.hour
            ),
            "le jeudi à 10h",
            false,
            2
        )

        val changeDayOfWeek = ValueDescriptor(
            DateEntityValue(
                referenceTime.plusDays(1),
                DateEntityGrain.hour
            ),
            "le jeudi",
            false,
            2
        )

        val changeDayOfWeek2 = ValueDescriptor(
            DateEntityValue(
                referenceTime.plusDays(2),
                DateEntityGrain.hour
            ),
            "vendredi",
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
                    eq(TIME_DIMENSION),
                    eq((tomorrow.value as DateEntityValue).date),
                    eq("2 jours Après")
                )
            } answers { listOf(ValueWithRange(0, 0, inThreeDays, TIME_DIMENSION)) }

            every {
                parser.parse(
                    eq(Locale.FRENCH.language),
                    eq(TIME_DIMENSION),
                    eq(referenceTime),
                    eq("en soirée demain")
                )
            } answers { listOf(ValueWithRange(0, 0, tomorrowInTheEvening, TIME_DIMENSION)) }

            every {
                parser.parse(
                    eq(Locale.FRENCH.language),
                    eq(TIME_DIMENSION),
                    eq((tomorrow.value as DateEntityValue).date.truncatedTo(ChronoUnit.DAYS)),
                    eq("en soirée")
                )
            } answers { listOf(ValueWithRange(0, 0, tomorrowInTheEvening, TIME_DIMENSION)) }

            injector.inject(
                Kodein {
                    bind<Parser>() with provider { parser }
                }
            )
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
    fun `mergeGrain returns non additional merge for en fin d'après-midi (fr)`() {
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
    fun `le 20 02 does change the month`() {
        val d2002 = ValueDescriptor(
            DateEntityValue(
                referenceTime.withMonth(2).withDayOfMonth(20),
                DateEntityGrain.day
            ),
            "le 20 02",
            false,
            2
        )
        val r = DatesMerge.merge(context, listOf(tomorrow.copy(initial = true), d2002))
        assertEquals(Month.FEBRUARY, (r?.value as DateEntityValue).date.month)
        assertEquals(20, (r.value as DateEntityValue).date.dayOfMonth)
    }

    @Test
    fun `merge with change day of week returns the new day of week`() {
        val r = DatesMerge.merge(context, listOf(nextMonth.copy(initial = true), changeDayOfWeek))
        val date = (r?.value as DateEntityValue).date
        assertEquals(DayOfWeek.THURSDAY, date.dayOfWeek)
        assertEquals(date.truncatedTo(ChronoUnit.DAYS), date.truncatedTo(ChronoUnit.DAYS))
    }

    @Test
    fun `merge with change day of week of a previously hour based date returns the new day of week truncated to day`() {
        val r = DatesMerge.merge(context, listOf(changeDayOfWeekMorning.copy(initial = true), changeDayOfWeek))
        val date = (r?.value as DateEntityValue).date
        assertEquals(DayOfWeek.THURSDAY, date.dayOfWeek)
        assertEquals(date.truncatedTo(ChronoUnit.DAYS), date.truncatedTo(ChronoUnit.DAYS))
    }

    @Test
    fun `merge with change day of week (simplified version) returns the new day of week`() {
        val r = DatesMerge.merge(context, listOf(nextMonth.copy(initial = true), changeDayOfWeek2))
        val date = (r?.value as DateEntityValue).date
        assertEquals(DayOfWeek.FRIDAY, date.dayOfWeek)
        assertEquals(date.truncatedTo(ChronoUnit.DAYS), date.truncatedTo(ChronoUnit.DAYS))
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
            context,
            listOf(
                tomorrow.copy(initial = true),
                changeDayOfMonth.copy(
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

    @Test
    fun `entre 9h et 10h is a french change hour pattern`() {
        assertTrue(DatesMerge.isChangeHourPattern("entre 9h et 10h"))
    }
}
