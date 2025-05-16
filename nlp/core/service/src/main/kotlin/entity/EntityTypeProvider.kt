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

import ai.tock.nlp.core.EntityType

/**
 * Implement this interface to support new entity types.
 * The implementation is loaded at runtime, using the java [java.util.ServiceLoader]
 * - you need to provide a META-INF/services/ai.tock.nlp.core.service.entity.EntityTypeProvider file.
 */
interface EntityTypeProvider {

    /**
     * Returns supported entity types - ie if the list of supported entity types if it is already known.
     */
    fun supportedEntityTypes(): Set<String> = emptySet()

    /**
     * Does the given [EntityType] can be classified?
     */
    fun supportClassification(namespace: String, entityTypeName: String): Boolean =
        supportedEntityTypes().contains("$namespace:$entityTypeName")

    /**
     * Does the given [EntityType] can be evaluated?
     */
    fun supportEvaluation(namespace: String, entityTypeName: String): Boolean = false

    /**
     * Does the given [EntityType] supports values merge?
     */
    fun supportValuesMerge(namespace: String, entityTypeName: String): Boolean = false

    /**
     * Returns the entity classifier - null by default.
     */
    fun getEntityTypeClassifier(): EntityTypeClassifier? = null

    /**
     * Returns the entity evaluator - null by default.
     */
    fun getEntityTypeEvaluator(): EntityTypeEvaluator? = null

    /**
     * Test the server is up.
     */
    fun healthcheck(): Boolean = true
}
