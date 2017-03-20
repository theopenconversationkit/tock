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

import org.junit.Test
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
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
        val result = DucklingClient.parse("fr", listOf("time"), ZonedDateTime.now(), "10 août 2055")
        println(result)
        assertEquals(LocalDateTime.of(2055, Month.AUGUST, 10, 0, 0).atZone(ZoneId.systemDefault()).withFixedOffsetZone(), ZonedDateTime.parse(result[0][":value"][":values"][0][":value"].string(), formatter))
    }

    @Test
    fun testCallWithReferenceDate() {
        val referenceDate = ZonedDateTime.now()
        val result = DucklingClient.parse("fr", listOf("time"), referenceDate, "dans 1h")
        println(result)
        assertEquals(referenceDate.plusHours(1).withSecond(0).withNano(0).withFixedOffsetZone(), ZonedDateTime.parse(result[0][":value"][":values"][0][":value"].string(), formatter))
    }

    @Test
    fun testIntervalDate() {
        val result = DucklingClient.parse("fr", listOf("time"), ZonedDateTime.now(), "du samedi 3 au dimanche 4 septembre")
        println(result)
        assertEquals(3, ZonedDateTime.parse(result[0][":value"][":from"][":value"].string(), formatter).dayOfMonth)
    }

    @Test
    fun testHealthcheck() {
        assertTrue { DucklingClient.healthcheck() }
    }
}