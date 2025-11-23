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

package ai.tock.nlp.api.client.model

import java.util.Locale

/**
 * The result of the [NlpQuery].
 */
data class NlpResult(
    /**
     * The intent selected.
     */
    val intent: String,
    /**
     * the namespace of the selected intent.
     */
    val intentNamespace: String,
    /**
     * The language selected.
     */
    val language: Locale,
    /**
     * The selected entities.
     */
    val entities: List<NlpEntityValue> = emptyList(),
    /**
     * The entities found but not retained.
     */
    val notRetainedEntities: List<NlpEntityValue> = emptyList(),
    /**
     * The intent evaluated probability.
     */
    val intentProbability: Double = 1.0,
    /**
     * The average entity evaluation probability.
     */
    val entitiesProbability: Double = 1.0,
    /**
     * The analysed query.
     */
    val retainedQuery: String,
    /**
     * Other intents with significant probabilities.
     */
    val otherIntentsProbabilities: Map<String, Double> = emptyMap(),
    /**
     * The static text response possibly returned for the [NlpQuery]
     */
    val staticResponse: String? = null,
    /**
     * Original intents (without qualifier) with significant probabilities.
     */
    val originalIntentsProbabilities: Map<String, Double> = emptyMap(),
) {
    fun firstValue(role: String): NlpEntityValue? = entities.firstOrNull { it.entity.role == role }

    fun entityTextContent(value: NlpEntityValue): String = retainedQuery.substring(value.start, value.end)

    fun hasIntent(
        intent: String,
        minProbability: Double = 0.0,
    ): Boolean = otherIntentsProbabilities.any { intent == it.key && it.value > minProbability }
}
