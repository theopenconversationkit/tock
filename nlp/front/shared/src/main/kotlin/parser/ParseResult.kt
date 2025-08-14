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

import ai.tock.shared.security.TockObfuscatorService
import java.util.Locale

/**
 * A NLP parse result.
 */
data class ParseResult(
    /**
     * The intent selected.
     */
    val intent: String,
    /**
     * The namespace of the selected intent.
     */
    val intentNamespace: String,
    /**
     * The language selected.
     */
    val language: Locale,
    /**
     * The selected entities.
     */
    val entities: List<ParsedEntityValue>,
    /**
     * The entities found but not retained.
     */
    val notRetainedEntities: List<ParsedEntityValue> = emptyList(),
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
     * Original intents (without qualifier) with significant probabilities.
     */
    val originalIntentsProbabilities: Map<String, Double> = emptyMap()
) {

    /**
     * Returns the first value for the specified entity role.
     */
    fun firstValue(role: String): ParsedEntityValue? = entities.firstOrNull { it.entity.role == role }

    /**
     * Obfuscates the result.
     */
    fun obfuscate(obfuscatedRanges: List<IntRange>): ParseResult {
        val obfuscatedQuery = TockObfuscatorService.obfuscate(
            text = retainedQuery,
            obfuscatedRanges = obfuscatedRanges
        ) ?: ""
        return copy(
            retainedQuery = obfuscatedQuery,
            entities = entities.map { it.obfuscate(obfuscatedQuery) },
            notRetainedEntities = notRetainedEntities.map { it.obfuscate(obfuscatedQuery) }
        )
    }
}
