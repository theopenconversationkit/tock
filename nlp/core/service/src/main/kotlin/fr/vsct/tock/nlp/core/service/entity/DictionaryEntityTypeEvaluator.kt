/*
 * Copyright (C) 2018 VSCT
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

package fr.vsct.tock.nlp.core.service.entity

import fr.vsct.tock.nlp.core.PredefinedValue
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import org.simmetrics.metrics.StringMetrics
import java.util.Locale

internal object DictionaryEntityTypeEvaluator : EntityTypeEvaluator {

    private val levenshtein = StringMetrics.damerauLevenshtein()

    override fun evaluate(context: EntityCallContextForEntity, text: String): EvaluationResult {
        val dictionary = DictionaryRepositoryService.getDictionary(context.entityType)

        val value = dictionary?.let {
            findValue(context.language, it.getLabelsMap(context.language), it.onlyValues, it.minDistance, text)
        }
        return EvaluationResult(true, value?.first?.value, value?.second ?: 0.0)
    }

    private fun findValue(
        locale: Locale,
        predefinedValues: Map<PredefinedValue, List<String>?>,
        onlyValues: Boolean,
        minDistance: Double,
        text: String
    ): Pair<PredefinedValue?, Double> {
        val textToCompare = text.toLowerCase(locale)
        val values = predefinedValues.mapValues { l -> l.value?.map { s -> s.toLowerCase(locale) } ?: emptyList() }
        for (e in values) {
            val labels = e.value
            if (labels.any { s -> s.toLowerCase(locale) == textToCompare }) {
                return e.key to 1.0
            }
        }
        if (!onlyValues) {
            var distance = 0F;
            var key: PredefinedValue? = null
            for (e in values) {
                val max = e.value.asSequence().map { levenshtein.compare(text, it) }.max()
                if (max != null && max > distance) {
                    distance = max
                    key = e.key
                }
            }
            if (key != null && distance >= minDistance) {
                return key to distance.toDouble()
            }
        }
        return null to 0.2
    }

}