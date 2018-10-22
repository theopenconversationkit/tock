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

import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.PredefinedValue
import fr.vsct.tock.nlp.model.EntityCallContext
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import fr.vsct.tock.nlp.model.EntityCallContextForIntent
import fr.vsct.tock.nlp.model.EntityCallContextForSubEntities
import java.util.Locale
import java.text.Normalizer


internal object PredefinedValuesEntityClassifier : EntityTypeClassifier {

    override fun classifyEntities(
        context: EntityCallContext,
        text: String,
        tokens: Array<String>
    ): List<EntityTypeRecognition> {
        return when (context) {
            is EntityCallContextForIntent -> classifyForIntent(context, stripAccents(text))
            is EntityCallContextForEntity -> TODO()
            is EntityCallContextForSubEntities -> TODO()
        }
    }

    private fun classifyForIntent(context: EntityCallContextForIntent, text: String): List<EntityTypeRecognition> {
        return context
            .intent
            .entities
            .filter { e -> e.entityType.predefinedValues.isNotEmpty() }
            .filter { e -> e.entityType.predefinedValues.firstOrNull { pv -> pv.labels.containsKey(context.language) } != null }
            .map { e ->

                val synonyms = e
                    .entityType
                    .predefinedValues
                    .map { pv -> pv.value } +
                    e
                    .entityType
                    .predefinedValues
                    .flatMap { pv -> pv.labels[context.language]!! }


                return synonyms
                    .filter { synonym -> text.contains(synonym, true) }
                    .map {
                        synonym ->

                        val start = text.indexOf(synonym, 0, true)
                        val end = start + synonym.length

                        val predefinedValueOfSynonym = predefinedValueOfSynonym(
                            context.language,
                            localizedPredefinedValues(context.language, e.entityType),
                            synonym
                        )

                    EntityTypeRecognition(EntityTypeValue(start, end, e.entityType, predefinedValueOfSynonym!!.value, true), 1.0)

                }

            }
    }

    private fun localizedPredefinedValues(locale: Locale, entityType: EntityType): Map<PredefinedValue, List<String>?> {
        return entityType
            .predefinedValues
            .associate { predefinedValue -> predefinedValue to predefinedValue.labels[locale] }
    }

    private fun predefinedValueOfSynonym(
        locale: Locale,
        predefinedValues: Map<PredefinedValue, List<String>?>,
        text: String
    ): PredefinedValue? {
        for (predefinedValue in predefinedValues.keys) {
            if (predefinedValues[predefinedValue] != null) {
                val synonyms = predefinedValues[predefinedValue]!!.toMutableList() + predefinedValue.value
                if (synonyms.find { s -> s.toLowerCase(locale) == stripAccents(text.toLowerCase(locale)) } != null) {
                    return predefinedValue
                }
            }
        }
        return null
    }

    fun stripAccents(text: String): String {
        var string = text
        string = Normalizer.normalize(string, Normalizer.Form.NFD)
        string = string.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
        return string
    }

    fun supportedEntityTypes(): Set<String> {
        return setOf()
    }

}