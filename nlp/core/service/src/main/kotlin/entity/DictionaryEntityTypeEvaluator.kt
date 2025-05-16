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

import ai.tock.nlp.core.PredefinedValue
import ai.tock.nlp.entity.StringValue
import ai.tock.nlp.entity.ValueWithProbability
import ai.tock.nlp.model.EntityCallContextForEntity
import org.simmetrics.metrics.StringMetrics
import java.util.Locale

internal object DictionaryEntityTypeEvaluator : EntityTypeEvaluator {

    private val levenshtein = StringMetrics.damerauLevenshtein()

    override fun evaluate(context: EntityCallContextForEntity, text: String): EvaluationResult {
        val dictionary = DictionaryRepositoryService.getDictionary(context.entityType)

        val value = dictionary?.let {
            findValue(
                context.language,
                it.getLabelsMap(context.language),
                it.onlyValues,
                it.minDistance,
                text.trim(),
                it.textSearch
            )
        }
        return EvaluationResult(true, value, value?.candidates?.firstOrNull()?.probability ?: 1.0)
    }

    private fun findValue(
        locale: Locale,
        predefinedValues: Map<PredefinedValue, List<String>?>,
        onlyValues: Boolean,
        minDistance: Double,
        text: String,
        textSearch: Boolean
    ): StringValue? {
        val textToCompare = text.lowercase(locale)
        val values = predefinedValues.mapValues { l -> l.value?.map { s -> s.lowercase(locale) } ?: emptyList() }
        if (onlyValues) {
            for (e in values) {
                val labels = e.value
                if (labels.any { s -> s.lowercase(locale) == textToCompare }) {
                    return StringValue(e.key.value)
                }
            }
        } else {
            val acceptableValues = values.mapNotNull { e ->
                val max = e.value.asSequence().map { levenshtein.compare(textToCompare, it) }.maxOrNull()
                if (max != null && (max > minDistance || (textSearch && e.value.any { textToCompare.contains(it) }))) {
                    ValueWithProbability(e.key.value, max.toDouble())
                } else {
                    null
                }
            }.sortedByDescending { it.probability }

            return acceptableValues.firstOrNull()?.value?.let { StringValue(it, acceptableValues) }
        }
        return null
    }
}
