/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.nlp.front.service.selector

import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.IntentClassification
import ai.tock.nlp.front.service.ParserRequestData

/**
 *
 */
internal class DefaultIntentSelector(data: ParserRequestData) : SelectorBase(data) {

    override fun selectIntent(classification: IntentClassification): Pair<Intent, Double>? {
        with(classification) {
            //select first
            var result: Pair<Intent, Double>? = null
            while (hasNext() && result == null) {
                next().apply {
                    if (data.isStateSupportedByIntent(this)) {
                        result = this to probability()
                    }
                }
            }

            //and take all other intents where probability is greater than 0.1
            while (hasNext()) {
                next().run {
                    if (data.isStateSupportedByIntent(this)) {
                        (this to probability())
                                .takeIf { (_, prob) -> prob > 0.1 }
                                ?.also { (intent, prob) -> otherIntents.put(intent.name, prob) }
                    } else {
                        //continue
                    }
                } ?: break
            }

            return result
        }
    }
}