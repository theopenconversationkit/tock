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

package ai.tock.nlp.core

/**
 * To select the best intent match - default take the highest probability.
 */
interface IntentSelector {
    companion object {
        val defaultIntentSelector =
            object : IntentSelector {
                override fun selectIntent(classification: IntentClassification): Pair<Intent, Double>? {
                    return if (classification.hasNext()) classification.next() to classification.probability() else null
                }
            }
    }

    fun selectIntent(classification: IntentClassification): Pair<Intent, Double>?
}
