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
import fr.vsct.tock.shared.withNamespace
import fr.vsct.tock.shared.withoutNamespace
import java.util.Locale

/**
 *
 */
data class IntentDefinition(val name: String,
                            val namespace:String,
                            val applications: Set<String>,
                            val entities: Set<EntityDefinition>,
                            val entitiesRegexp: Map<Locale, List<EntitiesRegexp>> = emptyMap(),
                            val _id: String? = null) {

    @Transient
    val qualifiedName : String = name.withNamespace(namespace)

    fun shortQualifiedName(defaultNamespace : String) = qualifiedName.withoutNamespace(defaultNamespace)

    fun findEntity(type: String, role: String): EntityDefinition? {
        return entities.firstOrNull { it.entityTypeName == type && it.role == role }
    }


}