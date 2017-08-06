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

import fr.vsct.tock.nlp.core.IntentSelector.Companion.defaultIntentSelector
import fr.vsct.tock.nlp.core.merge.ValueDescriptor
import fr.vsct.tock.nlp.core.sample.SampleExpression

/**
 *
 */
interface NlpCore {

    /**
     * Parse text with NLP engine.
     */
    fun parse(context: CallContext,
              text: String,
              intentSelector: IntentSelector = defaultIntentSelector)
            : ParsingResult

    fun updateIntentModel(context: BuildContext, expressions: List<SampleExpression>)

    fun updateEntityModelForIntent(context: BuildContext, intent: Intent, expressions: List<SampleExpression>)

    fun supportedNlpEngineTypes(): Set<NlpEngineType>

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
     * Returns all (built-in) evaluable entities.
     *
     * @return the evaluated entity types (namespace:name)
     */
    fun getEvaluableEntityTypes(): Set<String>

    /**
     * Does the given [EntityType] supports values merge?
     */
    fun supportValuesMerge(entityType: EntityType): Boolean

    /**
     * Merge two or more values for the given [EntityType].
     */
    fun mergeValues(context: CallContext, entityType: EntityType, values: List<ValueDescriptor>): ValueDescriptor?
}