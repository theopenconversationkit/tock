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

import ai.tock.nlp.entity.Value
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Both [DateEntityValue] && [DateIntervalEntityValue] can be seen as date range.
 */
interface DateEntityRange : Value {

    /**
     * Returns the start inclusive date.
     * Ie
     * - 9h -> 9h
     * - from 9h to 10h -> 9h
     */
    fun start(): ZonedDateTime

    /**
     * Returns the end inclusive date.
     * - from 9h to 10h -> 10h
     * - from 9h to 10h00 -> 10h
     * - 9h -> 10h
     */
    fun inclusiveEnd(): ZonedDateTime = inclusiveEnd(ZoneId.systemDefault())

    /**
     * Returns the end inclusive date.
     * Ie:
     * - from 9h to 10h -> 10h
     * - from 9h to 10h00 -> 10h
     * - 9h -> 10h
     * - this morning (4h to 12h) -> 12h
     */
    fun inclusiveEnd(zoneId: ZoneId): ZonedDateTime = end(zoneId)

    /**
     * Returns the end exclusive date.
     * - from 9h to 10h -> 11h
     * - from 9h to 10h00 -> 10h01
     * - 9h -> 10h
     * - this morning (4h to 12h) -> 13h
     */
    fun end(): ZonedDateTime = end(ZoneId.systemDefault())

    /**
     * Returns the end exclusive date.
     * Ie:
     * - from 9h to 10h -> 11h
     * - from 9h to 10h00 -> 10h01
     * - 9h -> 10h
     */
    fun end(zoneId: ZoneId): ZonedDateTime

    fun duration(): Duration
}
