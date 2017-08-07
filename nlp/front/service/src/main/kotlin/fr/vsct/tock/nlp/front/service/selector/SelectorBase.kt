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
import fr.vsct.tock.nlp.core.IntentSelector
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition

/**
 *
 */
internal abstract class SelectorBase : IntentSelector {

    /**
     * The intents with p > 0.1
     */
    val otherIntents: MutableMap<String, Double> = mutableMapOf()

    /**
     * State of the request
     */
    open val states: Set<String> = emptySet()

    /**
     * The intents application map (key: qualified name)
     */
    open val intentsMap: Map<String, IntentDefinition> = emptyMap()

    fun isAllowedIntent(intent: Intent): Boolean {
        return intentsMap[intent.name]?.isAllowed(states) ?: true
    }
}