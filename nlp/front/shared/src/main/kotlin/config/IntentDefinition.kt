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

package ai.tock.nlp.front.shared.config

import ai.tock.nlp.core.EntitiesRegexp
import ai.tock.nlp.core.Entity
import ai.tock.shared.withNamespace
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

/**
 * Definition of an intent.
 */
data class IntentDefinition(
    /**
     * The name of the intent.
     */
    val name: String,
    /**
     * The namespace of the intent.
     */
    val namespace: String,
    /**
     * The applications using this intent.
     */
    val applications: Set<Id<ApplicationDefinition>>,
    /**
     * The [EntityDefinition]s of this intent.
     */
    val entities: Set<EntityDefinition>,
    val entitiesRegexp: Map<Locale, LinkedHashSet<EntitiesRegexp>> = emptyMap(),
    /**
     * This intent is returned as a classification result
     * only if at least one of the mandatory states is requested.
     * There is no restriction for intents with empty mandatory states set.
     */
    val mandatoryStates: Set<String> = emptySet(),
    /**
     * The qualified sentences of each "shared intent" that contains only entities supported by the current intent
     * are used to build the entity model of this intent.
     */
    val sharedIntents: Set<Id<IntentDefinition>> = emptySet(),
    /**
     * The optional displayed label of the intent.
     */
    val label: String? = null,
    /**
     * The optional description of the intent.
     */
    val description: String? = null,
    /**
     * The optional category of the intent.
     */
    val category: String? = null,
    /**
     * The unique [Id] of the intent.
     */
    val _id: Id<IntentDefinition> = newId(),
) {
    /**
     * Qualified name (ie "namespace:name") of the intent.
     */
    @Transient
    val qualifiedName: String = name.withNamespace(namespace)

    fun findEntity(
        type: String,
        role: String,
    ): EntityDefinition? {
        return entities.firstOrNull { it.entityTypeName == type && it.role == role }
    }

    fun findEntity(role: String): EntityDefinition? {
        return entities.firstOrNull { it.role == role }
    }

    fun findEntity(entity: Entity): EntityDefinition? {
        return findEntity(entity.entityType.name, entity.role)
    }

    fun hasEntity(entity: Entity): Boolean {
        return findEntity(entity) != null
    }

    fun hasEntity(entity: ClassifiedEntity): Boolean {
        return findEntity(entity.type, entity.role) != null
    }

    fun supportStates(states: Set<String>): Boolean {
        return mandatoryStates.isEmpty() ||
            states.any { mandatoryStates.contains(it.lowercase()) }
    }
}
