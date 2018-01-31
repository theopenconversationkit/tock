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

package fr.vsct.tock.nlp.front.shared.config

import fr.vsct.tock.nlp.core.EntitiesRegexp
import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.shared.withNamespace
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

/**
 * Definition of an intent.
 */
data class IntentDefinition(val name: String,
                            val namespace: String,
                            val applications: Set<Id<ApplicationDefinition>>,
                            val entities: Set<EntityDefinition>,
                            val entitiesRegexp: Map<Locale, List<EntitiesRegexp>> = emptyMap(),
                            /**
                             * This intent is returned as a classification result
                             * only if at least one of the mandatory states is requested.
                             * There is no restriction for intents with empty mandatory states set.
                             */
                            val mandatoryStates: Set<String> = emptySet(),
                            val _id: Id<IntentDefinition> = newId()) {

    @Transient
    val qualifiedName: String = name.withNamespace(namespace)

    fun findEntity(type: String, role: String): EntityDefinition? {
        return entities.firstOrNull { it.entityTypeName == type && it.role == role }
    }

    fun findEntity(entity: Entity): EntityDefinition? {
        return findEntity(entity.entityType.name, entity.role)
    }

    fun hasEntity(entity: Entity): Boolean {
        return findEntity(entity) != null
    }

    fun supportStates(states: Set<String>): Boolean {
        return mandatoryStates.isEmpty()
                || states.any { mandatoryStates.contains(it) }
    }


}