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

package fr.vsct.tock.nlp.front.service.selector

import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.IntentClassification
import fr.vsct.tock.nlp.front.service.ParserRequestData

/**
 *
 */
internal class ExpectedIntentSelector(data: ParserRequestData) : SelectorBase(data) {

    override fun selectIntent(classification: IntentClassification): Pair<Intent, Double>? {
        with(classification) {
            val qualifiedIntents = mutableMapOf<Intent, Double>()
            val qualifiedIntentsNames = mutableSetOf<String>()

            while (hasNext()) {
                (next() to probability())
                        .also { (intent, prob) ->
                            //intents with modifiers are always supported
                            val modifier = data.getModifierForIntent(intent)

                            if (modifier != null) {
                                if (prob > 0.1) {
                                    otherIntents.put(intent.name, prob)
                                }
                                qualifiedIntents.put(intent, prob + modifier)
                                qualifiedIntentsNames.add(intent.name)
                            }

                            if (qualifiedIntents.isNotEmpty() && prob < 0.1 && data.intentsQualifiersNames.containsAll(qualifiedIntentsNames)) {
                                val result = qualifiedIntents.entries.sortedByDescending { it.value }.first()
                                val realProb = otherIntents[result.key.name] ?: prob
                                return result.key to realProb
                            }
                        }
            }

            return if (qualifiedIntents.isEmpty()) {
                null
            } else {
                val result = qualifiedIntents.entries.sortedByDescending { it.value }.first()
                val realProb = otherIntents[result.key.name] ?: 0.0
                result.key to realProb
            }
        }
    }
}