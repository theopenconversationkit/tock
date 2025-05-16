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

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Date interval definition.
 */
data class DateIntervalEntityValue(
    val date: DateEntityValue,
    val toDate: DateEntityValue
) : DateEntityRange {

    override fun start(): ZonedDateTime {
        return date.date
    }

    override fun inclusiveEnd(zoneId: ZoneId): ZonedDateTime {
        return toDate.grain.calculateInclusiveEnd(toDate.date, zoneId)
    }

    override fun end(zoneId: ZoneId): ZonedDateTime {
        return toDate.grain.calculateEnd(toDate.date, zoneId)
    }

    override fun duration(): Duration = Duration.between(date.grain.truncate(start()), toDate.grain.truncate(end(start().zone)))
}
