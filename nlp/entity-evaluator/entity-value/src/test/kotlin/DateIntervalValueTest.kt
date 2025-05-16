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

package ai.tock.nlp.entity.date

import ai.tock.nlp.entity.date.DateEntityGrain.hour
import ai.tock.nlp.entity.date.DateEntityGrain.minute
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals

/**
 *
 */
class DateIntervalValueTest {

    @Test
    fun duration_withDifferentTimeZone_shouldGiveTheRightValue() {
        val actual = ZonedDateTime.now()
        val duration = Duration.ofHours(2)
        val end = actual.plusHours(1).withZoneSameInstant(ZoneId.of("America/Phoenix"))
        assertEquals(
            duration,
            DateIntervalEntityValue(
                DateEntityValue(actual, hour),
                DateEntityValue(end, hour)
            ).duration()
        )
    }

    @Test
    fun testEnd() {
        val zoneId = ZoneId.of("America/Phoenix")
        val actual = hour.truncate(ZonedDateTime.now())
        val end = actual.plusHours(1).withZoneSameInstant(zoneId)
        assertEquals(
            end.plusHours(1),
            DateIntervalEntityValue(
                DateEntityValue(actual, hour),
                DateEntityValue(end, hour)
            ).end(zoneId)
        )
    }

    @Test
    fun testInclusiveEnd() {
        val zoneId = ZoneId.of("America/Phoenix")
        val actual = hour.truncate(ZonedDateTime.now())
        val end = actual.plusHours(1).withZoneSameInstant(zoneId)
        assertEquals(
            end,
            DateIntervalEntityValue(
                DateEntityValue(actual, hour),
                DateEntityValue(end, hour)
            ).inclusiveEnd(zoneId)
        )
    }

    @Test
    fun testInclusiveEndWithMinute() {
        val zoneId = ZoneId.of("America/Phoenix")
        val actual = hour.truncate(ZonedDateTime.now())
        val end = actual.plusHours(1).plusHours(1).withZoneSameInstant(zoneId)
        assertEquals(
            end,
            DateIntervalEntityValue(
                DateEntityValue(actual, hour),
                DateEntityValue(end, minute)
            ).inclusiveEnd(zoneId)
        )
    }
}
