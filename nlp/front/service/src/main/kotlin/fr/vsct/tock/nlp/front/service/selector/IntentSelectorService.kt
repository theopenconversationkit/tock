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

import fr.vsct.tock.nlp.core.CallContext
import fr.vsct.tock.nlp.front.service.FrontRepository.config
import fr.vsct.tock.nlp.front.service.FrontRepository.core
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue
import fr.vsct.tock.shared.namespace
import fr.vsct.tock.shared.withoutNamespace

/**
 *
 */
internal object IntentSelectorService {

    fun isValidSentence(query: ParseQuery, sentence: ClassifiedSentence?, expectedIntent: IntentDefinition?): Boolean {
        return sentence != null
                && query.context.checkExistingQuery
                && (expectedIntent == null || expectedIntent._id == sentence.classification.intentId)
                && query.state.states
                .run {
                    config.getIntentById(sentence.classification.intentId)?.isAllowed(this) ?: true
                }

    }

    fun select(
            parseQuery: ParseQuery,
            application: ApplicationDefinition,
            callContext: CallContext,
            query: String,
            expectedIntent: IntentDefinition?): ParseResult {
        val states = parseQuery.state.states
        val intentsMap =
                config.getIntentsByApplicationId(application._id!!)
                        .map { it.qualifiedName to it }
                        .toMap()
        val selector =
                if (expectedIntent == null) DefaultIntentSelector(states, intentsMap)
                else ExpectedIntentSelector(expectedIntent, states, intentsMap)

        val result = core.parse(callContext, query, selector)

        return ParseResult(
                result.intent.withoutNamespace(),
                result.intent.namespace(),
                result.entities.map { ParsedEntityValue(it.value, it.probability, core.supportValuesMerge(it.entityType)) },
                result.intentProbability,
                result.entitiesProbability,
                query,
                selector.otherIntents)
    }
}