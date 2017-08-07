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
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition

/**
 *
 */
internal class ExpectedIntentSelector(
        val forcedIntent: IntentDefinition,
        override val states: Set<String>,
        override val intentsMap: Map<String, IntentDefinition>) : SelectorBase() {

    override fun selectIntent(classification: IntentClassification): Pair<Intent, Double>? {
        with(classification) {
            var result: Pair<Intent, Double>? = null
            while (hasNext()) {
                (next() to probability())
                        .also { (intent, prob) ->
                            //forced intent is always allowed
                            if (forcedIntent.qualifiedName == intent.name) {
                                result = intent to prob
                            }
                            if (prob > 0.1) {
                                if (isAllowedIntent(intent)) {
                                    otherIntents.put(intent.name, prob)
                                }
                            } else if (result != null) {
                                return result
                            }
                        }
            }
        }
        return null
    }
}