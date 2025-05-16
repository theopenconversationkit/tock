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

package ai.tock.nlp.front.service.selector

import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.IntentClassification
import ai.tock.nlp.front.service.ParserRequestData

/**
 *
 */
internal class ExpectedIntentSelector(data: ParserRequestData) : SelectorBase(data) {

    override fun selectIntent(classification: IntentClassification): Pair<Intent, Double>? {
        with(classification) {
            val qualifiedIntents = mutableMapOf<Intent, Double>()
            val intentsProbabilities = mutableMapOf<String, Double>()

            while (hasNext()) {
                (next() to probability())
                    .also { (intent, prob) ->
                        // intents with modifiers are always supported
                        val modifier = data.getModifierForIntent(intent)

                        if (modifier != null) {
                            if (prob >= data.application.knownIntentThreshold) {
                                otherIntents[intent.name] = prob
                            }
                            qualifiedIntents[intent] = prob + modifier
                            intentsProbabilities[intent.name] = prob
                        }
                    }
            }

            return if (qualifiedIntents.isEmpty()) {
                null
            } else {
                val result = qualifiedIntents.entries.sortedByDescending { it.value }.first()
                // remove selected from other intents
                otherIntents.remove(result.key.name)
                result.key to (intentsProbabilities[result.key.name] ?: 0.0)
            }
        }
    }
}
