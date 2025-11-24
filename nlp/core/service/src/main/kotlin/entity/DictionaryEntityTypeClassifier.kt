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
import ai.tock.nlp.model.EntityCallContext
import ai.tock.nlp.model.EntityCallContextForEntity
import ai.tock.nlp.model.EntityCallContextForIntent
import ai.tock.nlp.model.EntityCallContextForSubEntities
import ai.tock.shared.stripAccents
import java.util.Locale
import kotlin.text.RegexOption.IGNORE_CASE

internal object DictionaryEntityTypeClassifier : EntityTypeClassifier {
    override fun classifyEntities(
        context: EntityCallContext,
        text: String,
    ): List<EntityTypeRecognition> {
        return when (context) {
            is EntityCallContextForIntent -> classifyForIntent(context, text.stripAccents().trim())
            is EntityCallContextForEntity -> emptyList() // TODO
            is EntityCallContextForSubEntities -> emptyList() // TODO
        }
    }

    private fun classifyForIntent(
        context: EntityCallContextForIntent,
        text: String,
    ): List<EntityTypeRecognition> {
        return context
            .intent
            .entities
            .asSequence()
            .map { it.entityType }
            .distinct()
            .map { e ->
                val data = DictionaryRepositoryService.getDictionary(e)
                if (data != null) {
                    val labelsMap = data.getLabelsMap(context.language)
                    val synonyms = labelsMap.values.flatMap { it ?: emptyList() }

                    synonyms
                        .asSequence()
                        .distinct()
                        .filter { synonym -> text.contains(synonym, true) }
                        .flatMap { synonym ->
                            "\\s+($synonym)\\s+|^($synonym)$|^($synonym)\\s+|\\s+($synonym)$"
                                .toRegex(IGNORE_CASE)
                                .findAll(text)
                                .mapNotNull { m ->
                                    m.groups
                                        .filterNotNull()
                                        .firstOrNull { it.value.equals(synonym, true) }
                                        ?.let { g ->
                                            val predefinedValueOfSynonym =
                                                predefinedValueOfSynonym(
                                                    context.language,
                                                    labelsMap,
                                                    synonym,
                                                )
                                            if (predefinedValueOfSynonym != null) {
                                                EntityTypeRecognition(
                                                    EntityTypeValue(
                                                        g.range.first,
                                                        g.range.last + 1,
                                                        e,
                                                        predefinedValueOfSynonym.value,
                                                        true,
                                                    ),
                                                    1.0,
                                                )
                                            } else {
                                                null
                                            }
                                        }
                                }
                        }
                } else {
                    emptySequence()
                }
            }
            .flatMap { it }
            .distinct()
            .toList()
    }

    private fun predefinedValueOfSynonym(
        locale: Locale,
        predefinedValues: Map<PredefinedValue, List<String>?>,
        text: String,
    ): PredefinedValue? {
        for (predefinedValue in predefinedValues.keys) {
            val allValues = predefinedValues[predefinedValue]
            if (allValues != null) {
                val synonyms = allValues.toMutableList() + predefinedValue.value
                if (synonyms.find { s -> s.lowercase(locale) == text.lowercase(locale).stripAccents() } != null) {
                    return predefinedValue
                }
            }
        }
        return null
    }
}
