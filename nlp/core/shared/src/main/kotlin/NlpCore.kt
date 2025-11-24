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

import ai.tock.nlp.core.IntentSelector.Companion.defaultIntentSelector
import ai.tock.nlp.core.merge.ValueDescriptor

/**
 * The main entry point for NLP.
 */
interface NlpCore {
    /**
     * Returns all built-in entities.
     *
     * @return the built-in entity types (namespace:name)
     */
    fun getBuiltInEntityTypes(): Set<String>

    /**
     * Parse text with NLP engine.
     */
    fun parse(
        context: CallContext,
        text: String,
        intentSelector: IntentSelector = defaultIntentSelector,
    ): ParsingResult

    /**
     * Supported nlp engines.
     */
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
        entities: List<EntityRecognition>,
    ): List<EntityRecognition>

    /**
     * Does the given [EntityType] supports values merge?
     */
    fun supportValuesMerge(entityType: EntityType): Boolean

    /**
     * Merge two or more values for the given [Entity].
     */
    fun mergeValues(
        context: CallContext,
        entity: Entity,
        values: List<ValueDescriptor>,
    ): ValueDescriptor?

    /**
     * Check engines availability.
     */
    fun healthcheck(): Boolean
}
