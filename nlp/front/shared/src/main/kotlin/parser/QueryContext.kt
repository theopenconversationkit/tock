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

package ai.tock.nlp.front.shared.parser

import ai.tock.shared.Dice
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.util.Locale

/**
 * The context of a user query.
 */
data class QueryContext(
    /**
     * The language of the query.
     */
    val language: Locale,
    /**
     * The unique client identifier.
     */
    val clientId: String = Dice.newId(),
    /**
     * The optional client device.
     */
    val clientDevice: String? = null,
    /**
     * The dialog identifier.
     */
    val dialogId: String = Dice.newId(),
    /**
     * The reference date used to parse the query.
     */
    val referenceDate: ZonedDateTime = now(UTC),
    /**
     * The user timezone.
     */
    val referenceTimezone: ZoneId = UTC,
    /**
     * Is it a non regression test?
     */
    val test: Boolean = false,
    /**
     * Should the query be saved in db if not already present?
     */
    val registerQuery: Boolean = !test,
    /**
     * If a query is already validated in the model,
     * returns directly the result without using the NLP model if
     * [checkExistingQuery] is true
     */
    val checkExistingQuery: Boolean = true,
    /**
     * Add this query in built-in stats.
     */
    val increaseQueryCounter: Boolean = !test
)
