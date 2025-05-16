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

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime.now
import java.time.ZonedDateTime.parse
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class DucklingClientIntegrationTest {

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    }

    @Test
    fun testSimpleCall() {
        val result = DucklingClient.parse("fr", listOf("time"), now(), systemDefault(), "10 août 2055")
        println(result)
        assertEquals(
            LocalDateTime.of(2055, Month.AUGUST, 10, 0, 0).atZone(systemDefault()).withFixedOffsetZone(),
            parse(result!![0][":value"][":values"][0][":value"].string(), formatter)
        )
    }

    @Test
    fun testSimpleNlCall() {
        val result = DucklingClient.parse("nl", listOf("time"), now(), systemDefault(), "08/10/2055")
        println(result)
        assertEquals(
            LocalDateTime.of(2055, Month.AUGUST, 10, 0, 0).atZone(systemDefault()).withFixedOffsetZone(),
            parse(result!![0][":value"][":values"][0][":value"].string(), formatter)
        )
    }

    @Test
    fun testCallWithSpecialQuote() {
        val result = DucklingClient.parse("fr", listOf("time"), now(), systemDefault(), "Aujourd’hui")
        println(result)
        assertEquals(
            LocalDateTime.now().atZone(systemDefault()).withFixedOffsetZone().truncatedTo(ChronoUnit.DAYS),
            parse(result!![0][":value"][":values"][0][":value"].string(), formatter)
        )
    }

    @Test
    fun testCallWithReferenceDate() {
        val referenceDate = now()
        val result = DucklingClient.parse("fr", listOf("time"), referenceDate, systemDefault(), "dans 1h")
        println(result)
        assertEquals(
            referenceDate.plusHours(1).withSecond(0).withNano(0).withFixedOffsetZone(),
            parse(result!![0][":value"][":values"][0][":value"].string(), formatter)
        )
    }

    @Test
    fun testIntervalDate() {
        val result =
            DucklingClient.parse("fr", listOf("time"), now(), systemDefault(), "du samedi 3 au dimanche 4 septembre")
        println(result)
        assertEquals(3, parse(result!![0][":value"][":from"][":value"].string(), formatter).dayOfMonth)
    }

    @Test
    fun testIntervalDate2() {
        val result =
            DucklingClient.parse("fr", listOf("time"), now(), systemDefault(), "du 3 au 4 septembre")
        println(result)
        assertEquals(3, parse(result!![0][":value"][":from"][":value"].string(), formatter).dayOfMonth)
    }

    @Test
    fun testIntervalDateWithWeekEnd() {
        val result =
            DucklingClient.parse("fr", listOf("time"), now(), systemDefault(), "le we du 3 au 4")
        println(result)
        assertEquals(3, parse(result!![0][":value"][":from"][":value"].string(), formatter).dayOfMonth)
    }

    @Test
    fun testCallWithDifferentTimezone() {
        val zoneId = ZoneId.of("America/New_York")
        val now = now()
        val result = DucklingClient.parse("fr", listOf("time"), now, zoneId, "dans 1h")
        println(result)
        assertEquals(
            now.withZoneSameInstant(zoneId).plusHours(1).toLocalDateTime().truncatedTo(ChronoUnit.MINUTES),
            parse(result!![0][":value"][":values"][0][":value"].string(), formatter).toLocalDateTime()
        )
    }

    @Test
    fun testCallWithTruncatedDay() {
        val now = now().plusDays(1)
        val result =
            DucklingClient.parse("fr", listOf("time"), now.truncatedTo(ChronoUnit.DAYS), systemDefault(), "le soir")
        println(result)
        assertEquals(
            now.withHour(18).toLocalDateTime().truncatedTo(ChronoUnit.HOURS),
            parse(result!![0][":value"][":from"][":value"].string(), formatter).toLocalDateTime()
        )
    }

    @Test
    fun testSameDay() {
        val now = now().plusDays(1)
        val result =
            DucklingClient.parse("fr", listOf("time"), now, systemDefault(), "le même jour")
        println(result)
        assertEquals(
            now.withFixedOffsetZone().truncatedTo(ChronoUnit.DAYS),
            parse(result!![0][":value"][":values"][0][":value"].string(), formatter)
        )
    }

    @Test
    fun testHealthcheck() {
        assertTrue { DucklingClient.healthcheck() }
    }
}
