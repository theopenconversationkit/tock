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

package fr.vsct.tock.nlp.entity.date

import java.lang.Exception
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS
import java.time.temporal.ChronoUnit.MINUTES
import java.time.temporal.ChronoUnit.MONTHS
import java.time.temporal.ChronoUnit.SECONDS
import java.time.temporal.ChronoUnit.WEEKS
import java.time.temporal.ChronoUnit.YEARS

/**
 *
 */
enum class DateEntityGrain(val time: Boolean) {

    //the order is important (duckling parsing)
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
    year(false);

    companion object {
        fun from(s: String?): DateEntityGrain {
            try {
                return if (s == null) unknown else valueOf(s)
            } catch (e: Exception) {
                return unknown
            }
        }
    }

    fun truncate(date: ZonedDateTime): ZonedDateTime {
        return when (this) {
            second -> date.truncatedTo(SECONDS)
            minute -> date.truncatedTo(MINUTES)
            hour -> date.truncatedTo(HOURS)
            day_of_week, day -> date.truncatedTo(DAYS)
            week -> date.truncatedTo(WEEKS)
            month -> date.truncatedTo(MONTHS)
            quarter -> date.truncatedTo(MONTHS)
            year -> date.truncatedTo(YEARS)
            else -> date
        }
    }

    fun calculateEnd(start: ZonedDateTime): ZonedDateTime {
        return when (this) {
            second -> truncate(start.plusSeconds(1))
            minute -> truncate(start.plusMinutes(1))
            hour -> truncate(start.plusHours(1))
            day_of_week, day -> truncate(start.plusDays(1))
            week -> truncate(start.plusWeeks(1))
            month -> truncate(start.plusMonths(1))
            quarter -> truncate(start.plusMonths(3))
            year -> truncate(start.plusYears(1))
            else -> start
        }
    }

}