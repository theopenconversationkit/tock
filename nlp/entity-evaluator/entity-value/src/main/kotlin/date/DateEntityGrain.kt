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

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS
import java.time.temporal.ChronoUnit.MINUTES
import java.time.temporal.ChronoUnit.MONTHS
import java.time.temporal.ChronoUnit.SECONDS
import java.time.temporal.TemporalAdjusters

/**
 *
 */
@Suppress("ktlint:standard:enum-entry-name-case")
enum class DateEntityGrain(val time: Boolean) {
    // the order is important (duckling parsing)
    timezone(false),
    unknown(false),
    second(true),
    minute(true),
    hour(true),
    day_of_week(false),
    day(false),
    week(false),
    month(false),
    quarter(false),
    year(false),
    ;

    companion object {
        fun from(s: String?): DateEntityGrain {
            try {
                return if (s == null) unknown else valueOf(s)
            } catch (e: Exception) {
                return unknown
            }
        }

        fun maxGrain(
            start: ZonedDateTime,
            end: ZonedDateTime,
        ): DateEntityGrain {
            return if (start.plusSeconds(1) >= end) {
                second
            } else if (start.truncatedTo(MINUTES).plusMinutes(1) >= end.truncatedTo(MINUTES)) {
                minute
            } else if (start.truncatedTo(HOURS).plusHours(1) >= end.truncatedTo(HOURS)) {
                hour
            } else if (start.truncatedTo(DAYS).plusDays(1) >= end.truncatedTo(DAYS)) {
                day
            } else if (start.truncatedTo(DAYS).plusWeeks(1) >= end.truncatedTo(DAYS)) {
                week
            } else if (start.truncatedTo(MONTHS).plusMonths(1) >= end.truncatedTo(MONTHS)) {
                month
            } else {
                year
            }
        }
    }

    fun truncate(date: ZonedDateTime): ZonedDateTime {
        return try {
            when (this) {
                second -> date.truncatedTo(SECONDS)
                minute -> date.truncatedTo(MINUTES)
                hour -> date.truncatedTo(HOURS)
                day_of_week, day -> date.truncatedTo(DAYS)
                // TODO depending of the timezone for the start of day
                week -> date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)).truncatedTo(DAYS)
                month -> date.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(DAYS)
                quarter -> date.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(DAYS)
                year -> date.with(TemporalAdjusters.firstDayOfYear()).truncatedTo(DAYS)
                else -> date
            }
        } catch (e: Exception) {
            // ignore
            date
        }
    }

    fun calculateInclusiveEnd(
        start: ZonedDateTime,
        zoneId: ZoneId,
    ): ZonedDateTime {
        val s = calculateEnd(start, zoneId)
        return when (this) {
            second -> truncate(s.minusSeconds(1))
            minute -> truncate(s.minusMinutes(1))
            hour -> truncate(s.minusHours(1))
            day_of_week, day -> truncate(s.minusDays(1))
            week -> truncate(s.minusWeeks(1))
            month -> truncate(s.minusMonths(1))
            quarter -> truncate(s.minusMonths(3))
            year -> truncate(s.minusYears(1))
            else -> s
        }
    }

    fun calculateEnd(
        start: ZonedDateTime,
        zoneId: ZoneId,
    ): ZonedDateTime {
        val s = start.withZoneSameInstant(zoneId)
        return when (this) {
            second -> truncate(s.plusSeconds(1))
            minute -> truncate(s.plusMinutes(1))
            hour -> truncate(s.plusHours(1))
            day_of_week, day -> truncate(s.plusDays(1))
            week -> truncate(s.plusWeeks(1))
            month -> truncate(s.plusMonths(1))
            quarter -> truncate(s.plusMonths(3))
            year -> truncate(s.plusYears(1))
            else -> s
        }
    }
}
