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

package ai.tock.nlp.front.shared

import ai.tock.nlp.front.shared.evaluation.EntityEvaluationQuery
import ai.tock.nlp.front.shared.evaluation.EntityEvaluationResult
import ai.tock.nlp.front.shared.merge.ValuesMergeQuery
import ai.tock.nlp.front.shared.merge.ValuesMergeResult
import ai.tock.nlp.front.shared.monitoring.MarkAsUnknownQuery
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.ParseResult

/**
 *
 */
interface Parser {

    /**
     * Parses sentences with NLP.
     */
    fun parse(query: ParseQuery): ParseResult

    /**
     * Evaluates entities.
     */
    fun evaluateEntities(query: EntityEvaluationQuery): EntityEvaluationResult

    /**
     * Merges entity values of same type.
     * For example "tomorrow" + "morning" = "tomorrow morning"
     */
    fun mergeValues(query: ValuesMergeQuery): ValuesMergeResult

    /**
     * Marks a sentence as unknown.
     */
    fun incrementUnknown(query: MarkAsUnknownQuery)

    /**
     * Checks parser availability.
     */
    fun healthcheck(): Boolean
}
