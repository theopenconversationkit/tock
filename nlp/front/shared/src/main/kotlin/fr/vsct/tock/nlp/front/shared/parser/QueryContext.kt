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

package fr.vsct.tock.nlp.front.shared.parser

import fr.vsct.tock.shared.Dice
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.util.Locale

/**
 *
 */
data class QueryContext(val language: Locale,
                        val clientId: String = Dice.newId(),
                        val clientDevice: String? = null,
                        val dialogId: String = Dice.newId(),
                        val referenceDate: ZonedDateTime = now(UTC),
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
) {
}