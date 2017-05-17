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

    fun calculateEnd(start: ZonedDateTime): ZonedDateTime {
        return when (this) {
            second -> start.plusSeconds(1).truncatedTo(SECONDS)
            minute -> start.plusMinutes(1).truncatedTo(MINUTES)
            hour -> start.plusHours(1).truncatedTo(HOURS)
            day_of_week, day -> start.plusDays(1).truncatedTo(DAYS)
            week -> start.plusWeeks(1).truncatedTo(WEEKS)
            month -> start.plusMonths(1).truncatedTo(MONTHS)
            quarter -> start.plusMonths(3).truncatedTo(MONTHS)
            year -> start.plusYears(1).truncatedTo(YEARS)
            else -> start
        }
    }

}