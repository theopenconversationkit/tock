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

package fr.vsct.tock.nlp.opennlp

import fr.vsct.tock.nlp.core.IntentRecognition
import fr.vsct.tock.nlp.model.IntentContext
import fr.vsct.tock.nlp.model.service.engine.IntentModelHolder
import fr.vsct.tock.nlp.model.service.engine.NlpIntentClassifier
import opennlp.tools.ml.model.AbstractModel

/**
 *
 */
internal class OpenNlpIntentClassifier(model: IntentModelHolder) : NlpIntentClassifier(model) {

    override fun classifyIntent(context: IntentContext, text: String, tokens: Array<String>): List<IntentRecognition> {
        with(model) {
            val openNlpModel = nativeModel as AbstractModel
            val outcomes = openNlpModel.eval(tokens)
            return outcomes.mapIndexed { i, d ->
                IntentRecognition(application.getIntent(openNlpModel.getOutcome(i)), d)
            }.sortedByDescending { it.probability }
        }
    }
}