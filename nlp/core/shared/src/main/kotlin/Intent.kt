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

package ai.tock.nlp.core

import ai.tock.shared.TOCK_NAMESPACE
import java.util.LinkedHashSet
import java.util.Locale

/**
 * An intent is defined by an unique name.
 */
data class Intent(
    val name: String,
    val entities: List<Entity>,
    val entitiesRegexp: Map<Locale, LinkedHashSet<EntitiesRegexp>> = emptyMap()
) {

    companion object {
        const val UNKNOWN_INTENT_NAME: String = "$TOCK_NAMESPACE:unknown"
        val UNKNOWN_INTENT: Intent = Intent(UNKNOWN_INTENT_NAME, emptyList())
    }

    fun hasEntity(entityType: EntityType, role: String) =
        entities.any { it.entityType == entityType && it.role == role }

    fun getEntity(role: String): Entity = entities.firstOrNull { it.role == role } ?: error("Unknown entity $role")

    override fun equals(other: Any?): Boolean = name == (other as? Intent)?.name

    override fun hashCode(): Int = name.hashCode()
}