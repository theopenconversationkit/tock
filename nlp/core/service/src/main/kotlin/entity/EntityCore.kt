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

package ai.tock.nlp.core.service.entity

import ai.tock.nlp.core.CallContext
import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.merge.ValueDescriptor
import ai.tock.nlp.model.EntityCallContext
import ai.tock.nlp.model.EntityCallContextForEntity

/**
 *
 */
internal interface EntityCore {

    /**
     * Does the given [EntityType] supports values merge?
     */
    fun supportValuesMerge(entityType: EntityType): Boolean

    /**
     * Classifies entity types.
     */
    fun classifyEntityTypes(context: EntityCallContext, text: String): List<EntityTypeRecognition>

    /**
     * Evaluate entity values.
     *
     * @param context the call context
     * @param text the query
     * @param entities the not yet evaluated identified entities
     *
     * @return the evaluated entities
     */
    fun evaluateEntities(context: CallContext, text: String, entitiesRecognition: List<EntityRecognition>): List<EntityRecognition>

    /**
     * Merge two or more values for the given [context].
     */
    fun mergeValues(context: EntityCallContextForEntity, values: List<ValueDescriptor>): ValueDescriptor?

    /**
     * Check entity model providers availability.
     */
    fun healthcheck(): Boolean
}
