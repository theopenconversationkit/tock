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

import ai.tock.nlp.front.service.ParserRequestData

/**
 *
 */
internal object IntentSelectorService {

    fun isValidClassifiedSentence(data: ParserRequestData): Boolean {
        with(data) {
            return classifiedSentence != null &&
                query.context.checkExistingQuery &&
                data.isStateEnabledForIntentId(classifiedSentence.classification.intentId)
        }
    }

    fun selector(data: ParserRequestData): SelectorBase {
        return if (data.intentsQualifiers.isEmpty()) DefaultIntentSelector(data)
        else ExpectedIntentSelector(data)
    }
}
