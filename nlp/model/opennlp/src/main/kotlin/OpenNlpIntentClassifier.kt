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

package ai.tock.nlp.opennlp

import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.IntentClassification
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.service.engine.IntentModelHolder
import ai.tock.nlp.model.service.engine.NlpIntentClassifier
import opennlp.tools.ml.model.AbstractModel

/**
 *
 */
internal class OpenNlpIntentClassifier(model: IntentModelHolder) : NlpIntentClassifier(model) {
    override fun classifyIntent(
        context: IntentContext,
        text: String,
        tokens: Array<String>,
    ): IntentClassification {
        return with(model) {
            val openNlpModel = nativeModel as AbstractModel
            val outcomes =
                openNlpModel.eval(tokens)
                    .mapIndexed { index, d -> index to d }
                    .sortedByDescending { it.second }
                    .iterator()

            object : IntentClassification {
                var probability = 0.0

                override fun probability(): Double = probability

                override fun hasNext(): Boolean = outcomes.hasNext()

                override fun next(): Intent {
                    return outcomes.next().let { (index, proba) ->
                        probability = proba
                        application.getIntent(openNlpModel.getOutcome(index)) ?: Intent.UNKNOWN_INTENT
                    }
                }
            }
        }
    }
}
