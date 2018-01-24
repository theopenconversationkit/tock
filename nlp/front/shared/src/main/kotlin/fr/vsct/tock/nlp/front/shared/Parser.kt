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

package fr.vsct.tock.nlp.front.shared

import fr.vsct.tock.nlp.front.shared.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.front.shared.merge.ValuesMergeResult
import fr.vsct.tock.nlp.front.shared.parser.ParseIntentEntitiesQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.evaluation.EntityEvaluationQuery
import fr.vsct.tock.nlp.front.shared.evaluation.EntityEvaluationResult

/**
 *
 */
interface Parser {

    /**
     * Parse sentences with NLP.
     */
    fun parse(query: ParseQuery): ParseResult

    /**
     * Parse entities for a specified intent.
     * This is useful when a result is expected and you just need to know the entities.
     */
    fun parseIntentEntities(query: ParseIntentEntitiesQuery): ParseResult

    /**
     * Evaluate entities.
     */
    fun evaluateEntities(query: EntityEvaluationQuery): EntityEvaluationResult

    /**
     * Merge entity values of same type.
     * For example "tomorrow" + "morning" = "tomorrow morning"
     */
    fun mergeValues(query: ValuesMergeQuery): ValuesMergeResult

    /**
     * Check parser availability.
     */
    fun healthcheck(): Boolean

}