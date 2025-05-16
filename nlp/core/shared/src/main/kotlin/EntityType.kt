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

/**
 * A type of entity.
 */
data class EntityType(
    /**
     * The qualified name of the entity (ie namespace:name)
     */
    val name: String,
    /**
     * The sub entities of this entity if any.
     */
    val subEntities: List<Entity> = emptyList(),
    /**
     * Is the entity based on a dictionary (predefined set of data)?
     */
    val dictionary: Boolean = false,
    /**
     * Is the entity has to be systematically obfuscated?
     */
    val obfuscated: Boolean = false
) {

    fun hasSubEntities(): Boolean = subEntities.isNotEmpty()

    fun findSubEntity(role: String): Entity? = subEntities.first { it.role == role }

    override fun equals(other: Any?): Boolean = name == (other as? EntityType)?.name

    override fun hashCode(): Int = name.hashCode()
}
