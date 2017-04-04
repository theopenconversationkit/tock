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

package fr.vsct.tock.nlp.core

import fr.vsct.tock.nlp.core.sample.SampleExpression

/**
 *
 */
interface NlpCore {

    fun parse(context: CallContext,
              text: String,
              intentSelector: (List<IntentRecognition>) -> IntentRecognition? = { it.firstOrNull() }): ParsingResult

    fun updateIntentModel(context: BuildContext, expressions: List<SampleExpression>)

    fun updateEntityModelForIntent(context: BuildContext, intent: Intent, expressions: List<SampleExpression>)

    fun registeredNlpEngineTypes(): Set<NlpEngineType>

    /**
     * Evaluate entity values.
     *
     * @param context the call context
     * @param text the query
     * @param entities the not yet evaluated identified entities
     *
     * @return the evaluated entities
     */
    fun evaluateEntities(
            context: CallContext,
            text: String,
            entities: List<EntityRecognition>): List<EntityRecognition>

    /**
     * Returns all (built-in) evaluated entities.
     *
     * @return the evaluated entity types (namespace:name)
     */
    fun getEvaluatedEntityTypes(): Set<String>
}