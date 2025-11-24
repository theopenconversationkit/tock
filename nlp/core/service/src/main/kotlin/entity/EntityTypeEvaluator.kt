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

package ai.tock.nlp.core.service.entity

import ai.tock.nlp.core.merge.ValueDescriptor
import ai.tock.nlp.model.EntityCallContextForEntity

/**
 * Evaluate a text for the specified entity.
 */
interface EntityTypeEvaluator {
    /**
     * Evaluates a text from the given entity context.
     *
     * @param context to know the entity to evaluate
     * @param text the text to evaluate
     * @return the evaluation result - with null [EvaluationResult.value] if no value found
     */
    fun evaluate(
        context: EntityCallContextForEntity,
        text: String,
    ): EvaluationResult

    /**
     * Merge two or more [ValueDescriptor].
     * Returns null if the merge is not applicable.
     */
    fun merge(
        context: EntityCallContextForEntity,
        values: List<ValueDescriptor>,
    ): ValueDescriptor? = null
}
