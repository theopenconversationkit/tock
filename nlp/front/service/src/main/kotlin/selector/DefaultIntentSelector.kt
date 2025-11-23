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
internal class DefaultIntentSelector(data: ParserRequestData) : SelectorBase(data) {
    override fun selectIntent(classification: IntentClassification): Pair<Intent, Double>? {
        with(classification) {
            // select first
            var result: Pair<Intent, Double>? = null
            while (hasNext() && result == null) {
                next().apply {
                    val p = probability()
                    if (p >= data.application.knownIntentThreshold) {
                        originalIntents[name] = p
                    }

                    if (data.isStateSupportedByIntent(this)) {
                        result = this to p
                    }
                }
            }

            // and take all other intents where probability is greater than the application knownIntentThreshold
            while (hasNext()) {
                next().run {
                    val p = probability()
                    if (p >= data.application.knownIntentThreshold) {
                        originalIntents[name] = p
                    }
                    if (data.isStateSupportedByIntent(this)) {
                        (this to p)
                            .takeIf { (_, prob) -> prob >= data.application.knownIntentThreshold }
                            ?.also { (intent, prob) -> otherIntents[intent.name] = prob }
                    } else {
                        // continue
                    }
                } ?: break
            }

            return result
        }
    }
}
