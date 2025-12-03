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

package ai.tock.nlp.front.service

import ai.tock.nlp.core.Intent
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.parser.IntentQualifier
import ai.tock.nlp.front.shared.parser.ParseQuery
import org.litote.kmongo.Id

/**
 * Data used in [ParserService].
 */
internal data class ParserRequestData(
    val application: ApplicationDefinition,
    val query: ParseQuery,
    val classifiedSentence: ClassifiedSentence?,
    val intentsQualifiers: Set<IntentQualifier>,
    val intents: List<IntentDefinition>,
) {
    private val intentsById = intents.map { it._id to it }.toMap()
    private val intentsByName = intents.map { it.qualifiedName to it }.toMap()

    private fun isIntentEnabled(intentId: Id<IntentDefinition>?): Boolean {
        return intentsQualifiers.isEmpty() ||
            intentsQualifiers.any { it.intent == intentsById[intentId]?.qualifiedName }
    }

    fun isStateEnabledForIntentId(intentId: Id<IntentDefinition>?): Boolean {
        return isIntentEnabled(intentId) && intentsById[intentId]?.supportStates(query.state.states) ?: true
    }

    fun isStateSupportedByIntent(intent: Intent): Boolean {
        return intentsByName[intent.name]?.supportStates(query.state.states) ?: true
    }

    fun getModifierForIntent(intent: Intent): Double? {
        return intentsQualifiers.firstOrNull { it.intent == intent.name }?.modifier
    }
}
