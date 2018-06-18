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
import mu.KotlinLogging
import java.util.Locale

object PredefinedValuesEntityEvaluator : EntityEvaluator {

    override fun evaluate(context: EntityCallContextForEntity, text: String): EvaluationResult {

        val predefinedValues = localizedPredefinedValues(context)

        val predefinedValue =
            predefinedValueOfLabel(context.language, predefinedValues, text)?.value

        return EvaluationResult(true, predefinedValue, if (predefinedValue == null) 0.5 else 1.0)
    }

    private fun localizedPredefinedValues(context: EntityCallContextForEntity): Map<PredefinedValue, List<String>?> {
        return context
            .entityType
            .predefinedValues
            .associate { predefinedValue -> predefinedValue to predefinedValue.labels[context.language] }
    }

    private fun predefinedValueOfLabel(
        locale: Locale,
        predefinedValues: Map<PredefinedValue, List<String>?>,
        text: String
    ): PredefinedValue? {
        for (predefinedValue in predefinedValues.keys) {
            val labels = predefinedValues[predefinedValue]
            if (labels != null && labels.find { s -> s.toLowerCase(locale) == text.toLowerCase(locale) } != null) {
                return predefinedValue
            }
        }
        return null
    }

}